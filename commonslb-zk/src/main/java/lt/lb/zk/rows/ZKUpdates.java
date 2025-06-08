package lt.lb.zk.rows;

import lt.lb.commons.Java;
import lt.lb.commons.rows.Updates;

/**
 *
 * @author laim0nas100
 */
public class ZKUpdates extends Updates<ZKUpdates> {

    public ZKUpdates(String type) {
        super(type);
    }

    protected ZKUpdates(ZKUpdates up) {
        super(up);
    }

    @Override
    protected ZKUpdates me() {
        return this;
    }

    @Override
    public ZKUpdates clone() {
        return new ZKUpdates(this);
    }

    public void commit() {
        if (active) {
            triggerUpdate(Java.getNanoTime());
        }
    }

}
