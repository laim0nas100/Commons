package lt.lb.commons.iteration;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Generalized paged iteration
 *
 * @author laim0nas100
 * @param <PageInfo> how to get next page and how to get items
 * @param <T> items
 */
public interface PagedIteration<PageInfo, T> extends Iterable<T> {

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

    public static class SimplePagesIterator<PageInfo, T> implements Iterator<Iterator<T>> {

        public SimplePagesIterator(PagedIteration<PageInfo, T> paged) {
            this.paged = Objects.requireNonNull(paged, "PagedIteration is null");
        }

        protected PageInfo lastPage = null;
        protected boolean lastPageUsed = false;
        protected boolean firstGot = false;

        protected PagedIteration<PageInfo, T> paged;

        @Override
        public boolean hasNext() {
            if (!firstGot) {

                lastPage = paged.getFirstPage();
                firstGot = true;
                return true;
            }
            return !lastPageUsed || paged.hasNextPage(lastPage);
        }

        @Override
        public Iterator<T> next() {

            if (hasNext()) {
                if (!lastPageUsed) {
                    Iterator<T> items = paged.getItems(lastPage);
                    lastPageUsed = true;
                    return items;
                } else {
                    lastPage = paged.getNextPage(lastPage);
                    lastPageUsed = true;
                    return paged.getItems(lastPage);
                }
            } else {
                throw new NoSuchElementException("No next value");
            }
        }
    }

    /**
     * Construct iterator of pages, if such need arises
     *
     * @return
     */
    public default Iterator<Iterator<T>> toPagedIterators() {
        return new CachedNextCheckIterator<>(new SimplePagesIterator<>(this));
    }

    public static class SimplePagedIterator<T, PageInfo> implements Iterator<T> {

        protected Iterator<T> current = null;
        protected PageInfo currentPage = null;
        protected boolean endReached = false;
        protected boolean firstGot = false;

        protected PagedIteration<PageInfo, T> paged;

        public SimplePagedIterator(PagedIteration<PageInfo, T> paged) {
            this.paged = Objects.requireNonNull(paged, "PagedIteration is null");
        }

        @Override
        public boolean hasNext() {
            if (endReached) {
                return false;
            }
            if (!firstGot) {

                currentPage = paged.getFirstPage();
                current = paged.getItems(currentPage);
                firstGot = true;
                return hasNext();
            }
            if (current.hasNext()) {
                return true;
            } else {
                while (paged.hasNextPage(currentPage)) {
                    currentPage = paged.getNextPage(currentPage);
                    current = paged.getItems(currentPage);
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
    }

    /**
     * Construct iterator of items based on this paged access
     *
     * @return
     */
    @Override
    public default Iterator<T> iterator() {
        return new CachedNextCheckIterator<>(new SimplePagedIterator<>(this));
    }
}
