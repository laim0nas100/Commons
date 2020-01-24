package lt.lb.commons.func;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * Stream decoration might end with terminal operation, which this object holds.
 * Specifically, this object holds terminal operations that has no result.
 *
 * @author laim0nas100
 * @param <T> stream source type
 * @param <Z> stream result type
 */
public class StreamMapperEnderVoid<T, Z> extends StreamMapperEnder<T, Z, Void> {

    StreamMapperEnderVoid(StreamMapper<T, Z> mapper, Consumer<Stream<Z>> ender) {
        super(mapper, st -> {
            ender.accept(st);
            return null;
        });
    }

}
