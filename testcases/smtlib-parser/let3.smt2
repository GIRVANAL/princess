
(set-option :inline-let false)

(set-info :source "derived from QF_UFLIA/wisas/xs_9_9.smt2")
(set-info :status sat)

(declare-fun arg0 () Int)
(declare-fun arg1 () Int)
(declare-fun fmt0 () Int)
(declare-fun fmt1 () Int)
(declare-fun distance () Int)
(declare-fun fmt_length () Int)
(declare-fun adr_lo () Int)
(declare-fun adr_medlo () Int)
(declare-fun adr_medhi () Int)
(declare-fun adr_hi () Int)
(declare-fun format (Int) Int)
(declare-fun percent () Int)
(declare-fun s () Int)
(declare-fun s_count (Int) Int)
(declare-fun x () Int)
(declare-fun x_count (Int) Int)
(assert 

(let ((?v_1 (+ fmt0 1)) (?v_0 (- (- fmt1 2) fmt0)) (?v_7 (format 0))) 
(let ((?v_16 (= ?v_7 percent)) (?v_8 (format 1))) 
(let ((?v_20 (= ?v_8 percent)) (?v_17 (= ?v_8 s)) (?v_51 (= ?v_8 x)) (?v_9 (format 2))) 
(let ((?v_24 (= ?v_9 percent)) (?v_21 (= ?v_9 s)) (?v_54 (= ?v_9 x)) (?v_10 (format 3))) 
(let ((?v_28 (= ?v_10 percent)) (?v_25 (= ?v_10 s)) (?v_57 (= ?v_10 x)) (?v_11 (format 4))) 
(let ((?v_32 (= ?v_11 percent)) (?v_29 (= ?v_11 s)) (?v_60 (= ?v_11 x)) (?v_12 (format 5))) 
(let ((?v_36 (= ?v_12 percent)) (?v_33 (= ?v_12 s)) (?v_63 (= ?v_12 x)) (?v_13 (format 6))) 
(let ((?v_40 (= ?v_13 percent)) (?v_37 (= ?v_13 s)) (?v_66 (= ?v_13 x)) (?v_14 (format 7))) 
(let ((?v_44 (= ?v_14 percent)) (?v_41 (= ?v_14 s)) (?v_69 (= ?v_14 x)) (?v_15 (format 8))) 
(let ((?v_48 (= ?v_15 percent)) (?v_45 (= ?v_15 s)) (?v_72 (= ?v_15 x)) (?v_18 (and ?v_16 ?v_17)) (?v_19 (s_count 0)) (?v_22 (and ?v_20 ?v_21)) (?v_23 (s_count 1)) (?v_26 (and ?v_24 ?v_25)) (?v_27 (s_count 2)) (?v_30 (and ?v_28 ?v_29)) (?v_31 (s_count 3)) (?v_34 (and ?v_32 ?v_33)) (?v_35 (s_count 4)) (?v_38 (and ?v_36 ?v_37)) (?v_39 (s_count 5)) (?v_42 (and ?v_40 ?v_41)) (?v_43 (s_count 6))) 
(let ((?v_46 (and ?v_44 ?v_45)) (?v_47 (s_count 7)) (?v_75 (format 9))) 
(let ((?v_49 (and ?v_48 (= ?v_75 s))) (?v_50 (s_count 8)) (?v_52 (and ?v_16 ?v_51)) (?v_53 (x_count 0)) (?v_55 (and ?v_20 ?v_54)) (?v_56 (x_count 1)) (?v_58 (and ?v_24 ?v_57)) (?v_59 (x_count 2)) (?v_61 (and ?v_28 ?v_60)) (?v_62 (x_count 3)) (?v_64 (and ?v_32 ?v_63)) (?v_65 (x_count 4)) (?v_67 (and ?v_36 ?v_66)) (?v_68 (x_count 5)) (?v_70 (and ?v_40 ?v_69)) (?v_71 (x_count 6)) (?v_73 (and ?v_44 ?v_72)) (?v_74 (x_count 7)) (?v_76 (and ?v_48 (= ?v_75 x))) (?v_77 (x_count 8)) (?v_2 (+ fmt0 0)) (?v_3 (+ fmt0 2)) (?v_4 (+ fmt0 3)) (?v_5 (+ fmt0 4)) (?v_6 (+ fmt0 5))) 

   ?v_66 )))))))))))))

(check-sat)
