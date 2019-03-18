package lt.lb.commons.iteration.impl;

import java.util.NoSuchElementException;
import java.util.Optional;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * @author laim0nas100
 */
public class CompositeROI<T> extends BaseROI<T> {

    private ReadOnlyIterator<T> currentIterator;
    private ReadOnlyIterator<ReadOnlyIterator<T>> rois;

    public CompositeROI(ReadOnlyIterator<ReadOnlyIterator<T>> rois) {
        this.rois = rois;
    }

    @Override
    public boolean hasNext() {
        ensureNewest();
        return currentIterator != null && currentIterator.hasNext();

    }

    @Override
    public T next() {
        if(!hasNext()){
            throw new NoSuchElementException("No next value");
        }
        this.index++;
        
        return setCurrent(currentIterator.next());
    }

    private void ensureNewest() {
        if (currentIterator == null || !currentIterator.hasNext()) {
            if (rois.hasNext()) {
                currentIterator = rois.getNext();
                while (!currentIterator.hasNext() && rois.hasNext()) {
                    currentIterator = rois.getNext();
                }
            }

        }
    }

}
