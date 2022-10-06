package experimental.jpa.lazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.prebuiltcollections.readonly.ReadOnlyList;

/**
 *
 * @author laim0nas100
 */
public class LazySearchResult<ID, T> implements ReadOnlyList<T> {

    private LazyLoadResult<ID> ids;
    private final LazyLoader<ID, T> loader;
    private final LazyLoaderIds<ID, T> exLoader;
    private int size = 0;

    private int pageIndex = 0;
    private final int pageSize;
    private final List<T> page = new ArrayList<>();

    public LazySearchResult(LazyLoader<ID, T> loader, int pageSize) {
        this.loader = loader;
        this.exLoader = null;
        this.pageSize = pageSize;
    }

    public LazySearchResult(LazyLoadResult<ID> ids, LazyLoader<ID, T> loader, int pageSize) {
        this.ids = ids;
        this.loader = loader;
        this.exLoader = null;
        this.pageSize = pageSize;
    }

    public LazySearchResult(int size, LazyLoaderIds<ID, T> loader, int pageSize) {
        this.size = size;
        this.loader = loader;
        this.exLoader = loader;
        this.pageSize = pageSize;
    }

    public static <ID, T> LazySearchResult<ID, T> empty() {
        return new LazySearchResult<>(F.cast(LazyLoadResult.emptyList), (params) -> ImmutableCollections.listOf(), 10);
    }

    @Override
    public int size() {
        return (this.exLoader != null) ? this.size : this.ids.getIds().size();
    }

    @Override
    public boolean isEmpty() {
        return (this.exLoader != null) ? ((this.size == 0)) : !this.ids.exists();
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    private void loadPage(int index) {
        this.page.clear();
        int pageNumber = index / this.pageSize;
        this.pageIndex = pageNumber * this.pageSize;
        if (this.exLoader != null) {
            this.ids = this.exLoader.loadIds(LazyLoadContext.list(pageIndex, pageSize));
//            this.ids = this.exLoader.loadIds(this.pageIndex);
            this.page.addAll(this.loader.lazyLoad(ids));
        } else {
            int from = pageIndex;
            int to = Math.min(this.pageIndex + this.pageSize, this.ids.size());
            List<T> loaded = this.loader.lazyLoad(ids.subresult(from, to));
            this.page.addAll(loaded);
        }
    }

    private void checkPage(int index) {
        if (this.page.isEmpty()) {
            loadPage(index);
            return;
        }
        if (index < this.pageIndex) {
            loadPage(index);
            return;
        }
        if (index >= this.pageIndex + this.page.size()) {
            loadPage(index);
        }
    }

    @Override
    public synchronized T get(int index) {
        checkPage(index);
        return this.page.get(index - this.pageIndex);
    }

    @Override
    public ListIterator<T> listIterator() {
        return new ListItr(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new ListItr(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        List<T> result = new ArrayList<>();
        for (int idx = fromIndex; idx < toIndex; idx++) {
            result.add(get(idx));
        }
        return Collections.unmodifiableList(result);
    }

    public List<ID> getIds() {
        return this.ids.getIds();
    }

    public int getSize() {
        return this.size;
    }

    public int getPageIndex() {
        return this.pageIndex;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public List<T> getPage() {
        return this.page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    private class Itr implements Iterator<T> {

        protected int cursor = 0;

        @Override
        public boolean hasNext() {
            return (this.cursor < LazySearchResult.this.size());
        }

        @Override
        public T next() {
            try {
                return LazySearchResult.this.get(this.cursor++);
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        private Itr() {
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class ListItr extends Itr implements ListIterator<T> {

        ListItr(int index) {
            this.cursor = index;
        }

        @Override
        public boolean hasPrevious() {
            return (this.cursor > 0);
        }

        @Override
        public T previous() {
            try {
                return LazySearchResult.this.get(cursor--);
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public int nextIndex() {
            return this.cursor;
        }

        @Override
        public int previousIndex() {
            return this.cursor - 1;
        }

        @Override
        public void set(T e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T e) {
            throw new UnsupportedOperationException();
        }
    }
}
