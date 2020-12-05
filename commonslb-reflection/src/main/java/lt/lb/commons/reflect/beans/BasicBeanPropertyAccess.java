package lt.lb.commons.reflect.beans;

import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * @author laim0nas100
 */
public class BasicBeanPropertyAccess<V, T> implements ValueProxy<T> {

    protected BasicBeanWritePropertyAccess<V, T> write;
    protected BasicBeanReadPropertyAccess<V, T> read;

    @Override
    public T get() {
        return read.get();
    }

    @Override
    public void set(T v) {
        write.accept(v);
    }

    public BasicBeanPropertyAccess(V object, String property) {
        write = new BasicBeanWritePropertyAccess<>(object, property);
        read = new BasicBeanReadPropertyAccess<>(object, property);
    }

}
