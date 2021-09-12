package lt.lb.commons.jpa.querydecor;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lt.lb.commons.func.Lambda;

/**
 *
 * Additional methods, and a way to add your own methods
 * @author laim0nas100
 */
public interface IQueryDecorExtensions<T_ROOT, T_RESULT, CTX, M extends IQueryDecor<T_ROOT, T_RESULT, CTX, M>> extends IQueryDecor<T_ROOT, T_RESULT, CTX, M> {

    public default <T> M withPredFunc(Function<Root<T_ROOT>, Expression<T>> att, Function<CriteriaBuilder, Function<Expression<T>, Predicate>> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);

        return withPred(t -> {
            Function<Expression<T>, Predicate> newFunc = func.apply(t.cb());
            if (newFunc == null) {
                return null;
            }
            Expression<T> exp = att.apply(t.root());
            return newFunc.apply(exp);

        });

    }

    public default <T> M withPredFunc(Function<Root<T_ROOT>, Expression<T>> att, T val, Function<CriteriaBuilder, BiFunction<Expression<T>, T, Predicate>> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);

        return withPred(t -> {
            BiFunction<Expression<T>, T, Predicate> newFunc = func.apply(t.cb());
            if (newFunc == null) {
                return null;
            }
            Expression<T> exp = att.apply(t.root());
            return newFunc.apply(exp, val);
        });

    }

    public default <T> M withPredFunc(Function<Root<T_ROOT>, Expression<T>> att, T val, T val2, Function<CriteriaBuilder, Lambda.L3R<Expression<T>, T, T, Predicate>> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);

        return withPred(t -> {
            Lambda.L3R<Expression<T>, T, T, Predicate> newFunc = func.apply(t.cb());
            if (newFunc == null) {
                return null;
            }
            Expression<T> exp = att.apply(t.root());
            return newFunc.apply(exp, val, val2);
        });
    }
}
