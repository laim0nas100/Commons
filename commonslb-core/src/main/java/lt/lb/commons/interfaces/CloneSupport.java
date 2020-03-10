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
 * @param <T>
 */
@FunctionalInterface
public interface CloneSupport<T> extends Cloneable {

    public T clone();

    public static <T,C extends Collection<T>> C cloneShallowCollection(C iter, Supplier<? extends C> collectionSupplier){
        if (iter == null) {
            return null;
        }
        C get = collectionSupplier.get();
        get.addAll(iter);
        return get;
    }
    
    /**
     * Clone or return a null
     *
     * @param <A> item type
     * @param <D> type that produces a cloned item
     * @param obj object to be cloned
     * @return cloned object or null
     */
    public static <A, D extends CloneSupport<A>> A cloneOrNull(D obj) {
        return obj == null ? null : obj.clone();
    }

    /**
     * Analogous to cloneOrNull. Also performs a cast. Implies that the cloning
     * function returns correct type. No explicit compile-time type checking.
     *
     * @param <A> item type
     * @param obj object to be cloned
     * @param cloningFunction cloning function
     * @return cloned object or null
     */
    public static <A extends Cloneable> A cloneOrNullCast(A obj, Function<? super A, Object> cloningFunction) {
        return obj == null ? null : (A) cloningFunction.apply(obj);
    }

    /**
     * Clone using provided function or juts return a null. Useful when objects
     * have non-standard way of cloning themselves.
     *
     * @param <D> parameter item type
     * @param <A> result item type
     * @param obj object to be cloned
     * @param cloningFunction cloning function
     * @return cloned object or null
     */
    public static <D, A> A cloneOrNull(D obj, Function<? super D, A> cloningFunction) {
        return obj == null ? null : cloningFunction.apply(obj);
    }

    /**
     * Clones an iterable to a collection.
     *
     * @param <A> item type
     * @param <D> item that produces a clone type
     * @param <C> collection type
     * @param iter iterable with items
     * @param collectionSupplier new collection supplier
     * @return
     */
    public static <A, D extends CloneSupport<A>, C extends Collection<A>> C cloneCollection(Iterable<D> iter, Supplier<? extends C> collectionSupplier) {
        if (iter == null) {
            return null;
        }
        return cloneAll(iter, collectionSupplier, (item, sink) -> sink.add(item), CloneSupport::clone);
    }

    /**
     * Clones a map with very specific cloning details.
     *
     * @param <K> parameter key type
     * @param <KK> result key type
     * @param <A> parameter value type
     * @param <AA> parameter value type
     * @param <C> result map type
     * @param <CC> parameter map type
     * @param map map with items
     * @param collectionSupplier new map supplier
     * @param keyCloningFuncion key cloning function
     * @param cloningFuncion value cloning function
     * @return
     */
    public static <K, KK, A, AA, C extends Map<K, A>, CC extends Map<KK, AA>> C cloneMap(CC map, Supplier<? extends C> collectionSupplier, Function<KK, K> keyCloningFuncion, Function<AA, A> cloningFuncion) {
        if (map == null) {
            return null;
        }

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
        if (map == null) {
            return null;
        }
        return cloneMap(map, collectionSupplier, k -> k, cloningFuncion);
    }

    /**
     * Clones a map with immutable keys and CloneSupport type values.
     *
     * @param <K> key type
     * @param <A> result value type
     * @param <AA> value type that produces a clone
     * @param <C> collection type
     * @param map map with items
     * @param collectionSupplier new map supplier
     * @return
     */
    public static <K, A, AA extends CloneSupport<A>, C extends Map<K, A>> C cloneMapImmutableSupported(Map<K, AA> map, Supplier<? extends C> collectionSupplier) {
        if (map == null) {
            return null;
        }
        return cloneMap(map, collectionSupplier, k -> k, CloneSupport::clone);
    }

    /**
     * Clones a map with immutable keys and CloneSupport type values and keys.
     *
     * @param <K> key type
     * @param <KK> key type that produces a clone
     * @param <A> result value type
     * @param <AA> value type that produces a clone
     * @param <C> collection type
     * @param map map with items
     * @param collectionSupplier new map supplier
     * @return
     */
    public static <K, KK extends CloneSupport<K>, A, AA extends CloneSupport<A>, C extends Map<K, A>> C cloneMapSupported(Map<KK, AA> map, Supplier<? extends C> collectionSupplier) {
        if (map == null) {
            return null;
        }
        return cloneMap(map, collectionSupplier, CloneSupport::clone, CloneSupport::clone);
    }

    /**
     * Clones an array.
     *
     * @param <A> item that supports clone type
     * @param <D> expected clone result type
     * @param iter array with items
     * @param arraySupplier array maker
     * @return
     */
    public static <A, D extends CloneSupport<A>> A[] cloneArray(D[] iter, Supplier<A[]> arraySupplier) {
        if (iter == null) {
            return null;
        }
        IntegerValue i = new IntegerValue(0);
        return cloneAll(ReadOnlyIterator.of(iter), arraySupplier, (item, sink) -> sink[i.getAndIncrement()] = item, CloneSupport::clone);

    }

    /**
     * Clones an iterable to specified sink with specified methodology.
     *
     * @param <A> item that supports clone type
     * @param <D> expected clone result type
     * @param <C> new collection
     * @param iter iterable with items
     * @param sinkSupplier new collection supplier
     * @param consumer how to add cloned item from iterable to new collection
     * @param cloningFuncion how to clone item
     * @return
     */
    public static <A, D extends CloneSupport<A>, C> C cloneAll(Iterable<D> iter, Supplier<C> sinkSupplier, BiConsumer<A, C> consumer, Function<D, A> cloningFuncion) {
        if (iter == null) {
            return null;
        }
        C get = sinkSupplier.get();
        iter.forEach(item -> {
            consumer.accept(CloneSupport.cloneOrNull(item, cloningFuncion), get);
        });

        return get;
    }
}
