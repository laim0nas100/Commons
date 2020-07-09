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
     * Explicit typing and property key information. Designed to be used with
     * Props.
     *
     * @param <T>
     */
    public static class PropGet<T> {

        /**
         * Construct PropGet of specified key and explicit type.
         * @param <Type>
         * @param str
         * @return 
         */
        public static <Type> PropGet<Type> of(String str) {
            return new PropGet<>(str);
        }
        public final String propKey;

        public PropGet(String propKey) {
            this.propKey = Objects.requireNonNull(propKey);
        }

       /**
         * Inserts new value at given Props object by this key and returns newly inserted value proxy.
         * @param props
         * @param value
         * @return 
         */
        public Value<T> insert(Props props, T value) {
            return props.insert(propKey, value);
        }

        /**
         * Removes value proxy from Props object by this key and returns it.
         * @param props
         * @return 
         */
        public Value<T> remove(Props props) {
            return props.remove(propKey);
        }

        /**
         * Removes value from Props object by this key and returns it.
         * @param props
         * @return 
         */
        public T removeGet(Props props) {
            Value<T> remove = props.remove(propKey);
            if (remove == null) {
                return null;
            }
            return remove.get();
        }

        /**
         * Inserts new value at given Props object by this key and returns newly inserted value.
         * @param props
         * @param value
         * @return 
         */
        public T insertGet(Props props, T value) {
            return (T) props.insert(propKey, value).get();
        }

        /**
         * Gets and casts value from given Props object by this key.
         * @param props
         * @return 
         */
        public T get(Props props) {
            return props.getCast(propKey);
        }

        /**
         * Gets proxy value from given Props object by his key.
         * @param props
         * @return 
         */
        public Value<T> getAsValue(Props props) {
            return props.get(propKey);
        }

    }

    /**
     * Insert value and return proxy object to that new value
     *
     * @param <T>
     * @param key
     * @param value
     * @return
     */
    public <T> Value<T> insert(String key, T value) {
        Value<T> val = new Value<>(value);
        this.put(key, val);
        return val;
    }

    /**
     * Retrieves value, unless its key is not present, then it returns default
     * value.
     *
     * @param <T>
     * @param str
     * @param val
     * @return
     */
    public <T> T getOrDefaultCast(String str, T val) {
        Value get = this.get(str);
        if (get == null) {
            return val;
        }
        return (T) get.get();
    }

    /**
     * Retrieves and casts value by given key.
     *
     * @param <T>
     * @param str
     * @return
     */
    public <T> T getCast(String str) {
        Value get = this.get(str);
        if (get == null) {
            return null;
        }
        return (T) get.get();
    }

}
