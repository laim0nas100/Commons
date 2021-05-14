package lt.lb.commons.iteration.general.accessors;

import lt.lb.commons.F;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.cons.IterMapBiCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import lt.lb.commons.iteration.general.result.IterMapResult;
import lt.lb.uncheckedutils.NestedException;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class AccessorImpl {

    public static <T> SafeOpt<IterIterableResult<T>> visitUncaught(IterIterableCons<T> iter, IterIterableResult<T> res) {
        return iter.visit(res) ? SafeOpt.of(res) : SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> visitCaught(IterIterableCons<T> iter, IterIterableResult<T> res) {
        return SafeOpt.ofNullable(res).filter(r -> iter.visit(r));
    }

    public static <T> SafeOpt<IterIterableResult<T>> visitUncaught(IterIterableBiCons<T> iter, int index, T val) {
        return iter.visit(index, val) ? SafeOpt.of(new IterIterableResult<>(index, val)) : SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> visitCaught(IterIterableBiCons<T> iter, int index, T val) {
        try {
            return visitUncaught(iter, index, val);
        } catch (Throwable th) {
            return SafeOpt.error(NestedException.unwrap(th));
        }
    }

    public static <K, V> SafeOpt<IterMapResult<K, V>> visitUncaught(IterMapCons<K, V> iter, IterMapResult<K, V> res) {
        return iter.visit(res) ? SafeOpt.of(res) : SafeOpt.empty();
    }

    public static <K, V> SafeOpt<IterMapResult<K, V>> visitCaught(IterMapCons<K, V> iter, IterMapResult<K, V> res) {
        return SafeOpt.ofNullable(res).filter(r -> iter.visit(r));
    }

    public static <K, V> SafeOpt<IterMapResult<K, V>> visitUncaught(int index, K key, V val, IterMapBiCons<K, V> iter) {
        return iter.visit(key, val) ? SafeOpt.of(new IterMapResult<>(index, key, val)) : SafeOpt.empty();
    }

    public static <K, V> SafeOpt<IterMapResult<K, V>> visitCaught(int index, K key, V val, IterMapBiCons<K, V> iter) {
        try {
            return visitUncaught(index, key, val, iter);
        } catch (Throwable th) {
            return SafeOpt.error(NestedException.unwrap(th));
        }
    }
}
