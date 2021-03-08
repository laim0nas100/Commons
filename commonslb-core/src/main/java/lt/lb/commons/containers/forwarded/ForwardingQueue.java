package lt.lb.commons.containers.forwarded;

import java.util.Queue;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingQueue<T> extends Queue<T>, ForwardingCollection<T> {

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
    public Queue<T> delegate();

}
