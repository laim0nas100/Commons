package lt.lb.commons.func;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * Stream decoration might end with terminal operation, which this object holds
 *
 * @author laim0nas100
 * @param <T> stream source type
 * @param <Z> stream result type
 * @param <R> result
 */
public class StreamMapperEnder<T, Z, R> extends StreamMapperAbstr<T, Z, R> {
    
    protected final StreamMapper<T, Z> mapper;
    protected final Function<Stream<Z>, R> ender;

    StreamMapperEnder(StreamMapper<T, Z> mapper, Function<Stream<Z>, R> ender) {
        this.mapper = Objects.requireNonNull(mapper);
        this.ender = Objects.requireNonNull(ender);
    }

    public Function<Stream<Z>, R> getEnder() {
        return ender;
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
    @Override
    public R startingWith(Stream<T> stream) {
        Objects.requireNonNull(stream, "Given stream was null");
        return ender.apply(mapper.decorate(stream));

    }

}
