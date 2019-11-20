package lt.lb.commons.func;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * Immutable stream action collector
 *
 * @author laim0nas100
 * @param <T> source type
 * @param <Z> result type
 */
public class StreamMapper<T, Z> extends StreamMapperAbstr<T, Z, Stream<Z>> {

    protected ArrayList<Function<Stream<Z>, Stream<Z>>> decs;
    protected Function<Object, Z> mapper;
    protected Function<Object, Stream<Z>> flatmapper;
    protected StreamMapper<Object, Z> after;
    protected StreamMapper<T, Object> parent;

    public static <E> StreamDecorator<E> of() {
        return new StreamDecorator<>();
    }

    public static <E> StreamDecorator<E> of(Iterable<E> iter) {
        return new StreamDecorator<>();
    }

    public static <E> StreamDecorator<E> of(Class<E> cls) {
        return new StreamDecorator<>();
    }

    @Override
    public Stream<Z> startingWith(Stream<T> stream) {
        return decorate(stream);
    }

    public static class StreamDecorator<E> extends StreamMapper<E, E> {
    };

    public StreamMapper() {
        this(0);
    }

    private StreamMapper(int size) {
        decs = new ArrayList<>(size);
    }

    /**
     * Decorate and map a stream with decorators within this object
     *
     * @param stream
     * @return
     */
    public Stream<Z> decorate(Stream<T> stream) {
        Objects.requireNonNull(stream, "Given stream was null");
        if (parent != null) {
            Stream decorated = parent.decorate(stream);
            if (mapper != null) {
                return applyDecs(decorated.map(mapper));
            } else if (flatmapper != null) {
                return applyDecs(decorated.flatMap(flatmapper));
            } else {
                return applyDecs(after.decorate(decorated));
            }
        } else {
            //this should be the same then
            return applyDecs((Stream<Z>) stream);
        }

    }

    /**
     * Only apply decorators
     *
     * @param stream
     * @return
     */
    public Stream<Z> applyDecs(Stream<Z> stream) {
        for (Function<Stream<Z>, Stream<Z>> fun : decs) {
            stream = fun.apply(stream);
        }
        return stream;
    }

    /**
     * DEFAULT STREAM ACTIONS
     */
    /**
     * Applies onClose decorator
     *
     * @param onClose
     * @return
     */
    public StreamMapper<T, Z> onClose(Runnable onClose) {
        return then(s -> s.onClose(onClose));
    }

    /**
     * Applies peek decorator
     *
     * @param action
     * @return
     */
    public StreamMapper<T, Z> peek(Consumer<? super Z> action) {
        return then(s -> s.peek(action));
    }

    /**
     * Applies filter decorator
     *
     * @param predicate
     * @return
     */
    public StreamMapper<T, Z> filter(Predicate<? super Z> predicate) {
        return then(s -> s.filter(predicate));
    }

    /**
     * Applies sorted decorator with comparator
     *
     * @param comparator
     * @return
     */
    public StreamMapper<T, Z> sorted(Comparator<? super Z> comparator) {
        return then(s -> s.sorted(comparator));
    }

    /**
     * Applies sorted decorator
     *
     * @return
     */
    public StreamMapper<T, Z> sorted() {
        return then(s -> s.sorted());
    }

    /**
     * Applies distinct decorator
     *
     * @return
     */
    public StreamMapper<T, Z> distinct() {
        return then(s -> s.distinct());
    }

    /**
     * Applies unordered decorator
     *
     * @return
     */
    public StreamMapper<T, Z> unordered() {
        return then(s -> s.unordered());
    }

    /**
     * Applies parallel decorator
     *
     * @return
     */
    public StreamMapper<T, Z> parallel() {
        return then(s -> s.parallel());
    }

    /**
     * Applies sequential decorator
     *
     * @return
     */
    public StreamMapper<T, Z> sequential() {
        return then(s -> s.sequential());
    }

    /**
     * Applies limit decorator
     *
     * @param maxSize
     * @return
     */
    public StreamMapper<T, Z> limit(long maxSize) {
        return then(s -> s.limit(maxSize));
    }

