package lt.lb.commons.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lt.lb.commons.containers.NumberValue;
import lt.lb.commons.containers.Value;

/**
 *
 * Unified Iterator with index
 * @author laim0nas100
 */
public interface ReadOnlyIterator<T> extends Iterable<T>, Iterator<T> {

    public static <T> ReadOnlyIterator<T> of(Stream<T> stream) {
        return of(stream.spliterator());
    }

    public static <T> ReadOnlyIterator<T> of(Spliterator<T> stream) {

        return new ReadOnlyIterator<T>() {
            NumberValue<Integer> index = NumberValue.of(-1);
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
                index.incrementAndGet();
                current.set(next);
                return next;
            }

            @Override
            public T getCurrent() {
                return current.get();
            }

            @Override
            public Integer getCurrentIndex() {
                return index.get();
            }

        };
    }

    public static <T> ReadOnlyIterator<T> of(Collection<T> col) {
        return of(col.iterator());
    }

    public static <T> ReadOnlyBidirectionalIterator<T> of(T[] array) {

        return new ReadOnlyBidirectionalIterator<T>() {
            NumberValue<Integer> i = NumberValue.of(-1);
            Value<T> current = new Value<>();

            @Override
            public boolean hasNext() {
                return i.get() < array.length - 1;
            }

            @Override
            public T next() {
                T val = array[i.incrementAndGet()];
                current.set(val);
                return val;
            }

            @Override
            public T getCurrent() {
                return current.get();
            }

            @Override
            public Integer getCurrentIndex() {
                return i.get();
            }

            @Override
            public boolean hasPrevious() {
                return i.get() > 0;
            }

            @Override
            public T previous() {
                T val = array[i.decrementAndGet()];
                current.set(val);
                return val;
            }
        };
    }

    public static <T> ReadOnlyIterator<T> of(Iterator<T> it) {
        return new ReadOnlyIterator<T>() {
            Value<T> current = new Value<>();
            NumberValue<Integer> i = NumberValue.of(-1);
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                T val = it.next();
                current.set(val);
                i.incrementAndGet();
                return val;
            }

            @Override
            public T getCurrent() {
                return current.get();
            }

            @Override
            public Integer getCurrentIndex() {
                return i.get();
            }
        };
    }

    /**
     * Puts remaining elements to ArrayList.
     *
     * @param <T>
     * @param iter
     * @return
     */
    public static <T> ArrayList<T> toArrayList(ReadOnlyIterator<T> iter) {
        ArrayList<T> list = new ArrayList();
        for (T item : iter) {
            list.add(item);
        }
        return list;
    }
    
    
    public static <T> LinkedList<T> toLinkedList(ReadOnlyIterator<T> iter){
        LinkedList<T> list = new LinkedList();
        for (T item : iter) {
            list.add(item);
        }
        return list;
    }

    /**
     * Creates a Stream of remaining elements.
     *
     * @param <T>
     * @param iter
     * @return
     */
    public static <T> Stream<T> toStream(ReadOnlyIterator<T> iter) {
        return StreamSupport.stream(iter.spliterator(), false);
    }

    @Override
    boolean hasNext();

    @Override
    T next();

    default T getNext() {
        return next();
    }

    Integer getCurrentIndex();

    T getCurrent();

    @Override
    default Iterator<T> iterator() {
        return this;
    }
}
