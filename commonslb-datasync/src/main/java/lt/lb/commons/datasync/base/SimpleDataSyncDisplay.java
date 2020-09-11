package lt.lb.commons.datasync.base;

import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * @author laim0nas100
 */
public class SimpleDataSyncDisplay<M> extends ExplicitDataSyncDisplay<M, M> {

    protected M managed;

    public SimpleDataSyncDisplay(M initial, ValueProxy<M> displaySync) {
        super(displaySync);
        withIdentityDisplay();
        this.managed = initial;
    }

    public SimpleDataSyncDisplay(ValueProxy<M> displaySync) {
        this(null, displaySync);
    }

    public SimpleDataSyncDisplay() {
        withIdentityDisplay();
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
