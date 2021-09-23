package lt.lb.commons.containers.values;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.containers.forwarded.ForwardingMap;

/**
 * Property class that stores any value (including null).
 *
 * @author laim0nas100
 */
public class Props implements ForwardingMap<String, Object> {

    protected Map<String, Object> map;

    public Props(Map<String, Object> delegated) {
        this.map = delegated;
    }

    public Props() {
        this(new HashMap<>());
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public static <T> ValueProxy<T> asValueProxy(Props props, String key) {
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

    public <T> Props with(String key, T val) {
        this.put(key, val);
        return this;
    }

    @Override
    public Map<String, Object> delegate() {
        return map;
    }

    /**
     * Explicit typing and property key information. Designed to be used with
     * Props.
     *
     * @param <T>
     */
    public static class PropGet<T> {

        /**
         * Construct PropGet of specified key and explicit type.
         *
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
         * Gets if value is present in given Props object by his key.
         *
         * @param props
         * @return
         */
        public boolean containsKey(Props props) {
            return props.containsKey(propKey);
        }

        /**
         * Gets proxy value from given Props object by his key.
         *
         * @param props
         * @return
         */
        public ValueProxy<T> getAsValue(Props props) {
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
        public ValueProxy<T> insert(Props props, T value) {
            return props.insert(propKey, value);
        }

        /**
         * Removes value from Props object by this key and returns it as proxy.
         *
         * @param props
         * @return
         */
        public ValueProxy<T> remove(Props props) {
            props.remove(propKey);
            return getAsValue(props);
        }

        /**
         * Removes value from Props object by this key and returns it.
         *
         * @param props
         * @return
         */
        public T removeGet(Props props) {
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
        public T insertGet(Props props, T value) {
            return (T) props.insert(propKey, value).get();
        }

        /**
         * Gets and casts value from given Props object by this key.
         *
         * @param props
         * @return
         */
        public T get(Props props) {
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
    public <T> ValueProxy<T> insert(String key, T value) {
        this.put(key, value);
        return asValueProxy(this, key);
    }

    /**
     * Inserts value with generated UUID and returns key specification.
     *
     * @param <T>
     * @param value
     * @return
     */
    public <T> PropGet<T> insertAny(T value) {
        String key = "PropGet-" + idGen.getAndIncrement();
        insert(key, value);
        return new PropGet<>(key);

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
        Object get = this.get(str);
        if (get == null) {
            return val;
        }
        return (T) get;
    }

    /**
     * Retrieves and casts value by given key.
     *
     * @param <T>
     * @param str
     * @return
     */
    public <T> T getCast(String str) {
        Object get = this.get(str);
        if (get == null) {
            return null;
        }
        return (T) get;
    }

    /**
     * Retrieves value and returns it as a String.
     *
     * @param str
     * @return
     */
    public String getString(String str) {
        Object get = this.get(str);
        if (get == null) {
            return null;
        }
        return String.valueOf(get);
    }

    private static final AtomicLong idGen = new AtomicLong(0L);

}
