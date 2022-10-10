package lt.lb.commons.clone;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.Ins;

/**
 *
 * @author laim0nas100
 */
public interface Cloner {

    public static final Cloner DEFAULT = new Cloner() {
    };

    /**
     * Cloner with default implementation and no iternal state
     *
     * @return
     */
    public static Cloner get() {
        return DEFAULT;
    }

    /**
     * Cloner that respect and returns object if the same reference has been
     * found ignoring only JVM immutables
     *
     * @return
     */
    public static RefCountingCloner refCountingMutables() {
        return new RefCountingCloner() {
            @Override
            public boolean refCheckPossible(Object obj) {
                if (obj == null) {
                    return false;
                }
                return !Ins.isJVMImmutable(obj.getClass());
            }
        };
    }

    /**
     * Cloner that respect and returns object if the same reference has been
     * found only of given type.
     *
     * @param classes
     * @return
     */
    public static RefCountingCloner refCountingOfTypes(final Class... classes) {
        if (classes.length == 0) {
            throw new IllegalArgumentException("Array of zero size is not allowed");
        }
        return new RefCountingCloner() {
            @Override
            public boolean refCheckPossible(Object obj) {
                if (obj == null) {
                    return false;
                }
                return Ins.ofNullable(obj).instanceOfAny(classes);
            }
        };
    }

    public static RefCountingCloner refCountingOf(Predicate predicate) {
        Objects.requireNonNull(predicate, "Predicate is null");
        return new RefCountingCloner() {
            @Override
            public boolean refCheckPossible(Object obj) {
                if (obj == null) {
                    return false;
                }
                return predicate.test(obj);
            }
        };
    }

    /**
     * Stores the new reference in identity map using old reference as key if
     * possible and returns supplier if it was stored successfully, otherwise
     * return null. Use this as the first line in object cloned construction
     * with old object as a key that has other references, so they won't
     * propagate. The reference may be stored in a middle of computation, so the
     * {@link Cloner} must facilitate value override to avoid cyclic
     * dependencies.
     *
     * @param <T>
     * @param <Y>
     * @param key
     * @param val
     * @return
     */
    public default <T, Y> Supplier<Y> refStoreIfPossible(T key, Y val) {
        return null;
    }

    /**
     *
     * @param <T> item type
     * @param <C> collection type
     * @param iter collection
     * @param collectionSupplier collection supplier
     * @return
     */
    public default <T, C extends Collection<T>> C cloneShallowCollection(C iter, Supplier<? extends C> collectionSupplier) {
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
    public default <A, D extends CloneSupport<A>> A cloneOrNull(D obj) {
        return obj == null ? null : obj.uncheckedClone(this);
    }

    /**
     * Clone or return a null
     *
     * @param <A> item type
     * @param <D> type that produces a cloned item
     * @param obj object to be cloned
     * @return cloned object or null
     */
    public default <A, D extends CloneSupport<A>> A cloneOrNullRef(D obj) {
        return cloneOrNull(obj);
    }

    /**
     * Analogous to cloneOrNull but also performs a cast. Implies that the
     * cloning function returns correct type. No explicit compile-time type
     * checking.
     *
     * @param <A> item type
     * @param obj object to be cloned
     * @param cloningFunction cloning function
     * @return cloned object or null
     */
    public default <A extends Cloneable> A cloneOrNullCast(A obj, Function<? super A, Object> cloningFunction) {
        Objects.requireNonNull(cloningFunction, "Cloning function is null");
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
    public default <D, A> A cloneOrNull(D obj, Function<? super D, A> cloningFunction) {
        Objects.requireNonNull(cloningFunction, "Cloning function is null");
        return obj == null ? null : cloningFunction.apply(obj);
    }

    public default <D extends A, A extends CloneSupport<A>, C extends Collection<D>> C cloneCollectionCast(Iterable<D> iter, Supplier<? extends C> collectionSupplier) {
        if (iter == null) {
            return null;
        }
        C collection = collectionSupplier.get();
        for (D item : iter) {
            D cloneOrNull = (D) cloneOrNull(item);
            collection.add(cloneOrNull);
        }

        return collection;
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
    public default <A, D extends CloneSupport<A>, C extends Collection<A>> C cloneCollection(Iterable<D> iter, Supplier<? extends C> collectionSupplier) {
        if (iter == null) {
            return null;
        }
        C collection = collectionSupplier.get();
        for (D item : iter) {
            A cloneOrNull = cloneOrNull(item);
            collection.add(cloneOrNull);
        }

        return collection;
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
    public default <K, KK, A, AA, C extends Map<K, A>, CC extends Map<KK, AA>> C cloneMap(CC map, Supplier<? extends C> collectionSupplier, Function<KK, K> keyCloningFuncion, Function<AA, A> cloningFuncion) {
        if (map == null) {
            return null;
        }

        C cloned = collectionSupplier.get();
        for (Map.Entry<KK, AA> entry : map.entrySet()) {
            K clonedKey = cloneOrNull(entry.getKey(), keyCloningFuncion);
            A clonedValue = cloneOrNull(entry.getValue(), cloningFuncion);
            cloned.put(clonedKey, clonedValue);
        }
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
    public default <K, A, C extends Map<K, A>> C cloneMapImmutableKeys(Map<K, A> map, Supplier<? extends C> collectionSupplier, Function<A, A> cloningFuncion) {
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
    public default <K, A, AA extends CloneSupport<A>, C extends Map<K, A>> C cloneMapImmutableSupported(Map<K, AA> map, Supplier<? extends C> collectionSupplier) {
        if (map == null) {
            return null;
        }
        return cloneMap(map, collectionSupplier, k -> k, v -> v.uncheckedClone(this));
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
    public default <K, KK extends CloneSupport<K>, A, AA extends CloneSupport<A>, C extends Map<K, A>> C cloneMapSupported(Map<KK, AA> map, Supplier<? extends C> collectionSupplier) {
        if (map == null) {
            return null;
        }
        return cloneMap(map, collectionSupplier,  k -> k.uncheckedClone(this),  v -> v.uncheckedClone(this));
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
    public default <D extends A, A extends CloneSupport<A>> A[] cloneArray(D[] iter, IntFunction<A[]> arraySupplier) {
        if (iter == null) {
            return null;
        }
        final int size = iter.length;
        A[] array = arraySupplier.apply(size);
        for (int i = 0; i < iter.length; i++) {
            array[i] = cloneOrNull(iter[i]);
        }
        return array;

    }

    /**
     * Clones an array with specific type hierarchy and casts the result.
     *
     * @param <A> item that supports clone type
     * @param <D> expected clone result type
     * @param iter array with items
     * @param arraySupplier array maker
     * @return
     */
    public default <D extends A, A extends CloneSupport<A>> D[] cloneArrayCast(D[] iter, IntFunction<D[]> arraySupplier) {
        if (iter == null) {
            return null;
        }
        final int size = iter.length;
        D[] array = arraySupplier.apply(size);
        for (int i = 0; i < iter.length; i++) {
            D cloneOrNull = (D) cloneOrNull(iter[i]);
            array[i] = cloneOrNull;
        }
        return array;

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
    public default <A, D extends A, C> C cloneAll(Iterable<D> iter, Supplier<C> sinkSupplier, BiConsumer<A, C> consumer, Function<D, A> cloningFuncion) {
        if (iter == null) {
            return null;
        }
        C collection = sinkSupplier.get();

        for (D item : iter) {
            consumer.accept(cloneOrNull(item, cloningFuncion), collection);
        }

        return collection;
    }
}
