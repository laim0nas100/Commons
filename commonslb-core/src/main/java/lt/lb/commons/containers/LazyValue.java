package lt.lb.commons.containers;

import java.util.function.Supplier;

/**
 * Value that loads after being called. Can manually set it.
 *
 * @author laim0nas100
 * @param <T> type
 */
public class LazyValue<T> extends Value<T> {

    
    
    protected Long loaded = null;
    protected Supplier<Boolean> loader = () -> loaded != null && System.nanoTime() - loaded >= 0;
    protected Supplier<T> supply;

    public LazyValue(Supplier<T> supply) {
        this.supply = supply;
    }

    public LazyValue(T value) {
        this(() -> value);
    }

    @Override
    public void set(T val) {
        loaded = System.nanoTime();
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
