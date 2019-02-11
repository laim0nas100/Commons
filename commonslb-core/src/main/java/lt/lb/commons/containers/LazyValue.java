package lt.lb.commons.containers;

import java.util.function.Supplier;

/**
 * Value that loads after being called. Can manually set it.
 *
 * @author laim0nas100
 * @param <T> type
 */
public class LazyValue<T> extends Value<T> {

    protected long loaded = Long.MAX_VALUE;
    protected Supplier<Boolean> loader = () -> System.nanoTime() >= loaded;
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
        loaded = Long.MAX_VALUE;
    }

}
