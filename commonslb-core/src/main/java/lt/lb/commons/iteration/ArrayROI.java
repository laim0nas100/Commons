package lt.lb.commons.iteration;

/**
 *
 * @author laim0nas100
 */
public class ArrayROI<T> extends BaseROI<T> implements ReadOnlyBidirectionalIterator<T> {

    protected T[] array;

    public ArrayROI(T... array) {
        this.array = array;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public T previous() {
        T val = array[--index];
        return setCurrent(val);
    }

    @Override
    public boolean hasNext() {
        return 1 + index < array.length - 1;
    }

    @Override
    public T next() {
        T val = array[++index];
        return setCurrent(val);
    }

}
