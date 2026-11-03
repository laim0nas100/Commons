package com.github.laim0nas100.commonslb.containers.collections;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import com.github.laim0nas100.commonslb.containers.values.ValueProxy;
import java.util.HashMap;
import lt.lb.prebuiltcollections.DelegatingMap;

/**
 * Map key-value indirection interface with typed parameters..
 *
 * @author laim0nas100
 */
public interface Props<K> extends DelegatingMap<K, Object>, Serializable {

    public static <K> Props<K> ofMap(Map<K, Object> map) {
        return new SimpleProps<>(map);
    }

    public static <K> Props<K> ofHashMap() {
        return ofMap(new HashMap<>());
    }
    
    public static <K> Props<K> ofArray(){
        return ofMap(new ArrayLinearMap<>());
    }

    public static class SimpleProps<K> implements Props<K> {

        protected final Map<K, Object> map;

        public SimpleProps(Map<K, Object> map) {
            this.map = Objects.requireNonNull(map);
        }

        @Override
        public Map<K, Object> delegate() {
            return map;
        }
    }

    public static <K, V, T> ValueProxy<T> asValueProxy(Map<K, V> props, K key) {
        return new ValueProxy<T>() {
            @Override
            public T get() {
                return (T) props.get(key);
            }

            @Override
            public void set(T v) {
                props.put(key, (V) v);
            }

            @Override
            public boolean isEmpty() {
                return !props.containsKey(key);
            }
        };
    }

    public default <T> Props<K> with(K key, T val) {
        this.put(key, val);
        return this;
    }

    public static class SimplePropGet<K, T> implements PropGet<K, T> {

        public final K propKey;

        public SimplePropGet(K propKey) {
            this.propKey = Objects.requireNonNull(propKey);
        }

        @Override
        public K getKey() {
            return propKey;
        }
    }

    /**
     * Explicit typing and property key information. Designed to be used with
     * Props.
     *
     * @param <T>
     */
    public interface PropGet<K, T> {

        /**
         * Construct PropGet of specified key and explicit type.
         *
         * @param K key type
         * @param Type value type
         * @param prop
         * @return
         */
        public static <K, Type> PropGet<K, Type> of(K prop) {
            return new SimplePropGet<>(prop);
        }

        /**
         * Gets if value is present in given Props object by his key.
         *
         * @param props
         * @return
         */
        public default <V> boolean containsKey(Map<K, V> props) {
            return props.containsKey(getKey());
        }

        public K getKey();

        /**
         * Gets proxy value from given Props object by his key.
         *
         * @param props
         * @return
         */
        public default <V> ValueProxy<T> getAsValue(Map<K, V> props) {
            return asValueProxy(props, getKey());
        }

        /**
         * Sets the value if not null, removes otherwise
         *
         * @param props
         * @param value
         * @return
         */
        public default <V> ValueProxy<T> setOrRemoveNull(Map<K, V> props, T value) {
            if (value == null) {
                return remove(props);
            } else {
                return insert(props, value);
            }
        }

        /**
         * Inserts new value at given Props object by this key and returns newly
         * inserted value proxy.
         *
         * @param props
         * @param value
         * @return
         */
        public default <V> ValueProxy<T> insert(Map<K, V> props, T value) {
            K key = getKey();
            props.put(key, (V) value);
            return asValueProxy(props, key);
        }

        /**
         * Removes value from Props object by this key and returns it as proxy.
         *
         * @param props
         * @return
         */
        public default <V> ValueProxy<T> remove(Map<K, V> props) {
            props.remove(getKey());
            return getAsValue(props);
        }

        /**
         * Removes value from Props object by this key and returns it.
         *
         * @param props
         * @return
         */
        public default <V> T removeGet(Map<K, V> props) {
            Object remove = props.remove(getKey());
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
        public default <V> T insertGet(Map<K, V> props, T value) {
            props.put(getKey(), (V) value);
            return value;
        }

        /**
         * Gets and casts value from given Props object by this key.
         *
         * @param props
         * @return
         */
        public default <V> T get(Map<K, V> props) {
            return (T) props.get(getKey());
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
    public default <T> ValueProxy<T> insert(K key, T value) {
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
    public default <V> V getOrDefaultCast(K key, V val) {
        Object get = this.get(key);
        if (get == null) {
            return val;
        }
        return (V) get;
    }

    /**
     * Retrieves and casts value by given key.
     *
     * @param <T>
     * @param key
     * @return
     */
    public default <V> V getCast(K key) {
        Object get = this.get(key);
        if (get == null) {
            return null;
        }
        return (V) get;
    }

    /**
     * Retrieves value and returns it as a String.
     *
     * @param key
     * @return
     */
    public default String getString(K key) {
        Object get = this.get(key);
        if (get == null) {
            return null;
        }
        return String.valueOf(get);
    }

}
