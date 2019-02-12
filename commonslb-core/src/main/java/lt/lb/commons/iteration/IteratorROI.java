package lt.lb.commons.iteration;

import java.util.Iterator;

/**
 *
 * @author laim0nas100
 */
public class IteratorROI<T> extends BaseROI<T> {

    protected Iterator<T> iter;

    public IteratorROI(Iterator<T> iter) {
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public T next() {
        index++;
        return setCurrent(iter.next());
    }


}
