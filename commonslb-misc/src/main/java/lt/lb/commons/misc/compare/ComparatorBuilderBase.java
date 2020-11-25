package lt.lb.commons.misc.compare;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * @author laim0nas100
 */
public abstract class ComparatorBuilderBase<T, M extends ComparatorBuilderBase<T, M>> {

    protected Optional<Comparator<? super T>> comparator = Optional.empty();
    protected Optional<M> parent = Optional.empty();
    protected boolean globalReverse;

    public ComparatorBuilderBase() {

    }

    /**
     * new keyword substitution
     *
     * @return
     */
    protected abstract M makeBase();

    /**
     * this keyword substitution
     *
     * @return
     */
    protected abstract M me();

    protected M fromGlobalReverse(M parent, boolean globalReverse) {
        M make = makeBase();
        make.globalReverse = globalReverse;
        make.parent = Optional.of(parent);
        return make;
    }

    protected M fromParent(M parent, Comparator<? super T> comparator) {
        M make = makeBase();
        make.parent = Optional.of(parent);
        make.comparator = Optional.of(comparator);
        return make;
    }

    public static <T> Comparator<T> empty() {
        return (T arg0, T arg1) -> 0;
    }

    /**
     * Construct a comparator on given configurations
     *
     * @return
     */
    public Comparator<T> build() {
        if (!parent.isPresent() && !comparator.isPresent()) {
            return ComparatorBuilderBase.empty();
        } else {

            LinkedList<M> chain = new LinkedList<>();
            M me = me();
            while (me.parent.isPresent()) {
                chain.addFirst(me);
                me = me.parent.get();
            }
            // optimization to find first
            Comparator<T> finalCmp = empty();
            while(!chain.isEmpty()) {
                M builder = chain.removeFirst();
                if (builder.comparator.isPresent()) {
                    finalCmp = builder.comparator.get()::compare;
                    break;
                }
            }
            for (M builder : chain) {
                if (builder.comparator.isPresent()) {
                    finalCmp = finalCmp.thenComparing(builder.comparator.get());
                } else if (builder.globalReverse) {
                    finalCmp = finalCmp.reversed();
                }

            }
            return finalCmp;
        }
    }

    /**
     * Construct a ext comparator of given configurations
     *
     * @return
     */
    public ExtComparator<T> buildExt() {
        return ExtComparator.of(build());
    }

    /**
     * Add comparator to the comparator chain.
     *
     * @param other
     * @return
     */
    public M thenComparing(Comparator<? super T> other) {
        return fromParent(me(), other);
    }

    /**
     * Add global reverse to currently present comparator chain. Will be applied after build.
     *
     * @return
     */
    public M reverseAll() {
        return fromGlobalReverse(me(), true);
    }

    /**
     * Add reverse to only the last comparator.
     *
     * @return
     *
     * @exception if no comparator is present
     */
    public M reverse() {
        if (!comparator.isPresent()) {
            throw new IllegalStateException("can't reverse last one. Must supply a comparator to reverse before doing a reverse, or do reverseAll after reversing the last one");
        }
        return fromParent(parent.get(), comparator.map(m -> m.reversed()).get());
    }

    /**
     *
     * @param <V> particular value of T object
     * @param func mapping function
     * @param cmp comparator to compare mapped value
     * @return
     */
    public <V> M thenComparingValue(Function<? super T, ? extends V> func, Comparator<? super V> cmp) {
        Comparator<T> c = (T arg0, T arg1) -> cmp.compare(func.apply(arg0), func.apply(arg1));
        return thenComparing(c);
    }

    /**
     *
     * @param <V> particular value of T object. Should handle null comparisons
     * @param func mapping function
     * @return
     */
    public <V extends Comparable> M thenComparingValue(Function<? super T, ? extends V> func) {
        Comparator<V> cmp = Comparator.naturalOrder();
        return thenComparingValue(func, cmp);

    }

    /**
     *
     * @param <V> particular value of T object.
     * @param emptyFirst if empty value should go first
     * @param func mapping function
     * @return
     */
    public <V extends Comparable> M thenComparingOptionalValue(boolean emptyFirst, Function<? super T, Optional<? extends V>> func) {
        Comparator<V> cmp = Comparator.naturalOrder();
        return thenComparingOptional(emptyFirst, func, cmp);
    }

    /**
     *
     * @param <V> particular value of T object.
     * @param emptyFirst if empty value should go first
     * @param func mapping function
     * @param cmp Comparator to compare the mapped 2 values
     * @return
     */
    public <V> M thenComparingOptional(boolean emptyFirst, Function<? super T, Optional<? extends V>> func, Comparator<? super V> cmp) {
        Comparator<T> comp = (v1, v2) -> {
            Optional<? extends V> apply1 = func.apply(v1);
            Optional<? extends V> apply2 = func.apply(v2);

            if (apply1.isPresent()) {
                if (apply2.isPresent()) {
                    return cmp.compare(apply1.get(), apply2.get());
                } else {
                    return emptyFirst ? 1 : -1; // second argument is empty
                }
            } else {
                if (apply2.isPresent()) {
                    return emptyFirst ? -1 : 1; // first argument is empty
                } else {
                    return 0;
                }
            }
        };

        return thenComparing(comp);

    }

    /**
     *
     * @param <V>
     * @param nullFirst if null should go first
     * @param func mapping function
     * @param cmp how to compare the 2 values
     * @return
     */
    public <V> M thenComparingNullable(boolean nullFirst, Function<? super T, ? extends V> func, Comparator<? super V> cmp) {

        Comparator<T> comp = (v1, v2) -> {
            V apply1 = func.apply(v1);
            V apply2 = func.apply(v2);

            if (apply1 != null) {
                if (apply2 != null) {
                    return cmp.compare(apply1, apply2);
                } else {
                    return nullFirst ? 1 : -1; // second argument is empty
                }
            } else {
                if (apply2 != null) {
                    return nullFirst ? -1 : 1; // first argument is empty
                } else {
                    return 0;
                }
            }
        };

        return thenComparing(comp);
    }

    /**
     *
     * @param <V>
     * @param nullFirst if null should go first
     * @param func mapping function
     * @return
     */
    public <V extends Comparable> M thenComparingNullableValue(boolean nullFirst, Function<? super T, ? extends V> func) {
        Comparator<V> cmp = Comparator.naturalOrder();
        return thenComparingNullable(nullFirst, func, cmp);
    }

    /**
     * Decorate with advanced mapping constructing another builder inside.
     *
     * @param <V>
     * @param func how to map to mapped value
     * @param decorator how to decorate the inner builder
     * @return
     */
    public <V, N extends ComparatorBuilderBase<V, N>> M mapped(Function<? super T, ? extends V> func, N builder) {
        Comparator<V> build = builder.build();
        return thenComparingValue(func, build);
    }

}
