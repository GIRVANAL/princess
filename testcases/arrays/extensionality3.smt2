(set-logic QF_AUFLIA)

(set-info :status unsat)
(set-info :notes |This benchmark is designed to require an array DP to propagate a properly entailed disjunction of equalities between shared terms.|)

(declare-fun a () (Array Int Int))
(declare-fun b () (Array Int Int))
(declare-fun c () (Array Int Int))
(declare-fun v () Int)
(declare-fun w () Int)
(declare-fun x () Int)
(declare-fun y () Int)
(declare-fun g ((Array Int Int)) Int)

(assert (= b (store a x v)))
(assert (= c (store b y w)))
(assert (= v (select a x)))
(assert (= w (select a y)))
(assert (not (= (g a) (g c))))
(check-sat)
(exit)
