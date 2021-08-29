package lt.lb.commons.iteration.streams.extendable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
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
 * @param <X> element type
 * @param <M> implementation type
 */
public interface DelegatingStream<X, M extends DelegatingStream<X, M>> extends Stream<X> {

    /**
     *
     * @return a delegate (real) stream
     */
    public Stream<X> delegate();

    /**
     * Create a new instance using modified stream with different parameter. Can
     * be mutable or immutable, depends on the implementation.
     *
     * Implementations should override this method with their specific type
     * info.
     *
     * @param <R>
     * @param modifiedStream
     * @return
     */
    public <R> DelegatingStream<R, ?> reconstruct(Stream<R> modifiedStream);

    /**
     *
     * @return this
     */
    public M me();

    /**
     * Use terminal operation on this instance. Not strictly enforced. The
     * consumer can have no effect.
     *
     * @param consumer
     */
    public default void terminal(Consumer<? super Stream<X>> consumer) {
        Objects.requireNonNull(consumer, "Stream consumer must not be null");
        consumer.accept(delegate());
    }

    /**
     * Use intermediate operation on this instance that return a result. Not
     * strictly enforced.
     *
     * @param <P>
     * @param mapper intermediate mapper
     * @return result returned by mapper
     */
    public default <P> P itermediateResult(Function<? super Stream<X>, ? extends P> mapper) {
        Objects.requireNonNull(mapper, "Stream mapper must not be null");
        return mapper.apply(delegate());
    }

    /**
     * Use terminal operation on this instance that return a result. Not
     * strictly enforced.
     *
     * @param <P>
     * @param mapper
     * @return result returned by mapper
     */
    public default <P> P terminalResult(Function<? super Stream<X>, ? extends P> mapper) {
        Objects.requireNonNull(mapper, "Stream mapper must not be null");
        return mapper.apply(delegate());
    }

    /**
     * Functor pattern
     *
     * @param decorator
     * @return
     */
    public default M decorating(Function<? super M, ? extends M> decorator) {
        Objects.requireNonNull(decorator, "Stream decorator must not be null");
        return decorator.apply(me());
    }

    /**
     * Use intermediate operation on this instance. Not strictly enforced.
     * Usually reconstruct the same stream type.
     *
     * @param decorator intermediate decorator
     * @return result returned by decorator
     */
    public default M intermediate(Function<? super Stream<X>, ? extends Stream<X>> decorator) {
        Objects.requireNonNull(decorator, "Stream decorator must not be null");
        return (M) reconstruct(decorator.apply(delegate()));
    }

    /**
     * Use intermediate operation on this instance. Not strictly enforced.
     *
     * Implementations should override this method with their specific type
     * info.
     *
     * @param streamDecor intermediate mapper
     * @return resulting stream returned by mapper
     */
    public default <R> DelegatingStream<R, ?> intermediateMap(Function<? super Stream<X>, ? extends Stream<R>> streamDecor) {
        Objects.requireNonNull(streamDecor, "Stream decorator must not be null");
        return reconstruct(streamDecor.apply(delegate()));
    }

    @Override
    public default M filter(Predicate<? super X> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");
        return intermediate(s -> s.filter(predicate));
    }

    @Override
    public default <R> DelegatingStream<R, ?> map(Function<? super X, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return intermediateMap(s -> s.map(mapper));
    }

    @Override
    public default IntStream mapToInt(ToIntFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return itermediateResult(s -> s.mapToInt(mapper));
    }

    @Override
    public default LongStream mapToLong(ToLongFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return itermediateResult(s -> s.mapToLong(mapper));
    }

    @Override
    public default DoubleStream mapToDouble(ToDoubleFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return itermediateResult(s -> s.mapToDouble(mapper));
    }

    @Override
    public default <R> DelegatingStream<R, ?> flatMap(Function<? super X, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return intermediateMap(s -> s.flatMap(mapper));
    }

    @Override
    public default IntStream flatMapToInt(Function<? super X, ? extends IntStream> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return itermediateResult(s -> s.flatMapToInt(mapper));
    }

    @Override
    public default LongStream flatMapToLong(Function<? super X, ? extends LongStream> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return itermediateResult(s -> s.flatMapToLong(mapper));
    }

    @Override
    public default DoubleStream flatMapToDouble(Function<? super X, ? extends DoubleStream> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return itermediateResult(s -> s.flatMapToDouble(mapper));
    }

    @Override
    public default M distinct() {
        return intermediate(s -> s.distinct());
    }

    @Override
    public default M sorted() {
        return intermediate(s -> s.sorted());
    }

    @Override
    public default M sorted(Comparator<? super X> comparator) {
        Objects.requireNonNull(comparator, "Comparator must not be null");
        return intermediate(s -> s.sorted(comparator));
    }

