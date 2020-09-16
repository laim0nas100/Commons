package lt.lb.commons.rows;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.BindingValue;
import lt.lb.commons.interfaces.CloneSupport;

/**
 *
 * @author laim0nas100
 */
public abstract class Updates<U extends Updates> implements CloneSupport<U> {
    
    protected List<OrderedRunnable> updateListeners = new ArrayList<>();
    protected Deque<U> followUps = new ArrayDeque<>();
    protected BindingValue<Long> bindingValue = new BindingValue<>();
    
    protected Set<String> boundDestinations = new LinkedHashSet<>();
    public final String type;
    public boolean active = true;
    
    public String getID() {
        return bindingValue.id;
    }
    
    public Updates(String type) {
        this.type = type;
        bindingValue.addListener((oldval, newval) -> {
            if (!active) {
                return;
            }
            if (Objects.equals(oldval, newval)) {
                return;
            }
            updateListeners.stream().collect(Collectors.toList()).forEach(Runnable::run);
            followUps.stream().collect(Collectors.toList()).forEach(f->{
                f.triggerUpdate(newval);
            });
            
        });
    }

    public List<OrderedRunnable> getUpdateListeners() {
        return updateListeners;
    }

    public Deque<U> getFollowUps() {
        return followUps;
    }

    public String getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }
    
    
    
    protected Updates(Updates old) {
        this(old.type);
        this.bindingValue.set(F.cast(old.bindingValue.get()));
        for (Object idd : old.boundDestinations) {
            String id = F.cast(idd);
            this.bindingValue.addListener(id, bindingValue.getListener(id).get());
        }
        this.updateListeners.addAll(old.updateListeners);
        this.followUps = new ArrayDeque<>(old.followUps);
    }
    
    protected abstract U me();
    
    public U addUpdate(OrderedRunnable run) {
        Objects.requireNonNull(run);
        U me = me();
        me.updateListeners.add(run);
        me.updateListeners.sort(OrderedRunnable.asOrder());
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
        me.boundDestinations.add(dest.getID());
        return me;
    }
    
    public U unbind(U dest) {
        U me = me();
        if (me.boundDestinations.remove(dest.getID())) {
            me.bindingValue.unbind(dest.getID());
        }
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
    
    public U removeFollowUp(U followUp) {
        Objects.requireNonNull(followUp);
        U me = me();
        me.followUps.remove(followUp);
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