    /**
     * Applies skip decorator
     *
     * @param n
     * @return
     */
    public StreamMapper<T, Z> skip(long n) {
        return then(s -> s.skip(n));
    }

    /**
     * MAPPING
     */
    /**
     * Map decorator
     *
     * @param <R>
     * @param mapper
     * @return
     */
    public <R> StreamMapper<T, R> map(Function<? super Z, ? extends R> mapper) {

        StreamMapper streamDecorator = new StreamMapper();
        streamDecorator.mapper = mapper;
        streamDecorator.parent = this;
        return streamDecorator;
    }

    /**
     * Flatmap decorator
     *
     * @param <R>
     * @param mapper
     * @return
     */
    public <R> StreamMapper<T, R> flatMap(Function<? super Z, ? extends Stream<? extends R>> mapper) {

        StreamMapper streamDecorator = new StreamMapper();
        streamDecorator.flatmapper = mapper;
        streamDecorator.parent = this;
        return streamDecorator;

    }

    /**
     * Flatmap decorator from iterable converting null to empty stream
     *
     * @param <R>
     * @param mapper
     * @return
     */
    public <R> StreamMapper<T, R> flatMapIterable(Function<? super Z, ? extends Iterable<? extends R>> mapper) {
        return flatMap(s -> fromIterable(mapper.apply(s)));
    }

    /**
     * Flatmap decorator from iterator converting null to empty stream
     *
     * @param <R>
     * @param mapper
     * @return
     */
    public <R> StreamMapper<T, R> flatMapIterator(Function<? super Z, ? extends Iterator<? extends R>> mapper) {
        return flatMap(s -> fromIterator(mapper.apply(s)));
    }

    /**
     * ENDERS
     */
    /**
     * Produce ender with forEach action
     *
     * @param action
     * @return
     */
    public StreamMapperEnder<T, Z, Void> forEach(Consumer<? super Z> action) {
        return endByVoid(s -> s.forEach(action));
    }

    /**
     * Produce ender with forEach action and index
     *
     * @param action
     * @return
     */
    public StreamMapperEnder<T, Z, Void> forEach(BiConsumer<Integer, ? super Z> action) {
        return endByVoid(s -> {
            AtomicInteger i = new AtomicInteger(0);
            s.forEach(a -> {
                action.accept(i.getAndIncrement(), a);
            });

        });
    }

    /**
     * Produce ender with forEachOrdered action
     *
     * @param action
     * @return
     */
    public StreamMapperEnder<T, Z, Void> forEachOrdered(Consumer<? super Z> action) {
        return endByVoid(s -> s.forEachOrdered(action));
    }

    /**
     * Produce ender with forEachOrdered action and index
     *
     * @param action
     * @return
     */
    public StreamMapperEnder<T, Z, Void> forEachOrdered(BiConsumer<Integer, ? super Z> action) {
        return endByVoid(s -> {
            AtomicInteger i = new AtomicInteger(0);
            s.forEachOrdered(a -> {
                action.accept(i.getAndIncrement(), a);
            });

        });
    }

    /**
     * Produce ender with toArray action
     *
     * @return
     */
    public StreamMapperEnder<T, Z, Object[]> toArray() {
        return endBy(s -> s.toArray());
    }

    /**
     * Produce ender with toArray action that takes array generator function as
     * a parameter
     *
     * @param <A>
     * @param generator
     * @return
     */
    public <A> StreamMapperEnder<T, Z, A[]> toArray(IntFunction<A[]> generator) {
        return endBy(s -> s.toArray(generator));
    }

    /**
     * Produce ender with reduce action
     *
     * @param identity
     * @param accumulator
     * @return
     */
    public StreamMapperEnder<T, Z, Z> reduce(Z identity, BinaryOperator<Z> accumulator) {
        return endBy(s -> s.reduce(identity, accumulator));
    }

