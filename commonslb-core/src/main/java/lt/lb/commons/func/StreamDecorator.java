package lt.lb.commons.func;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public abstract class StreamDecorator<T,R> {
   

    public abstract StreamDecorator<T,T> then(Function<Stream<T>, Stream<T>> fun);

    public StreamDecorator<T,T> filter(Predicate<? super T> predicate) {
        return then(s -> s.filter(predicate));
    }

    public StreamDecorator<T,T> sorted(Comparator<? super T> comparator) {
        return then(s -> s.sorted(comparator));
    }

    public StreamDecorator<T,T> sorted() {
        return then(s -> s.sorted());
    }

    public StreamDecorator<T,T> distinct() {
        return then(s -> s.distinct());
    }

    public StreamDecorator<T,T> unordered() {
        return then(s -> s.unordered());
    }

    public StreamDecorator<T,T> parallel() {
        return then(s -> s.parallel());
    }

    public StreamDecorator<T,T> sequential() {
        return then(s -> s.sequential());
    }

    public StreamDecorator<T,T> limit(long maxSize) {
        return then(s -> s.limit(maxSize));
    }
    
    public abstract <R> StreamDecorator<T, R> map(Function<? super T, ? extends R> mapper);

    public abstract <R> StreamDecorator<T, R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);
}
