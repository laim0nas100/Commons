package lt.lb.commons.interfaces;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
     * Clone or return a null
     *
     * @param <A> item type
     * @param obj object to be cloned
     * @return cloned object or null
     */
    public static <A extends CloneSupport<A>> A cloneOrNull(A obj) {
        return obj == null ? null : obj.clone();
    }

    /**
     * Clone using provided function or juts return a null. Useful when objects
     * have non-standard way of cloning themselves.
     *
     * @param <A> item type
     * @param obj object to be cloned
     * @param cloningFunction cloning function
     * @return cloned object or null
     */
    public static <A> A cloneOrNull(A obj, Function<? super A, A> cloningFunction) {
        return obj == null ? null : cloningFunction.apply(obj);
    }

    /**
     * Clones an iterable to a collection.
     *
     * @param <A> item type
     * @param <C> collection type
     * @param iter iterable with items
     * @param collectionSupplier new collection supplier
     * @return
     */
    public static <A extends CloneSupport<A>, C extends Collection<A>> C cloneCollection(Iterable<A> iter, Supplier<? extends C> collectionSupplier) {
        return cloneAll(iter, collectionSupplier, (item, sink) -> sink.add(item), CloneSupport::clone);
    }

    /**
     * Clones a map with very specific cloning details.
     *
     * @param <K> key type
     * @param <A> value type
     * @param <C> collection type
     * @param map map with items
     * @param collectionSupplier new map supplier
     * @param keyCloningFuncion key cloning function
     * @param cloningFuncion value cloning function
     * @return
     */
    public static <K, A, C extends Map<K, A>> C cloneMap(Map<K, A> map, Supplier<? extends C> collectionSupplier, Function<K, K> keyCloningFuncion, Function<A, A> cloningFuncion) {

        C cloned = collectionSupplier.get();

        map.entrySet().forEach(entry -> {
            K clonedKey = CloneSupport.cloneOrNull(entry.getKey(), keyCloningFuncion);
            A clonedValue = CloneSupport.cloneOrNull(entry.getValue(), cloningFuncion);
            cloned.put(clonedKey, clonedValue);
        });

        return cloned;
    }

    /**
     * Clones a map with immutable keys.
     *
     * @param <K> key type
     * @param <A> value type
     * @param <C> collection type
     * @param map map with items
     * @param collectionSupplier new map supplier
     * @param cloningFuncion value cloning function
     * @return
     */
    public static <K, A, C extends Map<K, A>> C cloneMapImmutableKeys(Map<K, A> map, Supplier<? extends C> collectionSupplier, Function<A, A> cloningFuncion) {
        return cloneMap(map, collectionSupplier, k -> k, cloningFuncion);
    }

    /**
     * Clones a map with immutable keys and CloneSupport type values.
     *
     * @param <K> key type
     * @param <A> value type
     * @param <C> collection type
     * @param map map with items
     * @param collectionSupplier new map supplier
     * @return
     */
    public static <K, A extends CloneSupport<A>, C extends Map<K, A>> C cloneMapImmutableSupported(Map<K, A> map, Supplier<? extends C> collectionSupplier) {
        return cloneMap(map, collectionSupplier, k -> k, CloneSupport::clone);
    }
    
    /**
     * Clones a map with immutable keys and CloneSupport type values and keys.
     *
     * @param <K> key type
     * @param <A> value type
     * @param <C> collection type
     * @param map map with items
     * @param collectionSupplier new map supplier
     * @return
     */
    public static <K extends CloneSupport<K>, A extends CloneSupport<A>, C extends Map<K, A>> C cloneMapSupported(Map<K, A> map, Supplier<? extends C> collectionSupplier) {
        return cloneMap(map, collectionSupplier, CloneSupport::clone, CloneSupport::clone);
    }

    /**
     * Clones an array.
     *
     * @param <A> item type
     * @param iter array with items
     * @param arraySupplier array maker
     * @return
     */
    public static <A extends CloneSupport<A>> A[] cloneArray(A[] iter, Supplier<A[]> arraySupplier) {
        IntegerValue i = new IntegerValue(0);
        return cloneAll(ReadOnlyIterator.of(iter), arraySupplier, (item, sink) -> sink[i.getAndIncrement()] = item, CloneSupport::clone);

    }

    /**
     * Clones an iterable to specified sink with specified methodology.
     *
     * @param <A> item type
     * @param <C> new collection
     * @param iter iterable with items
     * @param sinkSupplier new collection supplier
     * @param consumer how to add cloned item from iterable to new collection
     * @param cloningFuncion how to clone item
     * @return
     */
    public static <A extends CloneSupport<A>, C> C cloneAll(Iterable<A> iter, Supplier<C> sinkSupplier, BiConsumer<A, C> consumer, Function<A, A> cloningFuncion) {
        C get = sinkSupplier.get();
        iter.forEach(item -> {
            consumer.accept(CloneSupport.cloneOrNull(item, cloningFuncion), get);
        });

        return get;
    }
}
