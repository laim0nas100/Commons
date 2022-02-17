package lt.lb.commons.iteration.streams;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamAppenders<X, M extends DecoratableStream<X, M>> extends StreamExtension<X, M> {

    /**
     * Appends given stream to the end of current stream
     *
     * @param stream
     * @return
     */
    public default M append(Stream<? extends X> stream) {
        Objects.requireNonNull(stream, "Stream must not be null");
        return me().intermediate(str -> Stream.concat(str, stream));
    }

    /**
     * Appends given stream created from value to the end of current stream
     *
     * @param val
     * @return
     */
    public default M append(X val) {
        return append(Stream.of(val));
    }

    /**
     * Appends given stream created from values to the end of current stream
     *
     * @param values
     * @return
     */
    public default M append(X... values) {
        return append(Stream.of(values));
    }

    /**
     * Appends stream created from {@link Collection} to the end of current
     * stream
     *
     * @param collection
     * @return
     */
    public default M append(Collection<? extends X> collection) {
        Objects.requireNonNull(collection, "Collection must not be null");
        return append(collection.stream());
    }

    /**
     * Appends stream created from given {@link Iterable} to the end of current
     * stream
     *
     * @param iterable
     * @return
     */
    public default M append(Iterable<? extends X> iterable) {
        Objects.requireNonNull(iterable, "Iterable must not be null");
        return append(MakeStream.from(iterable, false));
    }

    /**
     * Appends stream created from given {@link Iterator} to the end of current
     * stream
     *
     * @param iterator
     * @return
     */
    public default M append(Iterator<? extends X> iterator) {
        Objects.requireNonNull(iterator, "Iterator must not be null");
        return append(MakeStream.from(iterator, false));
    }

    /**
     * Appends given stream to the start of current stream
     *
     * @param stream
     * @return
     */
    public default M prepend(Stream<? extends X> stream) {
        Objects.requireNonNull(stream, "Stream must not be null");
        return me().intermediate(str -> Stream.concat(stream, str));
    }

    /**
     * Appends given stream created from value to the start of current stream
     *
     * @param val
     * @return
     */
    public default M prepend(X val) {
        return prepend(Stream.of(val));
    }

    /**
     * Appends given stream created from values to the start of current stream
     *
     * @param values
     * @return
     */
    public default M prepend(X... values) {
        return prepend(Stream.of(values));
    }

    /**
     * Appends stream created from {@link Collection} to the start of current
     * stream
     *
     * @param collection
     * @return
     */
    public default M prepend(Collection<? extends X> collection) {
        Objects.requireNonNull(collection, "Collection must not be null");
        return prepend(collection.stream());
    }

    /**
     * Appends stream created from given {@link Iterable} to the start of
     * current stream
     *
     * @param iterable
     * @return
     */
    public default M prepend(Iterable<? extends X> iterable) {
        Objects.requireNonNull(iterable, "Iterable must not be null");
        return prepend(MakeStream.from(iterable,false));
    }

    /**
     * Appends stream created from given {@link Iterator} to the start of
     * current stream
     *
     * @param iterator
     * @return
     */
    public default M prepend(Iterator<? extends X> iterator) {
        Objects.requireNonNull(iterator, "Iterator must not be null");
        return prepend(MakeStream.from(iterator, false));
    }

}
