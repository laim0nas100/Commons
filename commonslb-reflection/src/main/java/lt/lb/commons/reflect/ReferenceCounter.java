/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import lt.lb.commons.containers.tuples.Tuple;

/**
 *
 * @author laim0nas100
 */
public class ReferenceCounter<T> {

    public <T> ReferenceCounter() {
        this(FieldFactory.isJVMImmutable.negate());
    }

    public <T> ReferenceCounter(Predicate<Class> supported) {
        this.supported = supported;
    }

    private Predicate<Class> supported;

    private Cache<Tuple<Class, Object>, Collection<Tuple<Object, T>>> references = newCache();

    protected <K, V> Cache<K, V> newCache() {
        return Caffeine.newBuilder().build();
    }

    protected Collection newCollection() {
        return new ArrayList(1);
    }

    public boolean supported(Object ob) {
        if (ob == null) {
            return false;
        }
        return supported.test(ob.getClass());
    }

    private void assertSupported(Object ob) {
        if (ob == null) {
            throw new IllegalArgumentException("null values are not supported as reference");
        }
        if (!supported(ob.getClass())) {
            throw new IllegalArgumentException(ob.getClass().getName() + " is not supported as reference");
        }

    }

    private Tuple<Class, Object> wrap(Object ob) {
        return new Tuple<>(ob.getClass(), ob);
    }

    public boolean contains(Object ob) {
        if (!this.supported(ob)) {
            return false;
        }
        Collection<Tuple<Object, T>> deque = references.getIfPresent(wrap(ob));

        if (deque == null) {
            return false;
        }

        for (Tuple<Object, T> tuple : deque) {
            if (tuple.g1 == ob) {
                return true;
            }
        }
        return false;

    }

    public T get(Object ob) {
        this.assertSupported(ob);
        Collection<Tuple<Object, T>> deque = references.getIfPresent(wrap(ob));
        for (Tuple<Object, T> tuple : deque) {
            if (tuple.g1 == ob) {
                return tuple.g2;
            }
        }

        throw new IllegalArgumentException("Reference was not found");
    }

    public boolean registerIfAbsent(Object ob, T value) {
        this.assertSupported(ob);
        return this.fastRegisterIfAbsent(ob, value);
    }

    public boolean fastRegisterIfAbsent(Object ob, T value) {

        Collection<Tuple<Object, T>> deque = references.get(wrap(ob), v -> newCollection());
        for (Tuple<Object, T> tuple : deque) {
            if (tuple.g1 == ob) {
                return false;
            }
        }
        return deque.add(new Tuple<>(ob, value));
    }

}
