package lt.lb.commons.jpa.querydecor;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase2;

/**
 *
 * @author laim0nas100
 */
public class QueryDecor {

    public static <R, T> Function<Phase2<R>, Predicate> of(Function<Root<R>, Expression<T>> att, Function<CriteriaBuilder, Function<Expression<T>, Predicate>> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);

        return t -> {
            CriteriaBuilder cb = t.cb();
            Function<Expression<T>, Predicate> newFunc = func.apply(cb);
            if (newFunc == null) {
                return null;
            }
            Expression<T> exp = att.apply(t.root());
            return newFunc.apply(exp);

        };
    }

    public static <R, T> Function<Phase2<R>, Predicate> of(Function<Root<R>, Expression<T>> att, T val, Function<CriteriaBuilder, BiFunction<Expression<T>, T, Predicate>> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);

        return t -> {
            CriteriaBuilder cb = t.cb();
            BiFunction<Expression<T>, T, Predicate> newFunc = func.apply(cb);
            if (newFunc == null) {
                return null;
            }
            Expression<T> exp = att.apply(t.root());
            return newFunc.apply(exp, val);
        };
    }

    public static <R, T> Function<Phase2<R>, Predicate> of(Function<Root<R>, Expression<T>> att, T val, T val2, Function<CriteriaBuilder, Lambda.L3R<Expression<T>, T, T, Predicate>> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);

        return t -> {
            CriteriaBuilder cb = t.cb();
            Lambda.L3R<Expression<T>, T, T, Predicate> newFunc = func.apply(cb);
            if (newFunc == null) {
                return null;
            }
            Expression<T> exp = att.apply(t.root());
            return newFunc.apply(exp, val, val2);
        };
    }

}
