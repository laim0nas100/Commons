package lt.lb.commons.containers;

import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 *
 * Proxy class
 * @param <T> generic type
 */
public class Value<T> {

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
    public T get() {
        return value;
    }

    /**
     *
     * @param val new value
     */
    public void set(T val) {
        this.value = val;
    }

    /**
     * Use as a Bean
     *
     * @return value
     */
    public T getValue() {
        return value;
    }

    /**
     * Use as a Bean
     *
     * @param value to set
     */
    public void setValue(T value) {
        this.value = value;
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
     * @param opname operation name
     * @param val
     * @return 
     */
    protected Supplier<RuntimeException> makeException(String opname, Object val) {
        return () -> {
            return new RuntimeException("Invalid operation (" + this.get() + " " + opname + " " + val + ")");
        };
    }

}
