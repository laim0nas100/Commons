package lt.lb.commons.jpa.querydecor.based;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaUpdate;

/**
 *
 * @author laim0nas100
 */
public interface IUpdateQueryDecor<T_ROOT, CTX, M extends IUpdateQueryDecor<T_ROOT, CTX, M>> extends ICommonRootQuery<T_ROOT, CTX, M>, QuerySupplier {

    @Override
    public CriteriaUpdate<T_ROOT> produceQuery(EntityManager em);

    @Override
    public default Query build(EntityManager em) {
        return em.createQuery(produceQuery(em));
    }

    public default int executeUpdate(EntityManager em) {
        return build(em).executeUpdate();
    }
}
