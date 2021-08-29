package lt.lb.commons.iteration.streams.extendable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamAppenders<X, M extends DelegatingStream<X, M>> extends StreamExtension<X, M> {

    public default M append(Stream<? extends X> stream) {
        Objects.requireNonNull(stream, "Stream must not be null");
        return me().intermediate(str -> Stream.concat(str, stream));
    }

    public default M append(X val) {
        return append(Stream.of(val));
    }

    public default M append(X... values) {
        return append(Stream.of(values));
    }

    public default M append(Iterable<? extends X> iterable) {
        Objects.requireNonNull(iterable, "Iterable must not be null");
        return append(fromIterable(iterable, false));
    }

    public default M append(Iterator<? extends X> iterator) {
        Objects.requireNonNull(iterator, "Iterator must not be null");
        return append(fromIterator(iterator, false));
    }

    public default M prepend(Stream<? extends X> stream) {
        Objects.requireNonNull(stream, "Stream must not be null");
        return me().intermediate(str -> Stream.concat(stream, str));
    }

    public default M prepend(X val) {
        return prepend(Stream.of(val));
    }

    public default M prepend(X... values) {
        return prepend(Stream.of(values));
    }

    public default M prepend(Iterable<? extends X> iterable) {
        Objects.requireNonNull(iterable, "Iterable must not be null");
        return prepend(fromIterable(iterable, false));
    }

    public default M prepend(Iterator<? extends X> iterator) {
        Objects.requireNonNull(iterator, "Iterator must not be null");
        return prepend(fromIterator(iterator, false));
    }

    /**
     * Converts {@link Iterable} to Stream.
     *
     * @param <T>
     * @param iterable
     * @param parallel
     * @return
     */
    public static <T> Stream<? extends T> fromIterable(Iterable<? extends T> iterable, boolean parallel) {
        Spliterator<? extends T> spliterator = iterable.spliterator();
        return StreamSupport.stream(spliterator, parallel);
    }

    /**
     * Converts {@link Iterator} to Stream.
     *
     * @param <T>
     * @param iterator
     * @param parallel
     * @return
     */
    public static <T> Stream<? extends T> fromIterator(Iterator<? extends T> iterator, boolean parallel) {
        if (!iterator.hasNext()) {
            return Stream.empty();
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), parallel);
    }

}
