package lt.lb.commons.clone;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public abstract class RefCountingCloner implements Cloner {

    protected final IdentityHashMap<Object, RefSupply> refMap = new IdentityHashMap();

    /**
     * Check if reference is already seen, and returns it, otherwise returns a
     * null. Does not store the reference.
     *
     * @param obj
     * @return
     */
    public Object refCheck(Object obj) {
        if (obj == null) {
            return null;
        }
        Supplier supply = refMap.getOrDefault(obj, null);
        if (supply == null) {
            return null;
        }
        return supply.get();
    }

    /**
     * Return whether the object CAN be stored in map to even be checked. By
     * default non-primitive non-nulls can be stored;
     *
     * @param obj
     * @return
     */
    public abstract boolean refCheckPossible(Object obj);

    public static class RefSupply<Y> implements Supplier<Y> {

        private Supplier<Y> valueSupply;
        private boolean done = false;
        private boolean initiated = false;
        private boolean overriden = false;
        private Y value;
        private Y overridenValue;

        public RefSupply(Supplier<Y> valueSupply) {
            Objects.requireNonNull(valueSupply, "Ref value supply must not be null");
            this.valueSupply = valueSupply;
        }

        public RefSupply(Y value) {
            Objects.requireNonNull(value, "Ref value must not be null");
            this.value = value;
            done = true;
        }

        @Override
        public Y get() {
            if (done) {
                return value;
            }
            if (overriden) {
                return overridenValue;
            }
            if (initiated) {
                throw new IllegalStateException("Cyclic clone dependency, remember to call Cloner.refStoreIfPossible on cyclic reference objects");
            }
            initiated = true; // prevent cycles
            Y get = valueSupply.get();

            if (overriden && get != overridenValue) {
                throw new IllegalStateException("Computed value is not the same reference as overriden value");
            }
            value = get;
            done = true;
            initiated = false;
            return value;
        }

        public boolean storeRef(Y val) {
            Objects.requireNonNull(val, "Ref value must not be null");
            if (initiated && !done) {// inside clone process
                overridenValue = val;
                overriden = true;
                return true;
            }

            return false;
        }

    }

    /**
     * Stores the value producer in identity map if possible and returns it if
     * it was stored, otherwise return null
     *
     * @param <T>
     * @param <Y>
     * @param key
     * @param func
     * @return
     */
    public <T, Y> RefSupply<Y> refStoreIfPossibleFunc(T key, Function<T, Y> func) {
        Objects.requireNonNull(func, "Clone function is null");
        if (refCheckPossible(key)) {
            return refMap.computeIfAbsent(key, k -> new RefSupply<Y>(() -> func.apply(key)));
        }
        return null;
    }

    /**
     * {@inheritDoc }
     *
     * @param <T>
     * @param <Y>
     * @param key
     * @param val
     * @return
     */
    @Override
    public <T, Y> RefSupply<Y> refStoreIfPossible(T key, Y val) {
        if (refCheckPossible(key)) {
            return refMap.compute(key, (k, obj) -> {
                if (obj == null) {
                    return new RefSupply<>(val);
                } else {
                    obj.storeRef(val);
                    return obj;
                }
            });
        }
        return null;
    }

    /**
     * Clone or return a null
     *
     * @param <A> item type
     * @param <D> type that produces a cloned item
     * @param obj object to be cloned
     * @return cloned object or null
     */
    @Override
    public <A, D extends CloneSupport<A>> A cloneOrNullRef(D obj) {
        return cloneOrNull(obj);
    }

    @Override
    public <T, C extends Collection<T>> C cloneShallowCollection(C iter, Supplier<? extends C> collectionSupplier) {
        Supplier<? extends C> ref = refStoreIfPossibleFunc(iter, it -> Cloner.super.cloneShallowCollection(it, collectionSupplier));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneShallowCollection(iter, collectionSupplier);
    }

    @Override
    public <A, D extends CloneSupport<A>> A cloneOrNull(D obj) {
        Supplier<A> ref = refStoreIfPossibleFunc(obj, o -> o.uncheckedClone(this));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneOrNull(obj);
    }

    @Override
    public <A extends Cloneable> A cloneOrNullCast(A obj, Function<? super A, Object> cloningFunction) {
        Supplier<Object> ref = refStoreIfPossibleFunc(obj, cloningFunction);
        if (ref != null) {
            return (A) ref.get();
        }

        return Cloner.super.cloneOrNullCast(obj, cloningFunction);
    }

    @Override
    public <D, A> A cloneOrNull(D obj, Function<? super D, A> cloningFunction) {
        Supplier<A> ref = refStoreIfPossibleFunc(obj, cloningFunction);
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneOrNull(obj, cloningFunction);
    }

    @Override
    public <D extends A, A extends CloneSupport<A>, C extends Collection<D>> C cloneCollectionCast(Iterable<D> iter, Supplier<? extends C> collectionSupplier) {
        Supplier<C> ref = refStoreIfPossibleFunc(iter, it -> Cloner.super.cloneCollectionCast(it, collectionSupplier));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneCollectionCast(iter, collectionSupplier);
    }

    @Override
    public <A, D extends CloneSupport<A>, C extends Collection<A>> C cloneCollection(Iterable<D> iter, Supplier<? extends C> collectionSupplier) {
        Supplier<C> ref = refStoreIfPossibleFunc(iter, it -> Cloner.super.cloneCollection(it, collectionSupplier));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneCollection(iter, collectionSupplier);
    }

    @Override
    public <K, KK extends CloneSupport<K>, A, AA extends CloneSupport<A>, C extends Map<K, A>> C cloneMapSupported(Map<KK, AA> map, Supplier<? extends C> collectionSupplier) {
        Supplier<C> ref = refStoreIfPossibleFunc(map, it -> Cloner.super.cloneMapSupported(it, collectionSupplier));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneMapSupported(map, collectionSupplier);
    }

    @Override
    public <K, A, AA extends CloneSupport<A>, C extends Map<K, A>> C cloneMapImmutableSupported(Map<K, AA> map, Supplier<? extends C> collectionSupplier) {
        Supplier<C> ref = refStoreIfPossibleFunc(map, it -> Cloner.super.cloneMapImmutableSupported(it, collectionSupplier));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneMapImmutableSupported(map, collectionSupplier);
    }

    @Override
    public <K, A, C extends Map<K, A>> C cloneMapImmutableKeys(Map<K, A> map, Supplier<? extends C> collectionSupplier, Function<A, A> cloningFuncion) {
         Supplier<C> ref = refStoreIfPossibleFunc(map, it -> Cloner.super.cloneMapImmutableKeys(it, collectionSupplier, cloningFuncion));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneMapImmutableKeys(map, collectionSupplier, cloningFuncion);
    }

    
    
    @Override
    public <K, KK, A, AA, C extends Map<K, A>, CC extends Map<KK, AA>> C cloneMap(CC map, Supplier<? extends C> collectionSupplier, Function<KK, K> keyCloningFuncion, Function<AA, A> cloningFuncion) {
        Supplier<C> ref = refStoreIfPossibleFunc(map, it -> Cloner.super.cloneMap(it, collectionSupplier, keyCloningFuncion, cloningFuncion));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneMap(map, collectionSupplier, keyCloningFuncion, cloningFuncion);
    }

    @Override
    public <D extends A, A extends CloneSupport<A>> A[] cloneArray(D[] iter, IntFunction<A[]> arraySupplier) {
        Supplier<A[]> ref = refStoreIfPossibleFunc(iter, it -> Cloner.super.cloneArray(it, arraySupplier));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneArray(iter, arraySupplier);
    }

    @Override
    public <D extends A, A extends CloneSupport<A>> D[] cloneArrayCast(D[] iter, IntFunction<D[]> arraySupplier) {
        Supplier<D[]> ref = refStoreIfPossibleFunc(iter, it -> Cloner.super.cloneArrayCast(it, arraySupplier));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneArrayCast(iter, arraySupplier);
    }

    @Override
    public <A, D extends A, C> C cloneAll(Iterable<D> iter, Supplier<C> sinkSupplier, BiConsumer<A, C> consumer, Function<D, A> cloningFuncion) {
        Supplier<C> ref = refStoreIfPossibleFunc(iter, it -> Cloner.super.cloneAll(it, sinkSupplier, consumer, cloningFuncion));
        if (ref != null) {
            return ref.get();
        }
        return Cloner.super.cloneAll(iter, sinkSupplier, consumer, cloningFuncion);
    }

}
