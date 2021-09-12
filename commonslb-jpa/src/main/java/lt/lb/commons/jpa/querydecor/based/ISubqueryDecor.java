package lt.lb.commons.jpa.querydecor.based;

import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import lt.lb.commons.jpa.querydecor.DecoratorPhases;

/**
 *
 * @author laim0nas100
 */
public interface ISubqueryDecor<T_ROOT, T_RESULT, CTX, M extends ISubqueryDecor<T_ROOT, T_RESULT, CTX, M>> extends IAbstractQueryDecor<T_ROOT, T_RESULT, CTX, M> {

    public <PARENT_ROOT> Subquery<T_RESULT> produceSubquery(EntityManager em, AbstractQuery<PARENT_ROOT> parentQuery);

    public M withDec3Subquery(Consumer<DecoratorPhases.Phase3Subquery<T_ROOT, T_RESULT, CTX>> cons);

    public M withPredSubquery(boolean having, Function<DecoratorPhases.Phase3Subquery<T_ROOT, T_RESULT, CTX>, Predicate> func);

    public default M withPredSubquery(Function<DecoratorPhases.Phase3Subquery<T_ROOT, T_RESULT, CTX>, Predicate> func) {
        return withPredSubquery(false, func);
    }

}
