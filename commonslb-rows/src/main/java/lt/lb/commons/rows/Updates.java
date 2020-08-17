package lt.lb.commons.rows;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.BindingValue;
import lt.lb.commons.interfaces.CloneSupport;

/**
 *
 * @author laim0nas100
 */
public abstract class Updates<U extends Updates> implements CloneSupport<U> {

    protected Deque<Runnable> list = new ArrayDeque<>();
    protected Queue<OrderedRunnable> updateListeners = new PriorityQueue<>();
    protected Deque<U> followUps = new ArrayDeque<>();
    protected BindingValue<Long> bindingValue = new BindingValue<>();

    protected Set<String> boundDestinations = new LinkedHashSet<>();
    public final String type;
    public boolean active = true;

    public Updates(String type) {
        this.type = type;
        bindingValue.addListener((oldval, newval) -> {
            if (!active) {
                return;
            }
            if (Objects.equals(oldval, newval)) {
                return;
            }
            for (Runnable run : list) {
                run.run();
            }
            for (U followUp : followUps) {
                followUp.triggerUpdate(newval);
            }

        });
    }

    protected Updates(Updates old) {
        this(old.type);
        this.bindingValue.set(F.cast(old.bindingValue.get()));
        for (Object idd : old.boundDestinations) {
            String id = F.cast(idd);
            this.bindingValue.addListener(id, bindingValue.getListener(id).get());
        }
        this.list = new ArrayDeque<>(old.list);
        this.followUps = new ArrayDeque<>(old.followUps);
    }

    protected abstract U me();

    public U addUpdate(OrderedRunnable run) {
        Objects.requireNonNull(run);
        U me = me();
        me.list.add(run);
        return me;
    }

    public U addUpdate(int order, Runnable run) {
        return addUpdate(new OrderedRunnable(order, run));
    }

    public U addUpdate(Runnable run) {
        return addUpdate(0, run);
    }

    public U bindPropogate(U dest) {
        Objects.requireNonNull(dest);
        U me = me();
        me.bindingValue.bindPropogate(dest.bindingValue);
        me.boundDestinations.add(dest.bindingValue.id);
        return me;
    }

    public U triggerUpdate(Long timeInstance) {
        U me = me();
        me.bindingValue.set(timeInstance);
        return me;
    }

    public U addFollowUp(U followUp) {
        Objects.requireNonNull(followUp);
        U me = me();
        me.followUps.add(followUp);
        return me;
    }

    @Override
    public abstract U clone();

    public static class DefaultUpdates extends Updates<DefaultUpdates> {

        public DefaultUpdates(String type) {
            super(type);
        }

        protected DefaultUpdates(DefaultUpdates old) {
            super(old);
        }

        @Override
        protected DefaultUpdates me() {
            return this;
        }

        @Override
        public DefaultUpdates clone() {
            return new DefaultUpdates(this);
        }

    }
}