    @Override
    public default M peek(Consumer<? super X> action) {
        Objects.requireNonNull(action, "Action must not be null");
        return intermediate(s -> s.peek(action));
    }

    @Override
    public default M limit(long maxSize) {
        return intermediate(s -> s.limit(maxSize));
    }

    @Override
    public default M skip(long n) {
        return intermediate(s -> s.skip(n));
    }

    @Override
    public default void forEach(Consumer<? super X> action) {
        Objects.requireNonNull(action, "Action must not be null");
        terminal(s -> s.forEach(action));
    }

    @Override
    public default void forEachOrdered(Consumer<? super X> action) {
        Objects.requireNonNull(action, "Action must not be null");
        terminal(s -> s.forEachOrdered(action));
    }

    @Override
    public default Object[] toArray() {
        return terminalResult(s -> s.toArray());
    }

    @Override
    public default <A> A[] toArray(IntFunction<A[]> generator) {
        Objects.requireNonNull(generator, "Generator must not be null");
        return terminalResult(s -> s.toArray(generator));
    }

    @Override
    public default X reduce(X identity, BinaryOperator<X> accumulator) {
        Objects.requireNonNull(accumulator, "Accumulator must not be null");
        return terminalResult(s -> s.reduce(identity, accumulator));
    }

    @Override
    public default Optional<X> reduce(BinaryOperator<X> accumulator) {
        Objects.requireNonNull(accumulator, "Accumulator must not be null");
        return terminalResult(s -> s.reduce(accumulator));
    }

    @Override
    public default <U> U reduce(U identity, BiFunction<U, ? super X, U> accumulator, BinaryOperator<U> combiner) {
        Objects.requireNonNull(accumulator, "Accumulator must not be null");
        Objects.requireNonNull(combiner, "Combiner must not be null");
        return terminalResult(s -> s.reduce(identity, accumulator, combiner));
    }

    @Override
    public default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super X> accumulator, BiConsumer<R, R> combiner) {
        Objects.requireNonNull(supplier, "Supplier must not be null");
        Objects.requireNonNull(accumulator, "Accumulator must not be null");
        Objects.requireNonNull(combiner, "Combiner must not be null");
        return terminalResult(s -> s.collect(supplier, accumulator, combiner));
    }

    @Override
    public default <R, A> R collect(Collector<? super X, A, R> collector) {
        Objects.requireNonNull(collector, "Collector must not be null");
        return terminalResult(s -> s.collect(collector));
    }

    @Override
    public default Optional<X> min(Comparator<? super X> comparator) {
        Objects.requireNonNull(comparator, "Comparator must not be null");
        return terminalResult(s -> s.min(comparator));
    }

    @Override
    public default Optional<X> max(Comparator<? super X> comparator) {
        Objects.requireNonNull(comparator, "Comparator must not be null");
        return terminalResult(s -> s.max(comparator));
    }

    @Override
    public default long count() {
        return terminalResult(s -> s.count());
    }

    @Override
    public default boolean anyMatch(Predicate<? super X> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");
        return terminalResult(s -> s.anyMatch(predicate));
    }

    @Override
    public default boolean allMatch(Predicate<? super X> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");
        return terminalResult(s -> s.allMatch(predicate));
    }

    @Override
    public default boolean noneMatch(Predicate<? super X> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");
        return terminalResult(s -> s.noneMatch(predicate));
    }

    @Override
    public default Optional<X> findFirst() {
        return terminalResult(s -> s.findFirst());
    }

    @Override
    public default Optional<X> findAny() {
        return terminalResult(s -> s.findAny());
    }

    @Override
    public default Iterator<X> iterator() {
        return terminalResult(s -> s.iterator());
    }

    @Override
    public default Spliterator<X> spliterator() {
        return terminalResult(s -> s.spliterator());
    }

    @Override
    public default boolean isParallel() {
        return itermediateResult(s -> s.isParallel());
    }

    @Override
    public default M sequential() {
        return intermediate(s -> s.sequential());
    }

    @Override
    public default M parallel() {
        return intermediate(s -> s.parallel());
    }

    @Override
    public default M unordered() {
        return intermediate(s -> s.unordered());
    }

    @Override
    public default M onClose(Runnable closeHandler) {
        Objects.requireNonNull(closeHandler, "CloseHandler must not be null");
        return intermediate(s -> s.onClose(closeHandler));
    }

    @Override
    public default void close() {
        terminal(s -> s.close());
    }

    public default <U> U chain(Function<? super M, U> chainMapper) {
        Objects.requireNonNull(chainMapper, "Chain mapper must not be null");
        return chainMapper.apply((me()));
    }

}
