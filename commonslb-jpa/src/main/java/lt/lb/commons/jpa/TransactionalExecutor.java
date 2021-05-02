package lt.lb.commons.jpa;

import lt.lb.uncheckedutils.CheckedExecutorUnified;

/**
 *
 * Should be used as a proxy with one method, to make use of transaction
 * frameworks such as Spring.
 *
 * To make use of @Transactional annotation, implementations should explicitly
 * implement all relevant methods.
 *
 * @author laim0nas100
 */
public interface TransactionalExecutor extends CheckedExecutorUnified {

}
