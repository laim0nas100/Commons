package lt.lb.commons.containers.forwarded;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingStream<T> extends Stream<T> {

    Stream<T> delegate();

    @Override
    public default boolean allMatch(Predicate<? super T> predicate) {
        return delegate().allMatch(predicate);
    }

    @Override
    public default boolean anyMatch(Predicate<? super T> predicate) {
        return delegate().anyMatch(predicate);
    }

    @Override
    public default void close() {
        delegate().close();
    }

    @Override
    public default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return delegate().collect(supplier, accumulator, combiner);
    }

    @Override
    public default <R, A> R collect(Collector<? super T, A, R> collector) {
        return delegate().collect(collector);
    }

    @Override
    public default long count() {
        return delegate().count();
    }

    @Override
    public default Stream<T> distinct() {
        return delegate().distinct();
    }

    @Override
    public default Stream<T> filter(Predicate<? super T> predicate) {
        return delegate().filter(predicate);
    }

    @Override
    public default Optional<T> findAny() {
        return delegate().findAny();
    }

    @Override
    public default Optional<T> findFirst() {
        return delegate().findFirst();
    }

    @Override
    public default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return delegate().flatMap(mapper);
    }

    @Override
    public default DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return delegate().flatMapToDouble(mapper);
    }

    @Override
    public default IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return delegate().flatMapToInt(mapper);
    }

    @Override
    public default LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return delegate().flatMapToLong(mapper);
    }

    @Override
    public default void forEach(Consumer<? super T> action) {
        delegate().forEach(action);
    }

    @Override
    public default void forEachOrdered(Consumer<? super T> action) {
        delegate().forEachOrdered(action);
    }

    @Override
    public default boolean isParallel() {
        return delegate().isParallel();
    }

    @Override
    public default Iterator<T> iterator() {
        return delegate().iterator();
    }

    @Override
    public default Stream<T> limit(long maxSize) {
        return delegate().limit(maxSize);
    }

    @Override
    public default <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return delegate().map(mapper);
    }

    @Override
    public default DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return delegate().mapToDouble(mapper);
    }

    @Override
    public default IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return delegate().mapToInt(mapper);
    }

    @Override
    public default LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return delegate().mapToLong(mapper);
    }

    @Override
    public default Optional<T> max(Comparator<? super T> comparator) {
        return delegate().max(comparator);
    }

    @Override
    public default Optional<T> min(Comparator<? super T> comparator) {
        return delegate().min(comparator);
    }

    @Override
    public default boolean noneMatch(Predicate<? super T> predicate) {
        return delegate().noneMatch(predicate);
    }

    @Override
    public default Stream<T> onClose(Runnable closeHandler) {
        return delegate().onClose(closeHandler);
    }

    @Override
    public default Stream<T> parallel() {
        return delegate().parallel();
    }

    @Override
    public default Stream<T> peek(Consumer<? super T> action) {
        return delegate().peek(action);
    }

    @Override
    public default T reduce(T identity, BinaryOperator<T> accumulator) {
        return delegate().reduce(identity, accumulator);
    }

    @Override
    public default Optional<T> reduce(BinaryOperator<T> accumulator) {
        return delegate().reduce(accumulator);
    }

    @Override
    public default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return delegate().reduce(identity, accumulator, combiner);
    }

    @Override
    public default Stream<T> sequential() {
        return delegate().sequential();
    }

    @Override
    public default Stream<T> skip(long n) {
        return delegate().skip(n);
    }

    @Override
    public default Stream<T> sorted() {
        return delegate().sorted();
    }

    @Override
    public default Stream<T> sorted(Comparator<? super T> comparator) {
        return delegate().sorted(comparator);
    }

    @Override
    public default Spliterator<T> spliterator() {
        return delegate().spliterator();
    }

    @Override
    public default Object[] toArray() {
        return delegate().toArray();
    }

    @Override
    public default <A> A[] toArray(IntFunction<A[]> generator) {
        return delegate().toArray(generator);
    }

    @Override
    public default Stream<T> unordered() {
        return delegate().unordered();
    }
}
