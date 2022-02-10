package lt.lb.commons.iteration.streams.extendable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.iteration.streams.extendable.StreamExtension.StreamExtensionsAll;

/**
 *
 * @author laim0nas100
 * @param <X>
 */
public class SimpleStream<X> implements StreamExtensionsAll<X, SimpleStream<X>> {

    protected Stream<X> stream;
    protected boolean remake;

    public SimpleStream(Collection<X> collection) {
        this(collection, false);
    }

    public SimpleStream(Collection<X> collection, boolean remaking) {
        this(Objects.requireNonNull(collection, "Supplied collection is nulll").stream(), remaking);
    }

    public SimpleStream(Stream<X> stream) {
        this(stream, false);
    }

    public SimpleStream(Stream<X> stream, boolean remaking) {
        this.stream = Objects.requireNonNull(stream, "Supplied stream is null");
        this.remake = remaking;
    }

    @Override
    public Stream<X> delegate() {
        return stream;
    }

    @Override
    public SimpleStream<X> me() {
        return this;
    }

    @Override
    public <R> SimpleStream<R> select(Class<R> cls) {
        return F.cast(StreamExtensionsAll.super.select(cls));
    }

    @Override
    public <R> SimpleStream<R> flatMap(Function<? super X, ? extends Stream<? extends R>> mapper) {
        return F.cast(StreamExtensionsAll.super.flatMap(mapper));
    }

    @Override
    public <R> SimpleStream<R> map(Function<? super X, ? extends R> mapper) {
        return F.cast(StreamExtensionsAll.super.map(mapper));
    }

    @Override
    public <R> SimpleStream<R> reconstruct(Stream<R> modifiedStream) {
        if (remake) {
            return new SimpleStream<>(modifiedStream, remake);
        }
        SimpleStream<R> ss = (SimpleStream) this;
        ss.stream = modifiedStream;
        return ss;
    }
}
