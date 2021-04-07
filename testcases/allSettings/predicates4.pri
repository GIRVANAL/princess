\existentialConstants {
  /* Declare parameters of the problem
   * (implicitly existentially quantified, outermost level)
   *
   * int x, y, z;
   */
}

\functions {
  /* Declare constants occurring in the problem
   * (implicitly universally quantified)
   *
   * int c, d, e;
   */  
}

\predicates {
  /* Declare predicates occurring in the problem
   * (implicitly universally quantified)
   *
   * r(int, int); p(int); q;
   */
  p; q; r;  
}

\problem {
  (p() -> q()) & ( !p() -> r())
<->
  (p() & q()) | (!p() & r())
}
