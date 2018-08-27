/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;
import lt.lb.commons.containers.Tuple;

/**
 *
 * @author Lemmin
 */
public class ReferenceCounter<T> {

    public <T> ReferenceCounter() {
        this(FieldFactory.isJDKImmutable.negate());
    }

    public <T> ReferenceCounter(Predicate<Class> supported) {
        this.supported = supported;
    }

    private Predicate<Class> supported;

    private Map<Class, ConcurrentLinkedDeque<Tuple<Object, T>>> references = new ConcurrentHashMap<>();

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
        if (!references.containsKey(cls)) {
            return false;
        }
        ConcurrentLinkedDeque<Tuple<Object, T>> deque = references.get(cls);

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
        if (references.containsKey(cls)) {
            ConcurrentLinkedDeque<Tuple<Object, T>> deque = references.get(cls);
            for (Tuple<Object, T> tuple : deque) {
                if (tuple.g1 == ob) {
                    return tuple.g2;
                }
            }
        }

        throw new RuntimeException("Reference was not found");
    }

    public boolean registerIfAbsent(Object ob, T value) {
        this.assertSupported(ob);
        if (this.contains(ob)) {
            return false;
        }
        Class cls = ob.getClass();
        ConcurrentLinkedDeque<Tuple<Object, T>> deque;
        if (!references.containsKey(cls)) {
            deque = new ConcurrentLinkedDeque<>();
            references.put(cls, deque);
        } else {
            deque = references.get(cls);
        }
        return deque.add(new Tuple<>(ob, value));

    }
}
