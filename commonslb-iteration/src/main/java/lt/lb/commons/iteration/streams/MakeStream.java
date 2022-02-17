package lt.lb.commons.iteration.streams;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * Static {@link SimpleStream} constructors.
 *
 * @author laim0nas100
 */
public abstract class MakeStream {

    /**
     * Converts given {@link Optional} to Stream.
     *
     * @param <T>
     * @param optional
     * @return
     */
    public static <T> SimpleStream<T> fromOptional(Optional<? extends T> optional) {
        Objects.requireNonNull(optional, "Optional must not be null");
        return fromNullable(optional.orElse(null));
    }

    /**
     * Converts given values to Stream.
     *
     * @param <T>
     * @param values
     * @return
     */
    public static <T> SimpleStream<T> fromValues(T... values) {
        Objects.requireNonNull(values, "values must not be null");
        switch (values.length) {
            case 0:
                return new SimpleStream<>(Stream.empty());
            case 1:
                return new SimpleStream<>(Stream.of(values[0]));
            default:
                return new SimpleStream<>(Arrays.stream(values));
        }
    }

    /**
     * Converts given value to Stream. Returns empty stream if value is null.
     *
     * @param <T>
     * @param val
     * @return
     */
    public static <T> SimpleStream<T> fromNullable(T val) {
        return val == null ? new SimpleStream<>(Stream.empty()) : new SimpleStream<>(Stream.of(val));
    }

    /**
     * Converts {@link Collection} to Stream.
     *
     * @param <T>
     * @param collection
     * @return
     */
    public static <T> SimpleStream<T> from(Collection<T> collection) {
        return from(collection, false);
    }

    /**
     * Converts {@link Collection} to Stream.
     *
     * @param <T>
     * @param collection
     * @param parallel
     * @return
     */
    public static <T> SimpleStream<T> from(Collection<T> collection, boolean parallel) {
        Objects.requireNonNull(collection, "Collection must not be null");
        return new SimpleStream<>(parallel ? collection.parallelStream() : collection.stream());
    }

    /**
     * Converts {@link Iterable} to Stream.
     *
     * @param <T>
     * @param iterable
     * @return
     */
    public static <T> SimpleStream<T> from(Iterable<T> iterable) {
        return from(iterable, false);
    }

    /**
     * Converts {@link Iterable} to Stream.
     *
     * @param <T>
     * @param iterable
     * @param parallel
     * @return
     */
    public static <T> SimpleStream<T> from(Iterable<T> iterable, boolean parallel) {
        Objects.requireNonNull(iterable, "Iterable must not be null");
        return new SimpleStream<>(StreamSupport.stream(iterable.spliterator(), parallel));
    }

    /**
     * Converts {@link Iterator} to Stream.
     *
     * @param <T>
     * @param iterator
     * @return
     */
    public static <T> SimpleStream<T> from(Iterator<T> iterator) {
        return from(iterator, false);
    }

    /**
     * Converts {@link Iterator} to Stream.
     *
     * @param <T>
     * @param iterator
     * @param parallel
     * @return
     */
    public static <T> SimpleStream<T> from(Iterator<T> iterator, boolean parallel) {
        Objects.requireNonNull(iterator, "Iterator must not be null");
        if (!iterator.hasNext()) {
            return new SimpleStream<>(Stream.empty());
        }
        return new SimpleStream<>(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), parallel));
    }

    /**
     * Converts array to Stream.
     *
     * @param <T>
     * @param array
     * @return
     */
    public static <T> SimpleStream<T> from(T[] array) {
        Objects.requireNonNull(array, "Array must not be null");
        switch (array.length) {
            case 0:
                return new SimpleStream<>(Stream.empty());
            case 1:
                return new SimpleStream<>(Stream.of(array[0]));
            default:
                return new SimpleStream<>(Arrays.stream(array));
        }
    }
}
