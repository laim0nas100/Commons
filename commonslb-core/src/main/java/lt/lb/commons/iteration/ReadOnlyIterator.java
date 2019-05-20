package lt.lb.commons.iteration;

import lt.lb.commons.iteration.impl.ArrayROI;
import lt.lb.commons.iteration.impl.StreamROI;
import lt.lb.commons.iteration.impl.IteratorROI;
import lt.lb.commons.iteration.impl.CompositeROI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lt.lb.commons.containers.collections.ForwardingStream;

/**
 *
 * Unified Iterator with index
 *
 * @author laim0nas100
 */
public interface ReadOnlyIterator<T> extends Iterable<T>, Iterator<T>, AutoCloseable {
    
    public static <T> ReadOnlyIterator<T> of(Stream<T> stream) {
        return new StreamROI<>(stream);
    }
    
    public static <T> ReadOnlyIterator<T> of(Collection<T> col) {
        return of(col.iterator());
    }
    
    public static <T> ReadOnlyBidirectionalIterator<T> of(T... array) {
        return new ArrayROI<>(array);
    }
    
    public static <T> ReadOnlyIterator<T> of(Iterator<T> it) {
        return new IteratorROI<>(it);
    }
    
    public static <T> ReadOnlyIterator<T> composite(ReadOnlyIterator<T>... iters) {
        return new CompositeROI<>(of(iters));
    }
    
    public static <T> ReadOnlyIterator<T> composite(Collection<ReadOnlyIterator<T>> iters) {
        return new CompositeROI<>(of(iters));
    }

    /**
     * Puts remaining elements to ArrayList.
     *
     * @return
     */
    public default ArrayList<T> toArrayList() {
        ArrayList<T> list = new ArrayList();
        for (T item : this) {
            list.add(item);
        }
        return list;
    }
    
    public default LinkedList<T> toLinkedList() {
        LinkedList<T> list = new LinkedList();
        for (T item : this) {
            list.add(item);
        }
        return list;
    }
     
    /**
     * Creates a Stream of remaining elements.
     *
     * @return
     */
    public default Stream<T> toStream() {
        ReadOnlyIterator<T> iter = this;
        Stream<T> stream = StreamSupport.stream(iter.spliterator(), false);
        return new ForwardingStream<T>(){
            @Override
            public void close() {
                iter.close();
            }

            @Override
            protected Stream<T> delegate() {
                return stream;
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
    
    Integer getCurrentIndex();
    
    T getCurrent();
    
    @Override
    default Iterator<T> iterator() {
        return this;
    }
    
    @Override
    default void close() {
    }
}

