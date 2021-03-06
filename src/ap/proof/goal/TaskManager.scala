/**
 * This file is part of Princess, a theorem prover for Presburger
 * arithmetic with uninterpreted predicates.
 * <http://www.philipp.ruemmer.org/princess.shtml>
 *
 * Copyright (C) 2009-2020 Philipp Ruemmer <ph_r@gmx.net>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of the authors nor the names of their
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ap.proof.goal;

import ap.util.Debug
import ap.basetypes.{LeftistHeap, HeapCollector}
import ap.terfor.conjunctions.{Conjunction, Quantifier}
import ap.terfor.preds.Predicate
import ap.proof.theoryPlugins.Plugin
import ap.parameters.{GoalSettings, Param}

object TaskManager {
  
  private def AC = Debug.AC_GOAL
  
  private implicit val orderTask : Ordering[PrioritisedTask] =
    new Ordering[PrioritisedTask] {
      def compare(thisTask : PrioritisedTask,
                  thatTask : PrioritisedTask) : Int =
        thisTask.priority compare thatTask.priority
    }
  
  protected[goal] type TaskHeap = LeftistHeap[PrioritisedTask, TaskInfoCollector]
    
  //////////////////////////////////////////////////////////////////////////////
  
  private def EMPTY_HEAP(abbrevLabels : Map[Predicate, Predicate]) : TaskHeap =
    LeftistHeap.EMPTY_HEAP(TaskInfoCollector.EMPTY(abbrevLabels))

  def EMPTY(settings : GoalSettings) : TaskManager =
    new TaskManager (EMPTY_HEAP(Param.ABBREV_LABELS(settings)),
                     (new EagerTaskAutomaton(
                        Param.THEORY_PLUGIN(settings))).INITIAL)
    
  val EMPTY : TaskManager = EMPTY(GoalSettings.DEFAULT)

  private object TRUE_EXCEPTION extends Exception
   
}

/**
 * An immutable class (priority queue) for handling a set of tasks in a proof
 * goal. Currently, this is implemented using a sorted set, but it would be
 * better to use a real immutable queue (leftist heap?).
 *
 * TODO: So far, no real subsumption checks are performed
 */
