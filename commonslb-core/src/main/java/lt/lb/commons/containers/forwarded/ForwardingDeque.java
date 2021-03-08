package lt.lb.commons.containers.forwarded;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingDeque<T> extends Deque<T>, ForwardingQueue<T> {

    @Override
    public default void addFirst(T e) {
        delegate().addFirst(e);
    }

    @Override
    public default void addLast(T e) {
        delegate().addLast(e);
    }

    @Override
    public default boolean offerFirst(T e) {
        return delegate().offerFirst(e);
    }

    @Override
    public default boolean offerLast(T e) {
        return delegate().offerLast(e);
    }

    @Override
    public default T removeFirst() {
        return delegate().removeFirst();
    }

    @Override
    public default T removeLast() {
        return delegate().removeLast();
    }

    @Override
    public default T pollFirst() {
        return delegate().pollFirst();
    }

    @Override
    public default T pollLast() {
        return delegate().pollLast();
    }

    @Override
    public default T getFirst() {
        return delegate().getFirst();
    }

    @Override
    public default T getLast() {
        return delegate().getLast();
    }

    @Override
    public default T peekFirst() {
        return delegate().peekFirst();
    }

    @Override
    public default T peekLast() {
        return delegate().peekLast();
    }

    @Override
    public default boolean removeFirstOccurrence(Object o) {
        return delegate().removeFirstOccurrence(o);
    }

    @Override
    public default boolean removeLastOccurrence(Object o) {
        return delegate().removeLastOccurrence(o);
    }

    @Override
    public default boolean add(T e) {
        return delegate().add(e);
    }

    @Override
    public default boolean offer(T e) {
        return delegate().offer(e);
    }

    @Override
    public default T remove() {
        return delegate().remove();
    }

    @Override
    public default T poll() {
        return delegate().poll();
    }

    @Override
    public default T element() {
        return delegate().element();
    }

    @Override
    public default T peek() {
        return delegate().peek();
    }

    @Override
    public default boolean addAll(Collection<? extends T> c) {
        return delegate().addAll(c);
    }

    @Override
    public default void push(T e) {
        delegate().push(e);
    }

    @Override
    public default T pop() {
        return delegate().pop();
    }

    @Override
    public default boolean remove(Object o) {
        return delegate().remove(o);
    }

    @Override
    public default boolean contains(Object o) {
        return delegate().contains(o);
    }

    @Override
    public default int size() {
        return delegate().size();
    }

    @Override
    public default Iterator<T> iterator() {
        return delegate().iterator();
    }

    @Override
    public default Iterator<T> descendingIterator() {
        return delegate().descendingIterator();
    }

    @Override
    public Deque<T> delegate();

}
