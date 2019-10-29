package lt.lb.commons.func;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import static lt.lb.commons.func.StreamMapper.fromIterable;
import static lt.lb.commons.func.StreamMapper.fromIterator;

/**
 *
 * Stream decoration might end with terminal operation, which this object holds
 *
 * @author laim0nas100
 */
public class StreamMapperEnder<T, Z, R> {

    private final StreamMapper<T, Z> mapper;
    private final Function<Stream<Z>, R> ender;

    StreamMapperEnder(StreamMapper<T, Z> mapper, Function<Stream<Z>, R> ender) {
        this.mapper = Objects.requireNonNull(mapper);
        this.ender = Objects.requireNonNull(ender);
    }

    public StreamMapper<T, Z> getMapper() {
        return mapper;
    }

    /**
     * Decorate and map a stream with decorators within this object
     *
     * @param stream
     * @return
     */
    public R startingWith(Stream<T> stream) {
        Objects.requireNonNull(stream, "Given stream was null");
        return ender.apply(mapper.decorate(stream));

    }

    /**
     * Decorates stream returning decorated empty stream on null
     *
     * @param stream
     * @return
     */
    public R startingWithEmpty(Stream<T> stream) {
        return startingWith(stream == null ? Stream.empty() : stream);
    }

    /**
     * Decorates stream returning decorated empty stream from iterable
     *
     * @param iterable
     * @return
     */
    public R startingWithEmpty(Iterable<T> iterable) {
        return startingWith(fromIterable(iterable));
    }

    /**
     * Decorates stream returning decorated empty stream from iterator
     *
     * @param iterator
     * @return
     */
    public R startingWithEmpty(Iterator<T> iterator) {
        return startingWith(fromIterator(iterator));
    }

}
