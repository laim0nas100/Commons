package lt.lb.commons.iteration;

import lt.lb.commons.iteration.impl.ArrayROI;
import lt.lb.commons.iteration.impl.StreamROI;
import lt.lb.commons.iteration.impl.IteratorROI;
import lt.lb.commons.iteration.impl.CompositeROI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lt.lb.commons.containers.ForwardingStream;
import lt.lb.commons.iteration.impl.EmptyROI;
import lt.lb.commons.iteration.impl.ListROI;

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
        return new IteratorROI<>(col.iterator());
    }
    
    public static <T> ReadOnlyBidirectionalIterator<T> of(List<T> col) {
        return new ListROI<>(col.listIterator());
    }

    public static <T> ReadOnlyBidirectionalIterator<T> of(T... array) {
        if(array.length == 0){
            return new EmptyROI<>();
        }
        return new ArrayROI<>(array);
    }

    public static <T> ReadOnlyIterator<T> of(Iterator<T> it) {
        return new IteratorROI<>(it);
    }

    public static <T> ReadOnlyIterator<T> composite(ReadOnlyIterator<T>... iters) {
        if(iters.length == 0){
            return new EmptyROI<>();
        }
        return new CompositeROI<>(of(iters));
    }

    public static <T> ReadOnlyIterator<T> composite(Collection<ReadOnlyIterator<T>> iters) {
        if(iters.isEmpty()){
            return new EmptyROI<>();
        }
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
        return new ForwardingStream<T>() {
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

    default <R> ReadOnlyIterator<R> map(Function<? super T, ? extends R> mapper) {
        ReadOnlyIterator<T> me = this;
        return new ReadOnlyIterator<R>() {
            @Override
            public R getCurrent() {
                return mapper.apply(me.getCurrent());
            }

            @Override
            public Integer getCurrentIndex() {
                return me.getCurrentIndex();
            }

            @Override
            public boolean hasNext() {
                return me.hasNext();
            }

            @Override
            public R next() {
                return mapper.apply(me.next());
            }
        };
    }
    
    default ReadOnlyIterator<T> withCloseOperation(Runnable run){
        ReadOnlyIterator<T> me = this;
        return new ReadOnlyIterator<T>() {
            @Override
            public void close() {
                me.close();
                run.run();
            }
            @Override
            public T getCurrent() {
                return me.getCurrent();
            }

            @Override
            public Integer getCurrentIndex() {
                return me.getCurrentIndex();
            }

            @Override
            public boolean hasNext() {
                return me.hasNext();
            }

            @Override
            public T next() {
                return me.next();
            }
            
            
        };
    }
}
