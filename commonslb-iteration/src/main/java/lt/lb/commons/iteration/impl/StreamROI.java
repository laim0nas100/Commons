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
        if(closed){
            return false;
        }
        if (!valueReady) {
            return spliterator.tryAdvance(this);
        }
        return valueReady;
    }

    @Override
    public T next() {
        assertClosed();
        if (!valueReady) { // item not present, try to get
            if (!spliterator.tryAdvance(this)) {
                throw new NoSuchElementException();
            }
        }
        valueReady = false;
        return setCurrentInc(nextValue);
    }

    @Override
    public void close() {
        super.close();
        stream.close();
    }

}
