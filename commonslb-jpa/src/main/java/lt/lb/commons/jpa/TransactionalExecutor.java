package lt.lb.commons.jpa;

import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedSupplier;

/**
 *
 * Should be used as a proxy with one method, to make use of transaction
 * frameworks such as Spring.
 *
 * To make use of @Transactional annotation, implementations should explicitly
 * implement this method.
 *
 * @author laim0nas100
 */
public interface TransactionalExecutor {

    public default <T> SafeOpt<T> execute(UncheckedSupplier<T> supl) {
        return SafeOpt.ofGet(supl);
    }
}
