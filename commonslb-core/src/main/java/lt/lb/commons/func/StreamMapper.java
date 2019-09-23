package lt.lb.commons.func;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 * @param <T> source type
 * @param <Z> result type
 */
public class StreamMapper<T, Z> {

    protected ArrayList<Function<Stream<Z>, Stream<Z>>> decs;
    protected Function<Object, Z> mapper;
    protected Function<Object, Stream<Z>> flatmapper;
    protected StreamMapper<Object, Z> after;
    protected StreamMapper<T, Object> parent;

    public static <E> StreamDecorator<E> of() {
        return new StreamDecorator<>();
    }
    
    public static <E> StreamDecorator<E> of(Class<E> cls) {
        return new StreamDecorator<>();
    }
    
    public static class StreamDecorator<E> extends StreamMapper<E, E> {
    };

    public StreamMapper() {
        this(0);
    }

    private StreamMapper(int size) {
        decs = new ArrayList<>(size);
        
    }

    public Stream<Z> decorate(Stream<T> stream) {

        if (parent != null) {
            Stream decorated = parent.decorate(stream);
            if (mapper != null) {
                return applyDecs(decorated.map(mapper));
            } else if (flatmapper != null){
                return applyDecs(decorated.flatMap(flatmapper));
            } else{
                return applyDecs(after.decorate(decorated));
            }
        } else {

            //this should be the same then
            return applyDecs((Stream<Z>) stream);
        }

    }

    protected Stream<Z> applyDecs(Stream<Z> stream) {
        for (Function<Stream<Z>, Stream<Z>> fun : decs) {
            stream = fun.apply(stream);
        }
        return stream;
    }

    public StreamMapper<T, Z> filter(Predicate<Z> predicate) {
        return then(s -> s.filter(predicate));
    }

    public StreamMapper<T, Z> sorted(Comparator<Z> comparator) {
        return then(s -> s.sorted(comparator));
    }

    public StreamMapper<T, Z> sorted() {
        return then(s -> s.sorted());
    }

    public StreamMapper<T, Z> distinct() {
        return then(s -> s.distinct());
    }

    public StreamMapper<T, Z> unordered() {
        return then(s -> s.unordered());
    }

    public StreamMapper<T, Z> parallel() {
        return then(s -> s.parallel());
    }

    public StreamMapper<T, Z> sequential() {
        return then(s -> s.sequential());
    }

    public StreamMapper<T, Z> limit(long maxSize) {
        return then(s -> s.limit(maxSize));
    }

    public <R> StreamMapper<T, R> map(Function<? super Z, ? extends R> mapper) {

        StreamMapper streamDecorator = new StreamMapper();
        streamDecorator.mapper = mapper;
        streamDecorator.parent = this;
        return streamDecorator;
    }

    public <R> StreamMapper<T, R> flatMap(Function<? super Z, ? extends Stream<? extends R>> mapper) {

        StreamMapper streamDecorator = new StreamMapper();
        streamDecorator.flatmapper = mapper;
        streamDecorator.parent = this;
        return streamDecorator;

    }
    
    public <R> StreamMapper<T, R> thenApply(StreamMapper<Z,R> sm){
        
        StreamMapper streamDecorator = new StreamMapper();
        streamDecorator.after = sm;
        streamDecorator.parent = this;
        return streamDecorator;
        
    }

    public StreamMapper<T, Z> then(Function<Stream<Z>, Stream<Z>> fun) {

        StreamMapper streamDecorator = new StreamMapper(this.decs.size() + 1);
        streamDecorator.decs.addAll(this.decs);
        streamDecorator.decs.add(fun);
        streamDecorator.flatmapper = this.flatmapper;
        streamDecorator.mapper = this.mapper;
        streamDecorator.parent = this.parent;
        streamDecorator.after = this.after;
        return streamDecorator;
    }

}
