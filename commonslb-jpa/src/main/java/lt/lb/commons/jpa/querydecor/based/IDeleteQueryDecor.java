package lt.lb.commons.jpa.querydecor.based;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaDelete;

/**
 *
 * @author laim0nas100
 */
public interface IDeleteQueryDecor<T_ROOT, CTX, M extends IDeleteQueryDecor<T_ROOT, CTX, M>> extends ICommonRootQuery<T_ROOT,  CTX, M>, QuerySupplier {

    
    @Override
    public CriteriaDelete<T_ROOT> produceQuery(EntityManager em);

    @Override
    public default Query build(EntityManager em) {
        return em.createQuery(produceQuery(em));
    }


    public default int executeDelete(EntityManager em) {
        return build(em).executeUpdate();
    }
}
