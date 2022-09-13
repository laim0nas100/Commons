package lt.lb.commons.jpa.querydecor;

import java.util.function.Function;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 */
public interface JpaExpResolvePredicate<SOURCE, SOURCE_P extends Path<SOURCE>, CTX>
        extends JpaExpResolvePredicates<SOURCE, Boolean, SOURCE_P, Predicate, CTX>,
        Function<DecoratorPhases.Phase2<SOURCE, CTX>, Predicate> {

    public default JpaExpResolvePredicate<SOURCE, SOURCE_P, CTX> not() {
        return (f, p) -> apply(f, p).not();
    }
}
