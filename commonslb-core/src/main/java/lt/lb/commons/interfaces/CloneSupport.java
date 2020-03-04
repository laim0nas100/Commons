package lt.lb.commons.interfaces;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 * Interface for cloning, explicitly declaring clone method publicly.
 *
 * @author laim0nas100
 */
public interface CloneSupport<T> extends Cloneable {

    public T clone();

    /**
     * Clones an iterable to a collection.
     * @param <A>
     * @param <C>
     * @param iter
     * @param collectionSupplier
     * @return 
     */
    public static <A extends CloneSupport<A>, C extends Collection<A>> C cloneColletion(Iterable<A> iter, Supplier<? extends C> collectionSupplier) {
        return cloneAll(iter, collectionSupplier, (item, sink) -> sink.add(item));
    }

    /**
     * Clones an array.
     * @param <A>
     * @param iter
     * @param arraySupplier
     * @return 
     */
    public static <A extends CloneSupport<A>> A[] cloneArray(A[] iter, Supplier<A[]> arraySupplier) {
        IntegerValue i = new IntegerValue(0);
        return cloneAll(ReadOnlyIterator.of(iter), arraySupplier, (item, sink) -> sink[i.getAndIncrement()] = item);

    }

    /**
     * Clones an iterable to specified sink with specified methodology.
     * @param <A>
     * @param <C>
     * @param iter
     * @param sinkSupplier
     * @param consumer
     * @return 
     */
    public static <A extends CloneSupport<A>, C> C cloneAll(Iterable<A> iter, Supplier<C> sinkSupplier, BiConsumer<A, C> consumer) {
        C get = sinkSupplier.get();
        iter.forEach(item -> {
            consumer.accept(item == null ? null : item.clone(), get);
        });

        return get;
    }
}
