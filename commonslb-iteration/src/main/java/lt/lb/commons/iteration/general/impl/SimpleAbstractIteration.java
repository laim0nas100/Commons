package lt.lb.commons.iteration.general.impl;

import java.util.Optional;
import lt.lb.commons.F;
import lt.lb.commons.iteration.general.IterationAbstract;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.cons.IterMapBiCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.impl.SimpleAbstractIteration.IterIterableAccessor.IterableBiConsAccessor;
import lt.lb.commons.iteration.general.impl.SimpleAbstractIteration.IterIterableAccessor.IterableConsAccessor;
import lt.lb.commons.iteration.general.impl.SimpleAbstractIteration.IterMapAccessor.MapBiConsAccessor;
import lt.lb.commons.iteration.general.impl.SimpleAbstractIteration.IterMapAccessor.MapConsAccessor;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public abstract class SimpleAbstractIteration<E extends SimpleAbstractIteration<E>> implements IterationAbstract<E> {

    protected int onlyIncludingLast = -1;
    protected int onlyIncludingFirst = -1;

    @Override
    public E first(int amountToInclude) {
        this.onlyIncludingFirst = amountToInclude;
        this.onlyIncludingLast = -1;
        return me();
    }

    @Override
    public E last(int amountToInclude) {
        this.onlyIncludingLast = amountToInclude;
        this.onlyIncludingFirst = -1;
        return me();
    }

    protected abstract E me();

    protected static IterableConsAccessor iterConsAccessor = new IterableConsAccessor();

    protected static IterableBiConsAccessor iterConsBiAccessor = new IterableBiConsAccessor();

    protected static MapConsAccessor mapConsAccessor = new MapConsAccessor();

    protected static MapBiConsAccessor mapConsBiAccessor = new MapBiConsAccessor();

    protected IterIterableAccessor resolveAccessor(IterIterableCons iter) {
        if (iter instanceof IterIterableBiCons) {
            return iterConsBiAccessor;
        }
        if (iter instanceof IterIterableCons) {
            return iterConsAccessor;
        }

        throw new IllegalArgumentException("Failed to resolve for iteration type " + iter);
    }

    protected IterMapAccessor resolveAccessor(IterMapCons iter) {
        if (iter instanceof IterMapBiCons) {
            return mapConsBiAccessor;
        }
        if (iter instanceof IterMapCons) {
            return mapConsAccessor;
        }

        throw new IllegalArgumentException("Failed to resolve for iteration type " + iter);
    }

    public static interface IterIterableAccessor {

        public <T> Optional<IterIterableResult<T>> tryVisit(int index, T val, IterIterableCons<T> iter);

        public static class IterableConsAccessor implements IterIterableAccessor {

            @Override
            public <T> Optional<IterIterableResult<T>> tryVisit(int index, T val, IterIterableCons<T> iter) {
                IterIterableResult<T> res = new IterIterableResult<>(index, val);
                if (iter.visit(res)) {
                    return Optional.of(res);
                } else {
                    return Optional.empty();
                }
            }

        }

        public static class IterableBiConsAccessor implements IterIterableAccessor {

            @Override
            public <T> Optional<IterIterableResult<T>> tryVisit(int index, T val, IterIterableCons<T> iter) {
                IterIterableBiCons<T> iterBi = F.cast(iter);
                if (iterBi.visit(index, val)) {
                    return Optional.of(new IterIterableResult<>(index, val));
                } else {
                    return Optional.empty();
                }
            }

        }

    }

    public static interface IterMapAccessor {

        public <K, V> Optional<IterMapResult<K, V>> tryVisit(int index, K key, V val, IterMapCons<K, V> iter);

        public static class MapConsAccessor implements IterMapAccessor {

            @Override
            public <K, V> Optional<IterMapResult<K, V>> tryVisit(int index, K key, V val, IterMapCons<K, V> iter) {
                IterMapResult<K, V> res = new IterMapResult<>(index, key, val);
                if (iter.visit(res)) {
                    return Optional.of(res);
                } else {
                    return Optional.empty();
                }
            }

        }

        public static class MapBiConsAccessor implements IterMapAccessor {

            @Override
            public <K, V> Optional<IterMapResult<K, V>> tryVisit(int index, K key, V val, IterMapCons<K, V> iter) {
                IterMapBiCons<K, V> iterBi = F.cast(iter);
                if (iterBi.visit(key, val)) {
                    return Optional.of(new IterMapResult<>(index, key, val));
                } else {
                    return Optional.empty();
                }
            }

        }

    }
}