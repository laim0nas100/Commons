package lt.lb.commons.containers.forwarded;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingIterable<T> extends Iterable<T> {

    public Iterable<T> delegate();

    @Override
    public default Iterator<T> iterator() {
        return delegate().iterator();
    }

    @Override
    public default void forEach(Consumer<? super T> action) {
        delegate().forEach(action);
    }

    @Override
    public default Spliterator<T> spliterator() {
        return delegate().spliterator();
    }
    
    

}
