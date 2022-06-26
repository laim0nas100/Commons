package lt.lb.commons.jpa.provider;

import javax.persistence.criteria.CriteriaBuilder;

/**
 *
 * @author laim0nas100
 */
public interface CriteriaBuilderProvider {

    public CriteriaBuilder getCriteriaBuilder();
}
