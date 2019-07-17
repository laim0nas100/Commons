package lt.lb.commons.iteration.impl;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lt.lb.commons.containers.tuples.Tuple;

/**
 *
 * @author laim0nas100
 */
public class StreamROI<T> extends BaseROI<T>{

    protected Spliterator<T> spliterator;
    protected Stream<T> stream;
    protected Tuple<Boolean,T> nextItem = new Tuple<>(false, null); // <Is Present, Next item> must be mutable, so no prmitives
    protected Consumer<T> cons = (item) -> {
        nextItem.setG2(item);
        nextItem.setG1(true);
    };

    public StreamROI(Stream<T> stream) {
        this.stream = stream;
        this.spliterator = stream.spliterator();
    }

    @Override
    public boolean hasNext() {
        if (!nextItem.g1) {
            spliterator.tryAdvance(cons);
        }
        return nextItem.g1;
    }

    @Override
    public T next() {
        if (!nextItem.g1) { // item not present, try to get
            if (!spliterator.tryAdvance(cons)) {
                throw new NoSuchElementException();
            }
        }
        T next = nextItem.g2;
        nextItem.setG1(false);
        index++;
        return setCurrent(next);
    }

    @Override
    public void close(){
        stream.close();
    }

}