    /**
     * Produce ender with reduce action
     *
     * @param accumulator
     * @return
     */
    public StreamMapperEnder<T, Z, Optional<Z>> reduce(BinaryOperator<Z> accumulator) {
        return endBy(s -> s.reduce(accumulator));
    }

    /**
     * Produce ender with reduce action
     *
     * @param <U>
     * @param identity
     * @param accumulator
     * @param combiner
     * @return
     */
    public <U> StreamMapperEnder<T, Z, U> reduce(U identity,
            BiFunction<U, ? super Z, U> accumulator,
            BinaryOperator<U> combiner) {
        return endBy(s -> s.reduce(identity, accumulator, combiner));
    }

    /**
     * Produce ender with collect action
     *
     * @param <R>
     * @param supplier
     * @param accumulator
     * @param combiner
     * @return
     */
    public <R> StreamMapperEnder<T, Z, R> collect(Supplier<R> supplier,
            BiConsumer<R, ? super Z> accumulator,
            BiConsumer<R, R> combiner) {
        return endBy(s -> s.collect(supplier, accumulator, combiner));
    }

    /**
     * Produce ender with collect action
     *
     * @param <R>
     * @param <A>
     * @param collector
     * @return
     */
    public <R, A> StreamMapperEnder<T, Z, R> collect(Collector<? super Z, A, R> collector) {
        return endBy(s -> s.collect(collector));
    }

    /**
     * Produce ender with collect to {@code List} action
     *
     * @param <A>
     * @return
     */
    public <A> StreamMapperEnder<T, Z, List<Z>> collectToList() {
        return endBy(s -> s.collect(Collectors.toList()));
    }

    /**
     * Produce ender with transform to {@code Iterator} action
     *
     * @param <A>
     * @return
     */
    public <A> StreamMapperEnder<T, Z, Iterator<Z>> toIterator() {
        return endBy(s -> s.iterator());
    }

    /**
     * Produce ender with transform to {@code lt.lb.commons.iteraton.ReadOnlyIterator} action
     *
     * @param <A>
     * @return
     */
    public <A> StreamMapperEnder<T, Z, ReadOnlyIterator<Z>> toReadOnlyIterator() {
        return endBy(s -> ReadOnlyIterator.of(s));
    }

    /**
     * Produce ender with collect to {@code Set} action
     *
     * @param <A>
     * @return
     */
    public <A> StreamMapperEnder<T, Z, Set<Z>> collectToSet() {
        return endBy(s -> s.collect(Collectors.toSet()));
    }

    /**
     * Produce ender with min action
     *
     * @param comparator
     * @return
     */
    public StreamMapperEnder<T, Z, Optional<Z>> min(Comparator<? super Z> comparator) {
        return endBy(s -> s.min(comparator));
    }

    /**
     * Produce ender with max action
     *
     * @param comparator
     * @return
     */
    public StreamMapperEnder<T, Z, Optional<Z>> max(Comparator<? super Z> comparator) {
        return endBy(s -> s.max(comparator));
    }

    /**
     * Produce ender with count action
     *
     * @return
     */
    public StreamMapperEnder<T, Z, Long> count() {
        return endBy(s -> s.count());
    }

    /**
     * Produce ender with anyMatch action
     *
     * @param predicate
     * @return
     */
    public StreamMapperEnder<T, Z, Boolean> anyMatch(Predicate<? super Z> predicate) {
        return endBy(s -> s.anyMatch(predicate));
    }

    /**
     * Produce ender with allMatch action
     *
     * @param predicate
     * @return
     */
    public StreamMapperEnder<T, Z, Boolean> allMatch(Predicate<? super Z> predicate) {
        return endBy(s -> s.allMatch(predicate));
    }

    /**
     * Produce ender with noneMatch action
     *
     * @param predicate
     * @return
     */
    public StreamMapperEnder<T, Z, Boolean> noneMatch(Predicate<? super Z> predicate) {
        return endBy(s -> s.noneMatch(predicate));
    }

    /**
     * Produce ender with findFirst action
     *
     * @return
     */
    public StreamMapperEnder<T, Z, Optional<Z>> findFirst() {
        return endBy(s -> s.findFirst());
    }

