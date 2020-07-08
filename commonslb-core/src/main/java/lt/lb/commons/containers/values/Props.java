package lt.lb.commons.containers.values;

import java.util.HashMap;
import java.util.Objects;

/**
 * Property class that stores any value (including null).
 *
 * @author laim0nas100
 */
public class Props extends HashMap<String, Value> {

    /**
     * Explicit typing and property key information.
     * Designed to be used with Props.
     *
     * @param <T>
     */
    public static class PropGet<T> {

        public static <Type> PropGet<Type> of(String str) {
            return new PropGet<>(str);
        }
        public final String propKey;

        public PropGet(String propKey) {
            this.propKey = Objects.requireNonNull(propKey);
        }

        public Value<T> insert(Props props, T value) {
            Value<T> insert = props.insert(propKey, value);
            return insert;
        }

        public T get(Props props) {
            return props.getCast(propKey);
        }

        public Value<T> getAsValue(Props props) {
            return props.get(propKey);
        }

    }

    public Value insert(String key, Object value) {
        Value val = new Value(value);
        this.put(key, val);
        return val;
    }

    public <T> T getOrDefaultCast(String str, T val) {
        Value get = this.get(str);
        if (get == null) {
            return val;
        }
        return (T) get.get();
    }

    public <T> T getCast(String str) {
        Value get = this.get(str);
        if (get == null) {
            return null;
        }
        return (T) get.get();
    }

}