class TaskManager private (// the regular tasks that have a priority
                           prioTasks : TaskManager.TaskHeap,
                           // Preprocessing tasks that can sneak in before
                           // regular tasks.
                           eagerTasks : EagerTaskManager) {

  import TaskManager.TRUE_EXCEPTION
  
  def +(t : PrioritisedTask) = new TaskManager (prioTasks + t, eagerTasks)

  def ++ (elems: Iterable[PrioritisedTask]): TaskManager =
    this ++ elems.iterator

  def ++ (elems: Iterator[PrioritisedTask]): TaskManager =
    if (elems.hasNext)
      new TaskManager (prioTasks insertIt elems, eagerTasks)
    else
      this

  def enqueue(elems: PrioritisedTask*): TaskManager = (this ++ elems.iterator)

  /**
   * Remove the first task from the queue.
   */
  def removeFirst : TaskManager = {
    //-BEGIN-ASSERTION-/////////////////////////////////////////////////////////
    Debug.assertPre(TaskManager.AC, !isEmpty)
    //-END-ASSERTION-///////////////////////////////////////////////////////////

    nextEagerTask match {
      case None =>
        new TaskManager (prioTasks.deleteMin, eagerTasks afterTask prioTasks.findMin)
      case Some(task) =>
        new TaskManager (prioTasks, eagerTasks afterTask task)
    }
  }
  
  private val nextEagerTask : Option[EagerTask] =
    eagerTasks recommend prioTasks.findMinOption
  
  /** Returns the element with the smallest priority value in the queue,
   *  or throws an error if there is no element contained in the queue.
   *
   *  @return   the element with the smallest priority.
   */
  def max: Task = nextEagerTask getOrElse prioTasks.findMin

  /**
   * Dequeue as long as the given predicate is satisfied
   */
  def dequeueWhile(pred : Task => Boolean) : (TaskManager, Seq[Task]) = {
    val buffer = Vector.newBuilder[Task]
    
    var newPrioTasks = prioTasks
    var newEagerTasks = eagerTasks
    var prioOption = newPrioTasks.findMinOption
    
    var cont = true
    while (cont) {
      (newEagerTasks recommend prioOption) match {
        case None =>
          // for some reason, pattern matching does not work at this point
          // (compiler bug?)
          if (prioOption.isDefined && pred(prioOption.get)) {
            val task = prioOption.get
            buffer += task
            newPrioTasks = newPrioTasks.deleteMin
            prioOption = newPrioTasks.findMinOption
            newEagerTasks = newEagerTasks afterTask task
          } else {
            cont = false
          }
        case Some(task) =>
          if (pred(task)) {
            buffer += task
            newEagerTasks = newEagerTasks afterTask task
          } else {
            cont = false
          }
      }
    }
    
    val res = buffer.result
    if (res.isEmpty)
      (this, res)
    else
      (new TaskManager(newPrioTasks, newEagerTasks), res)
  }
  
  /**
   * Compute information about the prioritised tasks (eager tasks are not
   * considered at this point)
   */
  def taskInfos : TaskInfoCollector = prioTasks.collector

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Update all <code>PrioritisedTask</code> stored by this managed, making
   * use of possibly new facts and information from the goal. The argument
   * <code>stopUpdating</code> can be used to tell at which point the updating
   * of tasks can be stopped, because some task or fact has been discovered that
   * can be used right away.
   */
  def updateTasks(goal : Goal,
                  stopUpdating : Task => Boolean) : TaskManager = {
    // TODO: make this more efficient by detecting more early whether updates
    // are meaningful
    
  //  print("" + prioTasks.size + " ... ")
 
    val newPrioTasks : TaskManager.TaskHeap = 
      try {
        val facts = new scala.collection.mutable.ArrayBuffer[Conjunction]
        
        def factCollector(f : Conjunction) : Unit =
          if (f.isTrue) throw TRUE_EXCEPTION else (facts += f)
        var foundFactsTask : Boolean = false
        
        def updateTask(prioTask : PrioritisedTask)
                         : Iterator[PrioritisedTask] =
          prioTask.updateTask(goal, factCollector _) match {
            case Seq(newTask) if (prioTask eq newTask) => {
              if (stopUpdating(newTask))
                foundFactsTask = true
              null
            }
            case res => {
              if (res exists stopUpdating)
                foundFactsTask = true
              res.iterator
            }
          }
        
        val tasks = prioTasks.flatItMap(updateTask _, (h) => foundFactsTask)
        if (facts.isEmpty)
          tasks
        else
          tasks ++ (goal formulaTasks Conjunction.disj(facts, goal.order))
      } catch {
        case TRUE_EXCEPTION =>
          prioTasks ++ (goal formulaTasks Conjunction.TRUE)
      }
  //    println(newPrioTasks.size)
    
    new TaskManager (newPrioTasks, eagerTasks)
  }

  /**
   * Eliminate all prioritised tasks for which the given predicate is false.
   */
  def filter(p : PrioritisedTask => Boolean) : TaskManager = {
    var changed = false

    val newPrioTasks = prioTasks.flatItMap({ t =>
      if (p(t)) {
        null
      } else {
        changed = true
        Iterator.empty
      }
    }, (_) => false)

    if (changed)
      new TaskManager(newPrioTasks, eagerTasks)
    else
      this
  }

  //////////////////////////////////////////////////////////////////////////////

  def isEmpty : Boolean = prioTasks.isEmpty && nextEagerTask.isEmpty

  def prioritisedTasks : Iterable[PrioritisedTask] = prioTasks

  def finalEagerTask : Boolean = nextEagerTask.isDefined && eagerTasks.atFinal
  
  //////////////////////////////////////////////////////////////////////////////
/*
  def printSize(goal : Goal) = {
    print(prioTasks.size)
    print("\t")
    var num = 0
    var factsBefore = 0
    var factsAfter = 0
    for (t <- prioTasks.iterator) {
      t match {
        case t : FormulaTask => {
          val newTasks = t updateTask goal
          num = num + newTasks.size
          factsAfter = factsAfter + (for (t <- newTasks; if (t.isInstanceOf[AddFactsTask])) yield t).size
        }
        case _ => num = num + 1
      }
      t match {
        case t : AddFactsTask => factsBefore = factsBefore + 1
        case _ => // nothing
      }
    }
    print(num)
    print("\t")
    print(factsBefore)
    print("\t")
    print(factsAfter)
    if (factsBefore != factsAfter || prioTasks.size != num)
      println("\t*")
    else
      println
  }
*/
  override def toString : String = {
    val strings =
      for (t <- nextEagerTask.iterator ++
                prioTasks.sortedIterator.take(2)) yield t.toString

    "[" + (if (strings.hasNext)
             strings.reduceLeft((s1 : String, s2 : String) => s1 + ", " + s2)
           else
             "") + "]" +
    (if (prioTasks.size > 2)
      " (" + (prioTasks.size - 2) + " further tasks)"
     else
      "")
  }
  
}
