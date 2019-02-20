package lt.lb.commons.containers;

import java.util.function.Supplier;
import lt.lb.commons.Timer;

/**
 * Value that loads after being called. Can manually set it.
 *
 * @author laim0nas100
 * @param <T> type
 */
public class LazyValue<T> extends Value<T> {
    
    protected Long loaded = null;
    protected Supplier<Boolean> loader = () -> loaded != null && loaded >= Timer.getNanoTime();
    protected Supplier<T> supply;

    public LazyValue(Supplier<T> supply) {
        this.supply = supply;
    }

    public LazyValue(T value) {
        this(() -> value);
    }

    @Override
    public void set(T val) {
        loaded = Timer.getNanoTime();
        super.set(val);
    }

    @Override
    public T get() {
        if (!loader.get()) {
            this.set(supply.get());
        }
        return super.get();
    }
    
    public void invalidate(){
        loaded = null;
    }

}
