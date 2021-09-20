package lt.lb.commons.containers.collections;

import java.util.*;
import lt.lb.commons.containers.collections.ListIterators.SkippingListIterator;

/**
 *
 * @author laim0nas100
 */
public class PagedList<T> implements List<T> {

    protected static int MIN_FULL_SIZE_4_PAGING = 512;

    protected PageAccess<T> cachedPageAccess;

    protected int initialPageSize = MIN_FULL_SIZE_4_PAGING;
    protected int pageSize = initialPageSize;
    protected int fullSize = 0;

    protected ArrayList<T> cachedList;

    public PagedList() {
    }

    protected List<Page<T>> pages = new ArrayList<>();

    private static class PageAccess<D> {

        public int from;
        public int to;
        public int pageNo;
        public int index;
        public Page<D> page;
        public Page<D> nextPage;
        public Page<D> prevPage;

        @Override
        public String toString() {
            return "PageNO:" + pageNo + " :" + index;
        }

        public D get() {
            return page.items.get(index);
        }

        public D set(D item) {
            return page.items.set(index, item);
        }
    }

    private static class Page<E> {

        public List<E> items;

        public Page(int pageSize) {
            items = new ArrayList<>(pageSize);
        }

        public Page() {
            this(MIN_FULL_SIZE_4_PAGING);
        }

        @Override
        public String toString() {
            return items.toString();
        }
    }

    protected void maybeChangeSize() {
        if (fullSize < MIN_FULL_SIZE_4_PAGING && pages.size() <= 2) {
            return;
        }
        double divPage = this.size() / (double) pageSize;
        if (pageSize < divPage) {
            pageSize *= 2;
            mergePages();
        } else if (divPage * 2 < pageSize) {
            pageSize /= 2;
            mergePages();
        }
    }

    protected void mergePages() {
        if (fullSize < MIN_FULL_SIZE_4_PAGING && pages.size() <= 2) {
            return;
        }
        Page<T> adding = null;
        ListIterator<Page<T>> iter = this.pages.listIterator();
        while (iter.hasNext()) {
            Page<T> next = iter.next();
            if (adding == null) {
                adding = next;
                continue;
            }
            int nextSize = next.items.size();
            if (adding.items.size() + nextSize < this.pageSize) {
                adding.items.addAll(next.items);
                iter.remove();
            } else {
                adding = next;
            }
        }

    }

    @Override
    public int size() {
        return fullSize;
    }

    @Override
    public boolean isEmpty() {
        return pages.isEmpty();
    }

    private PageAccess<T> getPageAccess(int fromIndex) {
        if (fromIndex >= this.size() || fromIndex < 0) {
            throw new NoSuchElementException();
        }
        if (this.cachedPageAccess != null && (this.cachedPageAccess.from >= fromIndex && this.cachedPageAccess.to < fromIndex)) {
            this.cachedPageAccess.index = fromIndex - this.cachedPageAccess.from;
            return this.cachedPageAccess;
        }
        ListIterator<Page<T>> iter = this.pages.listIterator();
        int prog = 0;
        int pageNo = 0;
        Page<T> page = null;
        while (iter.hasNext()) {
            Page<T> prev = page;
            page = iter.next();
            int newProg = page.items.size();

            if (prog + newProg > fromIndex) {
                int subIndex = fromIndex - prog;
                PageAccess<T> pa = new PageAccess<>();
                pa.from = prog;
                pa.to = prog + newProg;
                pa.index = subIndex;
                pa.page = page;
                pa.pageNo = pageNo;
                pa.prevPage = prev;
                if (iter.hasNext()) {
                    pa.nextPage = iter.next();
                }
                return pa;
            }
            prog += newProg;
            pageNo++;
        }
        throw new NoSuchElementException();

    }

