package lt.lb.commons.containers.values;

import java.util.Objects;
import java.util.function.Supplier;

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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Value<?> other = (Value<?>) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
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
        return this.value + "";
    }

}
