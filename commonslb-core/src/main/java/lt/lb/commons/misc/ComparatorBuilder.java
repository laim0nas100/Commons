package lt.lb.commons.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * Immutable. Builds a comparator with value mappings.
 *
 * @author laim0nas100
 */
public class ComparatorBuilder<T> {

    private ArrayList<Comparator<? super T>> comparators = new ArrayList<>();

    public ComparatorBuilder() {

    }

    private ComparatorBuilder(List<Comparator<? super T>> comps, Comparator<? super T> comparator) {
        comparators.addAll(comps);
        comparators.add(comparator);
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
        if (comparators.isEmpty()) {
            return ComparatorBuilder.empty();
        } else {
            return this::compareArgs;
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
     * Compare using current comparators in order
     *
     * @param arg0
     * @param arg1
     * @return
     */
    public int compareArgs(T arg0, T arg1) {
        if (comparators.isEmpty()) {
            return 0;
        }
        for (Comparator<? super T> cmp : comparators) {
            int res = cmp.compare(arg0, arg1);
            if (res != 0) {
                return res;
            }
        }
        return 0;
    }

    /**
     * Add comparator to the comparator list.
     *
     * @param other
     * @return
     */
    public ComparatorBuilder<T> thenComparing(Comparator<? super T> other) {
        return new ComparatorBuilder<>(comparators, other);
    }

    /**
     *
     * @param <V> particular value of T object
     * @param func mapping function
     * @param cmp comparator to compare mapped value
     * @return
     */
    public <V> ComparatorBuilder<T> thenComparingValue(Function<? super T, ? extends V> func, Comparator<? super V> cmp) {
        Comparator<T> c = (T arg0, T arg1) -> cmp.compare(func.apply(arg0), func.apply(arg1));
        return thenComparing(c);
    }

    /**
     *
     * @param <V> particular value of T object. Should handle null comparisons
     * @param func mapping function
     * @return
     */
    public <V extends Comparable> ComparatorBuilder<T> thenComparingValue(Function<? super T, ? extends V> func) {
        return thenComparingValue(func, Comparator.naturalOrder());
    }

    /**
     *
     * @param <V> particular value of T object.
     * @param emptyFirst if empty value should go first
     * @param func mapping function
     * @return
     */
    public <V extends Comparable> ComparatorBuilder<T> thenComparingOptionalValue(boolean emptyFirst, Function<? super T, Optional<? extends V>> func) {
        return thenComparingOptional(emptyFirst, func, Comparator.naturalOrder());
    }

    /**
     *
     * @param <V> particular value of T object.
     * @param emptyFirst if empty value should go first
     * @param func mapping function
     * @param cmp Comparator to compare the mapped 2 values
     * @return
     */
    public <V> ComparatorBuilder<T> thenComparingOptional(boolean emptyFirst, Function<? super T, Optional<? extends V>> func, Comparator<? super V> cmp) {
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
    public <V> ComparatorBuilder<T> thenComparingNullable(boolean nullFirst, Function<? super T, ? extends V> func, Comparator<? super V> cmp) {

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
    public <V extends Comparable> ComparatorBuilder<T> thenComparingNullableValue(boolean nullFirst, Function<? super T, ? extends V> func) {
        return thenComparingNullable(nullFirst, func, Comparator.naturalOrder());
    }

    /**
     * Decorate with advanced mapping constructing another builder inside.
     *
     * @param <V>
     * @param func how to map to mapped value
     * @param decorator how to decorate the inner builder
     * @return
     */
    public <V> ComparatorBuilder<T> mapped(Function<? super T, ? extends V> func, Function<ComparatorBuilder<V>, ComparatorBuilder<V>> decorator) {
        ComparatorBuilder<V> apply = decorator.apply(new ComparatorBuilder<>());
        Comparator<V> build = apply.build();
        return thenComparingValue(func, build);
    }

}