    @Override
    public boolean contains(Object o) {
        for (Page p : pages) {
            if (p.items.contains(o)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<T> toArrayList() {
        ArrayList<T> array = new ArrayList<>(this.fullSize);
        for (Page p : pages) {
            array.addAll(p.items);
        }
        return array;
    }

    @Override
    public Iterator<T> iterator() {
        return this.listIterator();
    }

    @Override
    public Object[] toArray() {
        return this.toArrayList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.toArrayList().toArray(a);
    }

    @Override
    public boolean add(T e) {

        if (this.isEmpty()) {
            Page p = new Page<>(this.pageSize);
            p.items.add(e);
            this.fullSize++;
            return this.pages.add(p);
        } else {
            this.maybeChangeSize();
            Page page = this.pages.get(pages.size() - 1);
            if (page.items.size() >= this.pageSize) {
                page = new Page<>(this.pageSize);
                this.pages.add(page);
            }
            this.fullSize++;
            page.items.add(e);
            return true;
        }
    }

    @Override
    public boolean remove(Object o) {
        int i = this.indexOf(o);
        if (i >= 0) {
            this.remove(i);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean found = false;
        for (Object o : c) {
            found = false;
            for (Page p : pages) {
                if (found) {
                    break;
                }
                found = p.items.contains(o);
            }
            if (!found) {
                return false;
            }
        }
        return found;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return addAll(fullSize, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {

        if (c.isEmpty()) {
            return true;
        }
        int size = c.size();
        int pSize = Math.max(size, pageSize);

        if (index == fullSize) {
            Page p = new Page<>(pSize);
            this.fullSize += size;
            pages.add(p);
            p.items.addAll(c);

        } else {
            PageAccess<T> pageAccess = getPageAccess(index);
            Page page = pageAccess.page;
            page.items.addAll(pageAccess.index, c);
            this.fullSize += size;

        }
        maybeChangeSize();
        return true;

    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object item : c) {
            this.remove(item);
        }
        maybeChangeSize();
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Set<?> set = new HashSet<>(c);
        ListIterator<T> listIterator = listIterator();
        while (listIterator.hasNext()) {
            T item = listIterator.next();
            if (!set.contains(item)) {
                listIterator.remove();
            }
        }
        maybeChangeSize();
        return true;
    }

    @Override
    public void clear() {
        this.fullSize = 0;
        this.pages.clear();
        this.pageSize = initialPageSize;
    }

    @Override
    public T get(int index) {
        return getPageAccess(index).get();
    }

    @Override
    public T set(int index, T element) {
        return getPageAccess(index).set(element);
    }

    @Override
    public void add(int index, T element) {
        this.maybeChangeSize();
        PageAccess pageAccess = this.getPageAccess(index);
        Page<T> page = pageAccess.page;
        if (page.items.size() < this.pageSize) {
            page.items.add(pageAccess.index, element);
        } else {

            //try previous page
            if (pageAccess.index == 0) {
                if (pageAccess.prevPage == null || pageAccess.prevPage.items.size() >= this.pageSize) {
                    Page<T> p = new Page<>(this.pageSize);
                    p.items.add(element);
                    this.pages.add(pageAccess.pageNo, p);
                } else {
                    pageAccess.prevPage.items.add(element);
                }
            } else {

                if (pageAccess.index == page.items.size()) {//is last index
                    if (pageAccess.nextPage == null) {
                        Page<T> p = new Page<>(this.pageSize);
                        p.items.add(element);
                        this.pages.add(pageAccess.pageNo + 1, p);
                    } else {
                        pageAccess.nextPage.items.add(0, element);
                    }

                } else {

                    //Splitting
                    Page left = new Page(this.pageSize);
                    Page right = new Page(this.pageSize);
                    int i = 0;
                    for (; i < pageAccess.index; i++) {
                        left.items.add(page.items.get(i));
                    }
                    for (; i < page.items.size(); i++) {
                        right.items.add(page.items.get(i));
                    }
                    page.items = right.items;
                    this.pages.add(pageAccess.pageNo, left);
                    left.items.add(element);

                }

            }

        }
        fullSize++;
    }

    @Override
    public T remove(int index) {
        this.maybeChangeSize();
        PageAccess<T> pageAccess = this.getPageAccess(index);
        T removed = pageAccess.page.items.remove(pageAccess.index);
        this.fullSize--;
        if (pageAccess.page.items.isEmpty()) {
            this.pages.remove(pageAccess.pageNo);
        }
        return removed;
    }

    @Override
    public int indexOf(Object o) {
        ListIterator<T> iter = this.listIterator();
        while (iter.hasNext()) {
            int index = iter.nextIndex();
            T next = iter.next();
            if (Objects.equals(next, o)) {
                return index;
            }
        }
        return -1;

    }

    @Override
    public int lastIndexOf(Object o) {
        ListIterator<T> iter = this.listIterator(this.fullSize);
        int i = this.fullSize;
        while (iter.hasPrevious()) {
            i--;
            T next = iter.previous();
            if (Objects.equals(next, o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return this.listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new SkippingListIterator(index, this);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return toArrayList().subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Page p : pages) {
            s.append(p.toString());
        }
        return s.toString();
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getPageCount() {
        return this.pages.size();
    }

    public String getPageRepresentation() {
        StringBuilder s = new StringBuilder();
        for (Page p : this.pages) {
            s.append(" [").append(p.items.size()).append("]");
        }
        return s.toString();
    }
}
