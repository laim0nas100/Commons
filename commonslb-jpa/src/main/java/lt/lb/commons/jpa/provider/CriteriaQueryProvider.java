package lt.lb.commons.jpa.provider;

import javax.persistence.criteria.CriteriaQuery;

/**
 *
 * @author laim0nas100
 */
public interface CriteriaQueryProvider<T> {
    public CriteriaQuery<T> getCriteriaQuery();
}
