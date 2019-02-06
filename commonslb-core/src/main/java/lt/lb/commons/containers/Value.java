package lt.lb.commons.containers;

import java.util.function.Supplier;
import lt.lb.commons.interfaces.ValueProxy;

/**
 *
 * @author laim0nas100
 *
 * Proxy class
 * @param <T> generic type
 */
public class Value<T> implements ValueProxy<T> {

    protected T value;

    /**
     * Create with null
     */
    public Value() {
    }

    /**
     * Create with explicit initial value
     *
     * @param val
     */
    public Value(T val) {
        this.value = val;
    }

    /**
     *
     * @return current value
     */
    @Override
    public T get() {
        return value;
    }

    /**
     *
     * @param val new value
     */
    @Override
    public void set(T val) {
        this.value = val;
    }

    @Override
    public String toString() {
        return this.value + "";
    }

    /**
     *
     * @param func new value
     * @return updated value
     */
    public T setAndGet(Supplier<T> func) {
        set(func.get());
        return get();
    }

    /**
     * @param func new value
     * @return old value
     */
    public T getAndSet(Supplier<T> func) {
        T got = this.get();
        set(func.get());
        return got;
    }

    /**
     * Method for making exceptions
     *
     * @param opname operation name
     * @param val
     * @return
     */
    protected Supplier<RuntimeException> makeException(String opname, Object val) {
        return () -> {
            return new RuntimeException("Invalid operation (" + this.get() + " " + opname + " " + val + ")");
        };
    }

    public boolean isNotNull() {
        return this.get() != null;
    }

    public boolean isEmpty() {
        return this.get() == null;
    }

}
