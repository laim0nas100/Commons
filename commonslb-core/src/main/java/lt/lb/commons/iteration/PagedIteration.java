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
public interface PagedIteration<PageInfo, T> extends Iterable {

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

    /**
     * Construct iterator of pages, if such need arises
     * @return
     */
    public default Iterator<Iterator<T>> toPagedIterators() {
        return new Iterator<Iterator<T>>() {
            PageInfo lastPage = null;
            boolean lastPageUsed = false;
            boolean firstGot = false;

            @Override
            public boolean hasNext() {
                if (!firstGot) {

                    lastPage = getFirstPage();
                    firstGot = true;
                    return true;
                }
                return !lastPageUsed || hasNextPage(lastPage);
            }

            @Override
            public Iterator<T> next() {

                if (hasNext()) {
                    if (!lastPageUsed) {
                        Iterator<T> items = getItems(lastPage);
                        lastPageUsed = true;
                        return items;
                    } else {
                        lastPage = getNextPage(lastPage);
                        lastPageUsed = true;
                        return getItems(lastPage);
                    }
                } else {
                    throw new NoSuchElementException("No next value");
                }
            }
        };
    }

    /**
     * Construct iterator of items based on this paged access
     * @return 
     */
    public default Iterator<T> iterator() {

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
