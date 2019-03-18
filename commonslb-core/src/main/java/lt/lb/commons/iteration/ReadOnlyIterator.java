package lt.lb.commons.iteration;

import lt.lb.commons.iteration.impl.ArrayROI;
import lt.lb.commons.iteration.impl.StreamROI;
import lt.lb.commons.iteration.impl.IteratorROI;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lt.lb.commons.ArrayOp;

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

    public static <T> LinkedList<T> toLinkedList(ReadOnlyIterator<T> iter) {
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
        Stream<T> stream = StreamSupport.stream(iter.spliterator(), false);

        return (Stream<T>) Proxy.newProxyInstance(Stream.class.getClassLoader(), ArrayOp.asArray(Stream.class), (Object proxy, Method method, Object[] args) -> {
            if (method.getName().equals("close")) {
                iter.close();
                return null;
            } else {
                return method.invoke(stream, args);
            }
        });
        

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
    default void close(){};
}
