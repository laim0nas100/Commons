package lt.lb.commons.datasync.base;

import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * @author laim0nas100
 */
public class SimpleDataSyncPersist<M> extends ExplicitDataSyncPersist<M, M> {

    protected M managed;

    public SimpleDataSyncPersist(M initial, ValueProxy<M> persistSync) {
        super(persistSync);
        withIdentityPersist();
        managed = initial;
    }

    public SimpleDataSyncPersist(ValueProxy<M> persistSync) {
        this(null, persistSync);
    }

    public SimpleDataSyncPersist() {
        withIdentityPersist();
    }

    @Override
    public M getManaged() {
        return managed;
    }

    @Override
    public void setManaged(M managed) {
        this.managed = managed;
    }

}
