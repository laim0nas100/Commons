package lt.lb.commons.iteration.streams.extendable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
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

    public default String joining() {
        return me().map(m -> String.valueOf(m)).collect(Collectors.joining());
    }

    public default String joining​(CharSequence delimiter) {
        return me().map(m -> String.valueOf(m)).collect(Collectors.joining(delimiter));
    }

    public default String joining​(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        return me().map(m -> String.valueOf(m)).collect(Collectors.joining(delimiter, prefix, suffix));
    }

    public default <C extends Collection<X>> C toCollection(Supplier<C> supl) {
        return me().collect(Collectors.toCollection(supl));
    }

    public default List<X> toList() {
        return me().collect(Collectors.toList());
    }

    public default Set<X> toSet() {
        return me().collect(Collectors.toSet());
    }

    public default <K, V> Map<K, V> toMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper
    ) {
        return me().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public default <K, V> Map<K, V> toMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction) {
        return me().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
    }

    public default <K, V, U extends Map<K, V>> U toMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction,
            Supplier<U> mapFactory) {
        return me().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction, mapFactory));
    }
    
    
    public default <K, V> ConcurrentMap<K, V> toConcurrentMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper
    ) {
        return me().collect(Collectors.toConcurrentMap(keyMapper, valueMapper));
    }

    public default <K, V> ConcurrentMap<K, V> toConcurrentMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction) {
        return me().collect(Collectors.toConcurrentMap(keyMapper, valueMapper, mergeFunction));
    }

    public default <K, V, U extends ConcurrentMap<K, V>> U toConcurrentMap(Function<? super X, ? extends K> keyMapper,
            Function<? super X, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction,
            Supplier<U> mapFactory) {
        return me().collect(Collectors.toConcurrentMap(keyMapper, valueMapper, mergeFunction, mapFactory));
    }

    public default <K> Map<K, List<X>> groupingBy(Function<? super X, ? extends K> classifier) {
        return me().collect(Collectors.groupingBy(classifier));
    }

    public default <K, D> Map<K, D> groupingBy(Function<? super X, ? extends K> classifier,
            Collector<? super X, ?, D> downstream) {
        return me().collect(Collectors.groupingBy(classifier, downstream));
    }

    public default <K, D, U extends Map<K, D>> U groupingBy(Function<? super X, ? extends K> classifier,
            Supplier<U> mapFactory, Collector<? super X, ?, D> downstream) {
        return me().collect(Collectors.groupingBy(classifier, mapFactory, downstream));
    }
    
    
    public default <K> ConcurrentMap<K, List<X>> groupingByConcurrent(Function<? super X, ? extends K> classifier) {
        return me().collect(Collectors.groupingByConcurrent(classifier));
    }

    public default <K, D> ConcurrentMap<K, D> groupingByConcurrent(Function<? super X, ? extends K> classifier,
            Collector<? super X, ?, D> downstream) {
        return me().collect(Collectors.groupingByConcurrent(classifier, downstream));
    }

    public default <K, D, U extends ConcurrentMap<K, D>> U groupingByConcurrent(Function<? super X, ? extends K> classifier,
            Supplier<U> mapFactory, Collector<? super X, ?, D> downstream) {
        
        return me().collect(Collectors.groupingByConcurrent(classifier, mapFactory, downstream));
    }

}
