package lt.lb.commons.iteration.impl;

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
        return currentIterator.hasNext();

    }

    @Override
    public T next() {
        this.index++;
        ensureNewest();
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



