package lt.lb.commons.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lt.lb.commons.containers.collections.CollectionOp;

/**
 *
 * @author laim0nas100
 */
public interface ExtCriteriaBuilder extends DelegatedCriteriaBuilder {
    
    public static ExtCriteriaBuilder of(final CriteriaBuilder delegate){
        Objects.requireNonNull(delegate, "CriteriaBuilder delegate must be provided");
        return ()->delegate;
    }
    
    public default <T extends Comparable<T>> Predicate inRange(CriteriaBuilder cb, Expression<T> exp, T min, T max) {
        boolean nMin = min == null;
        boolean nMax = max == null;
        if (nMin && nMax) { // no range, just allow any
            return bool(true);
        } else if (!nMax && !nMin) { // full range
            return cb.and(cb.lessThanOrEqualTo(exp, max), cb.greaterThanOrEqualTo(exp, min));

        } else if (nMin) {
            return cb.lessThanOrEqualTo(exp, max);
        } else {
            return cb.greaterThanOrEqualTo(exp, min);
        }
    }

    public default <T extends Comparable<T>> Predicate inRangeExc(CriteriaBuilder cb, Expression<T> exp, T min, T max) {
        boolean nMin = min == null;
        boolean nMax = max == null;
        if (nMin && nMax) { // no range, just allow any
            return bool(true);
        } else if (!nMax && !nMin) { // full range
            return cb.and(cb.lessThan(exp, max), cb.greaterThan(exp, min));

        } else if (nMin) {
            return cb.lessThan(exp, max);
        } else {
            return cb.greaterThan(exp, min);
        }
    }

    public default <T> Predicate bool(boolean b) {
        return b ? conjunction() : disjunction();
    }

    public default <T> Predicate in_emptyTrue(Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(true);
        }
        return exp.in(col);
    }

    public default <T> Predicate in_emptyFalse(Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(false);
        }
        return exp.in(col);
    }

    public default <T> Predicate notIn_emptyTrue(Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(true);
        }
        return exp.in(col).not();
    }

    public default <T> Predicate notIn_emptyFalse(Expression<T> exp, Collection<T> col) {
        if (CollectionOp.isEmpty(col)) {
            return bool(false);
        }
        return exp.in(col).not();
    }

    public default Predicate isTrueOrNull(Expression<Boolean> exp) {
        return or(isTrue(exp), isNull(exp));
    }

    public default Predicate isFalseOrNull(Expression<Boolean> exp) {
        return or(isFalse(exp), isNull(exp));
    }

    public default <T> Predicate splitNotIn(int chunk, Expression<T> exp, Collection<T> collection) {
        return split(false, chunk, exp, collection);
    }

    public default <T> Predicate splitIn(int chunk, Expression<T> exp, Collection<T> collection) {
        return split(true, chunk, exp, collection);
    }

    public default <T> Predicate split(boolean in, int chunk, Expression<T> exp, Collection<T> collection) {
        if (collection.isEmpty()) {
            return bool(!in);
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
        return in ? or(preds) : and(preds);
    }

    public default Predicate or(Collection<Predicate> preds) {
        return or(preds.stream().toArray(s -> new Predicate[s]));
    }

    public default Predicate and(Collection<Predicate> preds) {
        return and(preds.stream().toArray(s -> new Predicate[s]));
    }
}
