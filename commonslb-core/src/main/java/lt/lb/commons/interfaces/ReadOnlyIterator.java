/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.interfaces;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lt.lb.commons.containers.NumberValue;
import lt.lb.commons.containers.Value;

/**
 *
 * @author laim0nas100
 */
public interface ReadOnlyIterator<T> extends Iterable<T>, Iterator<T> {

    public static <T> ReadOnlyIterator<T> of(Stream<T> stream) {
        return of(stream.spliterator());
    }

    public static <T> ReadOnlyIterator<T> of(Spliterator<T> stream) {

        return new ReadOnlyIterator<T>() {
            Value<T> current = new Value<>();
            LinkedList<T> queue = new LinkedList<>();
            Consumer<T> cons = (item) -> {
                queue.addLast(item);
            };

            @Override
            public boolean hasNext() {
                if (queue.isEmpty()) {
                    stream.tryAdvance(cons);
                }
                return !queue.isEmpty();
            }

            @Override
            public T next() {
                if (queue.isEmpty()) {
                    if (!stream.tryAdvance(cons)) {
                        throw new NoSuchElementException();
                    }
                }
                T next = queue.pollFirst();
                current.set(next);
                return next;
            }

            @Override
            public T getCurrent() {
                return current.get();
            }

        };
    }

    public static <T> ReadOnlyIterator<T> of(Collection<T> col) {
        return of(col.iterator());
    }

    public static <T> ReadOnlyIterator<T> of(T[] array) {

        return new ReadOnlyIterator<T>() {
            NumberValue<Integer> i = NumberValue.of(0);
            Value<T> current = new Value<>();

            @Override
            public boolean hasNext() {
                return i.get() < array.length;
            }

            @Override
            public T next() {
                T val = array[i.getAndIncrement()];
                current.set(val);
                return val;
            }

            @Override
            public T getCurrent() {
                return current.get();
            }
        };
    }

    public static <T> ReadOnlyIterator<T> of(Iterator<T> it) {
        return new ReadOnlyIterator<T>() {
            Value<T> current = new Value<>();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                T val = it.next();
                current.set(val);
                return val;
            }

            @Override
            public T getCurrent() {
                return current.get();
            }
        };
    }

    @Override
    boolean hasNext();

    @Override
    T next();

    default T getNext() {
        return next();
    }

    T getCurrent();

    @Override
    default Iterator<T> iterator() {
        return this;
    }
}
