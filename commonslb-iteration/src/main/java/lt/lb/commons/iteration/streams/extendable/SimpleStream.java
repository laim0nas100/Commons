package lt.lb.commons.iteration.streams.extendable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import lt.lb.commons.iteration.streams.extendable.StreamExtension.StreamExtensionsAll;

/**
 *
 * @author laim0nas100
 * @param <X>
 */
public class SimpleStream<X> implements StreamExtensionsAll<X,SimpleStream<X>>  {

    protected Stream<X> stream;

    public SimpleStream(Stream<X> stream) {
        this.stream = Objects.requireNonNull(stream, "Supplied stream is null");
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
        return (SimpleStream) StreamExtensionsAll.super.select(cls);
    }

    @Override
    public <R> SimpleStream<R> flatMap(Function<? super X, ? extends Stream<? extends R>> mapper) {
        return (SimpleStream) StreamExtensionsAll.super.flatMap(mapper);
    }

    @Override
    public <R> SimpleStream<R> map(Function<? super X, ? extends R> mapper) {
        return (SimpleStream) StreamExtensionsAll.super.map(mapper);
    }

    @Override
    public <R> SimpleStream<R> reconstruct(Stream<R> modifiedStream) {
        SimpleStream<R> ss = (SimpleStream) this;
        ss.stream = modifiedStream;
        return ss;
    }

    
    public static void main(String[] args) {
         AtomicInteger generator = new AtomicInteger(0);
        SimpleStream<Integer> ss = new SimpleStream<>(Stream.generate(() -> generator.incrementAndGet()));

        SimpleStream<Number> select = ss.limit(12).select(Number.class);
        SimpleStream<String> map = select.map(m -> m + "");
        
//        map.forPairs((a,b)->{
//            System.out.println(a+"_"+b);
//        });
        
        map.forIndexed((index,b)->{
            System.out.println(index+"_"+b);
        });
//        System.out.println(map.toList());
//        System.out.println(map.toList());
        
        
    }
}
