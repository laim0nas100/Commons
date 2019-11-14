package lt.lb.commons.iteration;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Generalized paged iteration
 *
 * @author laim0nas100
 * @param <PageInfo> how to get next page and how to get items
 * @param <T> items
 */
public interface PagedIteration<PageInfo, T> {

    /**
     * First page
     *
     * @return
     */
    public PageInfo getFirstPage();

    /**
     * Getting items from page info
     *
     * @param info
     * @return
     */
    public Iterator<T> getItems(PageInfo info);

    /**
     * Getting next page info from given page info
     *
     * @param info
     * @return
     */
    public PageInfo getNextPage(PageInfo info);

    public boolean hasNextPage(PageInfo info);

    public default Iterator<T> toIterator() {

        return new Iterator<T>() {
            Iterator<T> current = null;
            PageInfo currentPage = null;
            boolean endReached = false;
            boolean firstGot = false;

            @Override
            public boolean hasNext() {
                if (endReached) {
                    return false;
                }
                if (!firstGot) {

                    currentPage = getFirstPage();
                    current = getItems(currentPage);
                    firstGot = true;
                    return hasNext();
                }
                if (current.hasNext()) {
                    return true;
                } else {
                    while (hasNextPage(currentPage)) {
                        currentPage = getNextPage(currentPage);
                        current = getItems(currentPage);
                        if (current.hasNext()) {
                            return true;
                        }
                    }
                    endReached = true;
                    return false;
                }
            }

            @Override
            public T next() {
                if (hasNext()) {
                    return current.next();
                } else {
                    throw new NoSuchElementException("No next value");
                }
            }
        };
    }
}
