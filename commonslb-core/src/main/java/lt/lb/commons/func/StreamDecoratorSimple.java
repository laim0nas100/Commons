package lt.lb.commons.func;

import java.util.ArrayDeque;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public class StreamDecoratorSimple<T> extends StreamDecorator<T,T>{

    private ArrayDeque<Function<Stream<T>, Stream<T>>> decs;

    public StreamDecoratorSimple() {
        this(1);
    }

    private StreamDecoratorSimple(int size) {
        decs = new ArrayDeque<>(size);
    }

    public Stream<T> decorate(Stream<T> stream) {

        for (Function<Stream<T>, Stream<T>> fun : decs) {
            stream = fun.apply(stream);
        }
        return stream;
    }

    @Override
    public <R> StreamDecorator<T, R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <R> StreamDecorator<T, R> map(Function<? super T, ? extends R> mapper) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public StreamDecoratorSimple<T> then(Function<Stream<T>, Stream<T>> fun) {

        StreamDecoratorSimple<T> streamDecorator = new StreamDecoratorSimple<>(this.decs.size() + 1);
        streamDecorator.decs.addAll(this.decs);
        streamDecorator.decs.add(fun);
        return streamDecorator;
    }


}
