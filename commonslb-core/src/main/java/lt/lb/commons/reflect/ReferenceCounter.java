/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import lt.lb.commons.containers.Tuple;

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

    private Cache<Class, Cache<Object, Collection<Tuple<Object, T>>>> references = Caffeine.newBuilder().build();

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

    public boolean contains(Object ob) {
        if (!this.supported(ob)) {
            return false;
        }
        Class cls = ob.getClass();
        Cache<Object, Collection<Tuple<Object, T>>> map = references.getIfPresent(cls);
        if (map == null) {
            return false;
        }

        Collection<Tuple<Object, T>> deque = map.getIfPresent(ob);
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
        Class cls = ob.getClass();
        Collection<Tuple<Object, T>> deque = references.getIfPresent(cls).getIfPresent(ob);
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
        Class cls = ob.getClass();
        Cache<Object, Collection<Tuple<Object, T>>> map;
        Collection<Tuple<Object, T>> deque;

        map = references.get(cls, v -> Caffeine.newBuilder().build());
        deque = map.get(ob, v -> new ConcurrentLinkedQueue<>());
        for (Tuple<Object, T> tuple : deque) {
            if (tuple.g1 == ob) {
                return false;
            }
        }
        return deque.add(new Tuple<>(ob, value));
    }

}
