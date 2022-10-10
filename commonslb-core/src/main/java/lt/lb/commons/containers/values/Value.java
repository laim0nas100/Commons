package lt.lb.commons.containers.values;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof ValueProxy) {
            return Objects.equals(value, ((ValueProxy) obj).get());
        }
        return false;
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
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

}
