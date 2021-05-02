package lt.lb.commons.jpa;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lt.lb.commons.containers.collections.CollectionOp;
import lt.lb.commons.func.Lambda;

/**
 *
 * Commonly used JPA predicate patterns
 *
 * @author laim0nas100
 */
public class QuickPred {

    public static <T> Lambda.L2R<CriteriaBuilder, T, Predicate> unlift1(Lambda.L1R<CriteriaBuilder, Lambda.L1R<T, Predicate>> func) {
        return (cb, exp) -> {
            return func.apply(cb).apply(exp);
        };
    }

//    public static <T, Y> Lambda.L3R<CriteriaBuilder, T, Y, Predicate> unlift2(Lambda.L1R<CriteriaBuilder, Lambda.L2R<T, Y, Predicate>> func) {
//        return (cb, exp1, exp2) -> {
//            return func.apply(cb).apply(exp1, exp2);
//        };
//    }
//
//    public static <T, Y, U> Lambda.L4R<CriteriaBuilder, T, Y, U, Predicate> unlift3(Lambda.L1R<CriteriaBuilder, Lambda.L3R<T, Y, U, Predicate>> func) {
//        return (cb, exp1, exp2, exp3) -> {
//            return func.apply(cb).apply(exp1, exp2, exp3);
//        };
//    }

    public static <T> Predicate bool(CriteriaBuilder cb, boolean b) {
        return b ? cb.conjunction() : cb.disjunction();
    }

    public static <T> Predicate inEmptyAllowed(CriteriaBuilder cb, Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(cb, true);
        }
        return exp.in(col);
    }

    public static <T> Predicate inEmptyDisallowed(CriteriaBuilder cb, Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(cb, false);
        }
        return exp.in(col);
    }

    public static <T> Predicate notInEmptyAllowed(CriteriaBuilder cb, Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(cb, true);
        }
        return exp.in(col).not();
    }

    public static <T> Predicate notInEmptyDisallowed(CriteriaBuilder cb, Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(cb, false);
        }
        return exp.in(col).not();
    }

    public static Predicate isTrueOrNull(CriteriaBuilder cb, Expression<Boolean> exp) {
        return cb.or(cb.isTrue(exp), cb.isNull(exp));
    }

    public static Predicate isFalseOrNull(CriteriaBuilder cb, Expression<Boolean> exp) {
        return cb.or(cb.isFalse(exp), cb.isNull(exp));
    }

}
