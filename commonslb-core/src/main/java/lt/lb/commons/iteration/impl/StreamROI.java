package lt.lb.commons.iteration.impl;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public class StreamROI<T> extends BaseROI<T> implements Consumer<T> {

    protected Spliterator<T> spliterator;
    protected Stream<T> stream;
    protected boolean valueReady;
    protected T nextValue;
//    protected Tuple<Boolean, T> nextItem = new Tuple<>(false, null); // <Is Present, Next item> must be mutable, so no prmitives

    public StreamROI(Stream<T> stream) {
        Objects.requireNonNull(stream);
        this.stream = stream;
        this.spliterator = stream.spliterator();
    }

    @Override
    public void accept(T t) {
        valueReady = true;
        nextValue = t;
    }

    @Override
    public boolean hasNext() {
        if (!valueReady) {
            return spliterator.tryAdvance(this);
        }
        return valueReady;
    }

    @Override
    public T next() {
        if (!valueReady) { // item not present, try to get
            if (!spliterator.tryAdvance(this)) {
                throw new NoSuchElementException();
            }
        }
        valueReady = false;
        index++;
        return setCurrent(nextValue);
    }

    @Override
    public void close() {
        stream.close();
    }

}
