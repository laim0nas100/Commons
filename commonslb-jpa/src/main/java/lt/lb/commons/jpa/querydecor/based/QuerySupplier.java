package lt.lb.commons.jpa.querydecor.based;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CommonAbstractCriteria;

/**
 *
 * @author laim0nas100
 */
public interface QuerySupplier {

    public CommonAbstractCriteria produceQuery(EntityManager em);

    public Query build(EntityManager em);
}
