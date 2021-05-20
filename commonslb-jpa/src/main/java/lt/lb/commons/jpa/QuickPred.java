package lt.lb.commons.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
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
    public static <T extends Comparable<T>> Predicate inRange(CriteriaBuilder cb, Expression<T> exp, T min, T max) {
        boolean nMin = min == null;
        boolean nMax = max == null;
        if (nMin && nMax) { // no range, just allow any
            return bool(cb, true);
        } else if (!nMax && !nMin) { // full range
            return cb.and(cb.lessThanOrEqualTo(exp, max), cb.greaterThanOrEqualTo(exp, min));

        } else if (nMin) {
            return cb.lessThanOrEqualTo(exp, max);
        } else {
            return cb.greaterThanOrEqualTo(exp, min);
        }
    }

    public static <T extends Comparable<T>> Predicate inRangeExc(CriteriaBuilder cb, Expression<T> exp, T min, T max) {
        boolean nMin = min == null;
        boolean nMax = max == null;
        if (nMin && nMax) { // no range, just allow any
            return bool(cb, true);
        } else if (!nMax && !nMin) { // full range
            return cb.and(cb.lessThan(exp, max), cb.greaterThan(exp, min));

        } else if (nMin) {
            return cb.lessThan(exp, max);
        } else {
            return cb.greaterThan(exp, min);
        }
    }

    public static <T> Predicate bool(CriteriaBuilder cb, boolean b) {
        return b ? cb.conjunction() : cb.disjunction();
    }

    public static <T> Predicate in_emptyTrue(CriteriaBuilder cb, Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(cb, true);
        }
        return exp.in(col);
    }

    public static <T> Predicate in_emptyFalse(CriteriaBuilder cb, Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(cb, false);
        }
        return exp.in(col);
    }

    public static <T> Predicate notIn_emptyTrue(CriteriaBuilder cb, Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(cb, true);
        }
        return exp.in(col).not();
    }

    public static <T> Predicate notIn_emptyFalse(CriteriaBuilder cb, Expression<T> exp, Collection<T> col) {
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

    public static <T> Predicate splitNotIn(CriteriaBuilder cb, int chunk, Expression<T> exp, Collection<T> collection) {
        return split(false, cb, chunk, exp, collection);
    }

    public static <T> Predicate splitIn(CriteriaBuilder cb, int chunk, Expression<T> exp, Collection<T> collection) {
        return split(true, cb, chunk, exp, collection);
    }

    public static <T> Predicate split(boolean in, CriteriaBuilder cb, int chunk, Expression<T> exp, Collection<T> collection) {
        if (collection.isEmpty()) {
            return bool(cb, !in);
        }
        chunk = Math.max(chunk, 100);
        List<Predicate> preds = new ArrayList<>();
        CollectionOp.doBatchSet(chunk, collection, subSet -> {
            if (in) {
                preds.add(exp.in(subSet));
            } else {
                preds.add(exp.in(subSet).not());
            }
        });
        return in ? or(cb, preds) : and(cb, preds);
    }

    public static Predicate or(CriteriaBuilder cb, Collection<Predicate> preds) {
        Predicate[] toArray = preds.stream().toArray(s -> new Predicate[s]);
        return cb.or(toArray);
    }

    public static Predicate and(CriteriaBuilder cb, Collection<Predicate> preds) {
        Predicate[] toArray = preds.stream().toArray(s -> new Predicate[s]);
        return cb.and(toArray);
    }

}
