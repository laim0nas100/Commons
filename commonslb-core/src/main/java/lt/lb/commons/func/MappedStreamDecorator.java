package lt.lb.commons.func;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public class MappedStreamDecorator<Src, Cur, Rez> extends StreamDecorator<Cur, Rez> {

    private StreamDecoratorSimple<Cur> simple = new StreamDecoratorSimple<>();
    private MappedStreamDecorator<Src, Cur, Rez> parent;
    private Function<? super Cur, ? extends Rez> mapper;

    @Override
    public <R> StreamDecorator<Cur, R> flatMap(Function<? super Cur, ? extends Stream<? extends R>> mapper) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <R> StreamDecorator<Cur, R> map(Function<? super Cur, ? extends R> mapper) {

        MappedStreamDecorator<Src, Cur ,R> newMap = new MappedStreamDecorator<>();
        MappedStreamDecorator<Src, Cur, Rez> aThis = this;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StreamDecorator<Cur, Cur> then(Function<Stream<Cur>, Stream<Cur>> fun) {
        return simple.then(fun);
    }

}
