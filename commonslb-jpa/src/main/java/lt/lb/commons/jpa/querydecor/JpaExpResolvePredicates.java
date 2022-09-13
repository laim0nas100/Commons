package lt.lb.commons.jpa.querydecor;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 */
public interface JpaExpResolvePredicates<SOURCE, CURRENT, SOURCE_P extends Path<SOURCE>, CURRENT_P extends Expression<CURRENT>, CTX>
        extends JpaExpResolve<SOURCE, CURRENT, SOURCE_P, CURRENT_P, CTX> {

    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> equal(CURRENT value) {
        return (f, p) -> f.cb().equal(apply(f, p), value);
    }
    
    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> notEqual(CURRENT value) {
        return (f, p) -> f.cb().notEqual(apply(f, p), value);
    }
    
    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> isNull() {
        return (f, p) -> f.cb().isNull(apply(f, p));
    }
    
    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> isNotNull() {
        return (f, p) -> f.cb().isNotNull(apply(f, p));
    }

    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> in(CURRENT... objects) {
        Objects.requireNonNull(objects);
        if (objects.length == 0) {
            throw new IllegalArgumentException("Object array using 'in' via JpaExpResolve is empty");
        }
        return (f, p) -> apply(f, p).in(objects);
    }

    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> in(Collection<CURRENT> objects) {
        Objects.requireNonNull(objects);
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("Object collection using 'in' via JpaExpResolve is empty");
        }
        return (f, p) -> apply(f, p).in(objects);
    }

    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> notIn(CURRENT... objects) {
        Objects.requireNonNull(objects);
        if (objects.length == 0) {
            throw new IllegalArgumentException("Object array using 'in' via JpaExpResolve is empty");
        }
        return (f, p) -> apply(f, p).in(objects).not();
    }

    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> notIn(Collection<CURRENT> objects) {
        Objects.requireNonNull(objects);
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("Object collection using 'in' via JpaExpResolve is empty");
        }
        return (f, p) -> apply(f, p).in(objects).not();
    }

    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> thenPredicate(BiFunction<DecoratorPhases.Phase1<CTX>, CURRENT_P, Predicate> functor) {
        Objects.requireNonNull(functor);
        return (f, p) -> functor.apply(f, apply(f, p));
    }
}
