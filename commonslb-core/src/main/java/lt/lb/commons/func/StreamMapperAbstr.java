package lt.lb.commons.func;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public abstract class StreamMapperAbstr<T,Z,R> {
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
        return startingWith(StreamMappers.fromIterable(iterable));
    }

    /**
     * Decorates stream returning decorated empty stream from iterator
     *
     * @param iterator
     * @return
     */
    public R startingWithOpt(Iterator<T> iterator) {
        return startingWith(StreamMappers.fromIterator(iterator));
    }
    
    /**
     * Decorates empty stream. Relevant if mapper has concat operations.
     * @return 
     */
    public R startingWithOpt(){
        return startingWith(null);
    }
}