    /**
     * Produce ender with findAny action
     *
     * @return
     */
    public StreamMapperEnder<T, Z, Optional<Z>> findAny() {
        return endBy(s -> s.findAny());
    }

    /**
     * Mapper composition
     *
     * @param <R>
     * @param sm
     * @return
     */
    public <R> StreamMapper<T, R> thenCombine(StreamMapper<Z, R> sm) {

        StreamMapper streamDecorator = new StreamMapper();
        streamDecorator.after = sm;
        streamDecorator.parent = this;
        return streamDecorator;

    }

    /**
     * Custom stream decorator
     *
     * @param fun
     * @return
     */
    public StreamMapper<T, Z> then(Function<Stream<Z>, Stream<Z>> fun) {

        StreamMapper streamDecorator = new StreamMapper(this.decs.size() + 1);
        streamDecorator.decs.addAll(this.decs);
        streamDecorator.decs.add(fun);
        streamDecorator.flatmapper = this.flatmapper;
        streamDecorator.mapper = this.mapper;
        streamDecorator.parent = this.parent;
        streamDecorator.after = this.after;
        return streamDecorator;
    }

    /**
     * Apply stream mapper decorator
     *
     * @param <R>
     * @param func
     * @return
     */
    public <R> StreamMapper<T, R> apply(Function<StreamMapper<T, Z>, StreamMapper<T, R>> func) {
        return func.apply(this);
    }

    /**
     * Apply stream mapper decorators without mapping
     *
     * @param funcs
     * @return
     */
    public StreamMapper<T, Z> applyAll(Function<StreamMapper<T, Z>, StreamMapper<T, Z>>... funcs) {
        StreamMapper<T, Z> me = this;
        for (Function<StreamMapper<T, Z>, StreamMapper<T, Z>> fun : funcs) {
            me = fun.apply(me);
        }
        return me;
    }

    /**
     * Apply stream decorator with temporary mapping. Can reuse predicates on
     * the same type that can be achieved by mapping current type.
     *
     * @param <R>
     * @param mapper
     * @param predicate
     * @return
     */
    public <R> StreamMapper<T, Z> filterWhen(Function<? super Z, ? extends R> mapper, Predicate<? super R> predicate) {
        return filter(s -> predicate.test(mapper.apply(s)));
    }

    /**
     * Create a custom ender
     *
     * @param <R>
     * @param ender
     * @return
     */
    public <R> StreamMapperEnder<T, Z, R> endBy(Function<Stream<Z>, R> ender) {
        return new StreamMapperEnder<>(this, ender);
    }

    /**
     * Create a custom ender with no return value
     *
     * @param ender
     * @return
     */
    public StreamMapperEnder<T, Z, Void> endByVoid(Consumer<Stream<Z>> ender) {

        return new StreamMapperEnder<>(this, s -> {
            ender.accept(s);
            return null;
        });
    }

    /**
     * Converts iterable to Stream. If null, return empty stream;
     *
     * @param <T>
     * @param iterable
     * @return
     */
    public static <T> Stream<T> fromIterable(Iterable<T> iterable) {
        return Optional.ofNullable(iterable)
                .map(s -> s.spliterator())
                .map(s -> StreamSupport.stream(s, false)).orElseGet(Stream::empty);
    }

    /**
     * Converts iterator to Stream. If null, return empty stream;
     *
     * @param <T>
     * @param iterator
     * @return
     */
    public static <T> Stream<T> fromIterator(Iterator<T> iterator) {
        return Optional.ofNullable(iterator)
                .map(s -> Spliterators.spliteratorUnknownSize(s, 0))
                .map(s -> StreamSupport.stream(s, false)).orElseGet(Stream::empty);
    }

    /**
     * Converts array to Stream. If null, return empty stream;
     *
     * @param <T>
     * @param array
     * @return
     */
    public static <T> Stream<T> fromArray(T[] array) {
        return Optional.ofNullable(array)
                .map(s -> Arrays.stream(s)).orElseGet(Stream::empty);
    }

}
