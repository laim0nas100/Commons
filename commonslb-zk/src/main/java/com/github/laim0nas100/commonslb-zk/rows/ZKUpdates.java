package lt.lb.zk.rows;

import com.github.laim0nas100.commonslb.Java;
import com.github.laim0nas100.commonslb.rows.Updates;

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
