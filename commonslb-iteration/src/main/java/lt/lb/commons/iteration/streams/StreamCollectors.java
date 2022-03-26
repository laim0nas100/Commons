package lt.lb.commons.iteration.streams;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamCollectors<X, M extends DecoratableStream<X, M>> extends StreamExtension<X, M> {

    /**
     * Quick-hand for {@link Collectors#joining() }
     */
    public default String joining() {
        return me().map(m -> String.valueOf(m)).collect(Collectors.joining());
    }

    /**
     * Quick-hand for {@link Collectors#joining(java.lang.CharSequence) }
     */
    public default String joining​(CharSequence delimiter) {
        return me().map(m -> String.valueOf(m)).collect(Collectors.joining(delimiter));
    }

    /**
     * Quick-hand for
     * {@link Collectors#joining(java.lang.CharSequence, java.lang.CharSequence, java.lang.CharSequence)}
     */
    public default String joining​(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        return me().map(m -> String.valueOf(m)).collect(Collectors.joining(delimiter, prefix, suffix));
    }

    /**
     * Quick-hand for
     * {@link Collectors#toCollection(java.util.function.Supplier) }
     *
     * @param <C>
     * @param supl
     * @return
     */
    public default <C extends Collection<X>> C toCollection(Supplier<C> supl) {
        return me().collect(Collectors.toCollection(supl));
    }

    /**
     * Quick-hand for {@link Collectors#toList() }
     */
    public default List<X> toList() {
        return me().collect(Collectors.toList());
    }

    /**
     * Quick-hand for {@link Collectors#toList() } wrapped in
     * {@link Collections#unmodifiableList(java.util.List)}
     *
     * Allows null values.
     */
    public default List<X> toUnmodifiableList() {
        return Collections.unmodifiableList(toList());
    }

    /**
     * Quick-hand for {@link Collectors#toSet() }
     */
    public default Set<X> toSet() {
        return me().collect(Collectors.toSet());
    }

    /**
     * Quick-hand for {@link Collectors#toSet() } wrapped in
     * {@link Collections#unmodifiableList(java.util.Set)}
     *
     * Allows null values.
     */
    public default Set<X> toUnmodifiableSet() {
        return Collections.unmodifiableSet(toSet());
    }

    /**
     * Quick-hand for
     * {@link Collectors#toMap(java.util.function.Function, java.util.function.Function)}
     */
    public default <K, V> Map<K, V> toMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper
    ) {
        return me().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    /**
     * Quick-hand for
     * {@link Collectors#toMap(java.util.function.Function, java.util.function.Function)}
     * wrapped in {@link Collections#unmodifiableMap(java.util.Map) }
     *
     * Allows null values or keys.
     */
    public default <K, V> Map<K, V> toUnmodifiableMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper
    ) {
        return Collections.unmodifiableMap(toMap(keyMapper, valueMapper));
    }

    /**
     * Quick-hand for
     * {@link Collectors#toMap(java.util.function.Function, java.util.function.Function, java.util.function.BinaryOperator) }
     */
    public default <K, V> Map<K, V> toMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction) {
        return me().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
    }

    /**
     * Quick-hand for
     * {@link Collectors#toMap(java.util.function.Function, java.util.function.Function, java.util.function.BinaryOperator)}
     * wrapped in {@link Collections#unmodifiableMap(java.util.Map) }
     *
     * Allows null values or keys.
     */
    public default <K, V> Map<K, V> toUnmodifiableMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction) {
        return Collections.unmodifiableMap(toMap(keyMapper, valueMapper, mergeFunction));
    }

    /**
     * Quick-hand for
     * {@link Collectors#toMap(java.util.function.Function, java.util.function.Function, java.util.function.BinaryOperator, java.util.function.Supplier)}
     */
    public default <K, V, U extends Map<K, V>> U toMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction,
            Supplier<U> mapFactory) {
        return me().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction, mapFactory));
    }

    /**
     * Quick-hand for
     * {@link Collectors#toConcurrentMap(java.util.function.Function, java.util.function.Function)}
     */
    public default <K, V> ConcurrentMap<K, V> toConcurrentMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper
    ) {
        return me().collect(Collectors.toConcurrentMap(keyMapper, valueMapper));
    }

    /**
     * Quick-hand for
     * {@link Collectors#toConcurrentMap(java.util.function.Function, java.util.function.Function, java.util.function.BinaryOperator) }
     */
    public default <K, V> ConcurrentMap<K, V> toConcurrentMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction) {
        return me().collect(Collectors.toConcurrentMap(keyMapper, valueMapper, mergeFunction));
    }

    /**
     * Quick-hand for
     * {@link Collectors#toConcurrentMap(java.util.function.Function, java.util.function.Function, java.util.function.BinaryOperator, java.util.function.Supplier)}
     */
    public default <K, V, U extends ConcurrentMap<K, V>> U toConcurrentMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction,
            Supplier<U> mapFactory) {
        return me().collect(Collectors.toConcurrentMap(keyMapper, valueMapper, mergeFunction, mapFactory));
    }

    /**
     * Quick-hand for {@link Collectors#groupingBy(java.util.function.Function)}
     */
    public default <K> Map<K, List<X>> groupingBy(Function<? super X, ? extends K> classifier) {
        return me().collect(Collectors.groupingBy(classifier));
    }

    /**
     * Quick-hand for
     * {@link Collectors#groupingBy(java.util.function.Function, java.util.stream.Collector)}
     */
    public default <K, D> Map<K, D> groupingBy(Function<? super X, ? extends K> classifier,
            Collector<? super X, ?, D> downstream) {
        return me().collect(Collectors.groupingBy(classifier, downstream));
    }

    /**
     * Quick-hand for
     * {@link Collectors#groupingBy(java.util.function.Function, java.util.function.Supplier, java.util.stream.Collector)}
     */
    public default <K, D, U extends Map<K, D>> U groupingBy(Function<? super X, ? extends K> classifier,
            Supplier<U> mapFactory, Collector<? super X, ?, D> downstream) {
        return me().collect(Collectors.groupingBy(classifier, mapFactory, downstream));
    }

    /**
     * Quick-hand for
     * {@link Collectors#groupingByConcurrent(java.util.function.Function)}
     */
    public default <K> ConcurrentMap<K, List<X>> groupingByConcurrent(Function<? super X, ? extends K> classifier) {
        return me().collect(Collectors.groupingByConcurrent(classifier));
    }

    /**
     * Quick-hand for
     * {@link Collectors#groupingByConcurrent(java.util.function.Function, java.util.stream.Collector)}
     */
    public default <K, D> ConcurrentMap<K, D> groupingByConcurrent(Function<? super X, ? extends K> classifier,
            Collector<? super X, ?, D> downstream) {
        return me().collect(Collectors.groupingByConcurrent(classifier, downstream));
    }

    /**
     * Quick-hand for
     * {@link Collectors#groupingByConcurrent(java.util.function.Function, java.util.function.Supplier, java.util.stream.Collector)}
     */
    public default <K, D, U extends ConcurrentMap<K, D>> U groupingByConcurrent(Function<? super X, ? extends K> classifier,
            Supplier<U> mapFactory, Collector<? super X, ?, D> downstream) {

        return me().collect(Collectors.groupingByConcurrent(classifier, mapFactory, downstream));
    }

    /**
     * Quick-hand for
     * {@link Collectors#partitioningBy(java.util.function.Predicate)}
     */
    public default <K> Map<Boolean, List<X>> partitioningBy(Predicate<? super X> classifier) {
        return me().collect(Collectors.partitioningBy(classifier));
    }

    /**
     * Quick-hand for
     * {@link Collectors#partitioningBy(java.util.function.Predicate, java.util.stream.Collector)}
     */
    public default <K, D> Map<Boolean, D> partitioningBy(Predicate<? super X> classifier, Collector<? super X, ?, D> downstream) {
        return me().collect(Collectors.partitioningBy(classifier, downstream));
    }

    /**
     * Limits the size to 2 and collects items to a list.If only one item is
     * present, returns it, otherwise the list was non-unique or empty, so
     * returns empty {@link Optional}
     *
     * @return unique item or empty.
     */
    public default Optional<X> toUniqueOrEmpty() {
        List<X> collect = me().limit(2).collect(Collectors.toList());
        if (collect.size() == 1) {
            return Optional.ofNullable(collect.get(0));
        } else {
            return Optional.empty();
        }
    }

}
