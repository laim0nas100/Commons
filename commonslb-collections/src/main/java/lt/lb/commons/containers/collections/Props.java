package lt.lb.commons.containers.collections;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.prebuiltcollections.DelegatingMap;

/**
 * Property class that stores any value (including null).
 *
 * @author laim0nas100
 */
public class Props<K> implements DelegatingMap<K, Object>, Serializable {

    protected Map<K, Object> map;

    public Props(Map<K, Object> delegated) {
        this.map = delegated;
    }

    public Props() {
        this(new HashMap<>());
    }

    public Map<K, Object> getMap() {
        return map;
    }

    public static <K, T> ValueProxy<T> asValueProxy(Props<K> props, K key) {
        return new ValueProxy<T>() {
            @Override
            public T get() {
                return props.getCast(key);
            }

            @Override
            public void set(T v) {
                props.put(key, v);
            }

            @Override
            public boolean isEmpty() {
                return !props.containsKey(key);
            }
        };
    }

    public <T> Props<K> with(K key, T val) {
        this.put(key, val);
        return this;
    }

    @Override
    public Map<K, Object> delegate() {
        return map;
    }

    /**
     * Explicit typing and property key information. Designed to be used with
     * Props.
     *
     * @param <T>
     */
    public static class PropGet<K, T> {

        /**
         * Construct PropGet of specified key and explicit type.
         *
         * @param K key type
         * @param Type value type
         * @param prop
         * @return
         */
        public static <K, Type> PropGet<K, Type> of(K prop) {
            return new PropGet<>(prop);
        }

        public final K propKey;

        public PropGet(K propKey) {
            this.propKey = Objects.requireNonNull(propKey);
        }

        /**
         * Gets if value is present in given Props object by his key.
         *
         * @param props
         * @return
         */
        public boolean containsKey(Props<K> props) {
            return props.containsKey(propKey);
        }

        /**
         * Gets proxy value from given Props object by his key.
         *
         * @param props
         * @return
         */
        public ValueProxy<T> getAsValue(Props<K> props) {
            return asValueProxy(props, propKey);
        }

        /**
         * Inserts new value at given Props object by this key and returns newly
         * inserted value proxy.
         *
         * @param props
         * @param value
         * @return
         */
        public ValueProxy<T> insert(Props<K> props, T value) {
            return props.insert(propKey, value);
        }

        /**
         * Removes value from Props object by this key and returns it as proxy.
         *
         * @param props
         * @return
         */
        public ValueProxy<T> remove(Props<K> props) {
            props.remove(propKey);
            return getAsValue(props);
        }

        /**
         * Removes value from Props object by this key and returns it.
         *
         * @param props
         * @return
         */
        public T removeGet(Props<K> props) {
            Object remove = props.remove(propKey);
            if (remove == null) {
                return null;
            }
            return (T) remove;
        }

        /**
         * Inserts new value at given Props object by this key and returns newly
         * inserted value.
         *
         * @param props
         * @param value
         * @return
         */
        public T insertGet(Props<K> props, T value) {
            return (T) props.insert(propKey, value).get();
        }

        /**
         * Gets and casts value from given Props object by this key.
         *
         * @param props
         * @return
         */
        public T get(Props<K> props) {
            return props.getCast(propKey);
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
    public <T> ValueProxy<T> insert(K key, T value) {
        this.put(key, value);
        return asValueProxy(this, key);
    }

    /**
     * Retrieves value, unless its key is not present, then it returns default
     * value.
     *
     * @param <T>
     * @param key
     * @param val
     * @return
     */
    public <T> T getOrDefaultCast(K key, T val) {
        Object get = this.get(key);
        if (get == null) {
            return val;
        }
        return (T) get;
    }

    /**
     * Retrieves and casts value by given key.
     *
     * @param <T>
     * @param key
     * @return
     */
    public <T> T getCast(K key) {
        Object get = this.get(key);
        if (get == null) {
            return null;
        }
        return (T) get;
    }

    /**
     * Retrieves value and returns it as a String.
     *
     * @param key
     * @return
     */
    public String getString(K key) {
        Object get = this.get(key);
        if (get == null) {
            return null;
        }
        return String.valueOf(get);
    }

}
