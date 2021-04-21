package lt.lb.commons.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author laim0nas100
 */
public interface EntityManagerAware {

    public EntityManager getEntityManager();

    public default EntityManagerFactory getEntityManagerFactory() {
        return getEntityManager().getEntityManagerFactory();
    }

}
