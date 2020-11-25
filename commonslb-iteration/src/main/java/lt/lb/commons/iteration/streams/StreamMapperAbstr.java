package lt.lb.commons.iteration.streams;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public abstract class StreamMapperAbstr<T, Z, R> {

    /**
     * Decorate and map a stream with decorators within this object
     *
     * @param stream
     * @return
     */
    public abstract R startingWith(Stream<T> stream);

    /**
     * Decorates stream returning decorated empty stream on null
     *
     * @param stream
     * @return
     */
    public R startingWithOpt(Stream<T> stream) {
        return startingWith(stream == null ? Stream.empty() : stream);
    }

    /**
     * Decorates stream returning decorated empty stream from iterable
     *
     * @param iterable
     * @return
     */
    public R startingWithOpt(Iterable<T> iterable) {
        return startingWith(StreamMapper.fromIterable(iterable));
    }

    /**
     * Decorates stream returning decorated empty stream from iterator
     *
     * @param iterator
     * @return
     */
    public R startingWithOpt(Iterator<T> iterator) {
        return startingWith(StreamMapper.fromIterator(iterator));
    }

    public R startingWithOpt(T[] array) {
        return startingWith(StreamMapper.fromArray(array));
    }

    /**
     * Decorates empty stream.Relevant if mapper has concat operations.
     *
     * @param values
     * @return
     */
    public R startingWith(T... values) {
        if (values.length == 0) {
            return startingWith(Stream.empty());
        } else {
            return startingWith(StreamMapper.fromArray(values));
        }
    }
}
