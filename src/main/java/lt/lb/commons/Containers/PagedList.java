/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Containers;

import java.util.*;

/**
 *
 * @author Lemmin
 */
public class PagedList<T> implements List<T> {

    protected int pageSize = 8;
    protected int fullSize;

    public PagedList() {
    }

    protected LinkedList<Page<T>> pages = new LinkedList<>();

    private static class PageAccess<D> {

        public int pageNo;
        public int index;
        public Page<D> page;
        public Page<D> nextPage;
        public Page<D> prevPage;

        @Override
        public String toString() {
            return "PageNO:" + pageNo + " :" + index;
        }
    }

    private static class Page<E> {

        public List<E> items = new ArrayList<>();

        public Page() {
            items.size();
        }

        @Override
        public String toString() {
            return items.toString();
        }
    }

    @Override
    public int size() {
        int size = 0;
        for (Page p : pages) {
            size += p.items.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return pages.isEmpty();
    }

    private PageAccess<T> getPageAccess(int fromIndex) {
        if (fromIndex >= this.size() || fromIndex < 0) {
            throw new NoSuchElementException();
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
        ArrayList array = new ArrayList<>(this.fullSize);
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
            Page p = new Page<>();
            p.items.add(e);
            return this.pages.add(p);
        } else {
            Page page = this.pages.getLast();
            if (page.items.size() >= this.pageSize) {
                page = new Page<>();
                this.pages.add(page);
            }
            page.items.add(e);
            return true;
        }
    }

    @Override
    public boolean remove(Object o) {
        int i = this.indexOf(o);
        if (i >= 0) {
            this.remove(this.indexOf(o));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.toArrayList().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object item : c) {
            this.remove(item);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        this.fullSize = 0;
        this.pages.clear();
    }

    @Override
    public T get(int index) {
        PageAccess<T> pageAccess = this.getPageAccess(index);
        return pageAccess.page.items.get(pageAccess.index);
    }

    @Override
    public T set(int index, T element) {
        PageAccess<T> pageAccess = this.getPageAccess(index);
        return pageAccess.page.items.set(pageAccess.index, element);
    }

    @Override
    public void add(int index, T element) {
        PageAccess pageAccess = this.getPageAccess(index);
        Page<T> page = pageAccess.page;
        if (page.items.size() < this.pageSize) {
            page.items.add(pageAccess.index, element);
        } else {

            //try previous page
            //TODO
            if (pageAccess.index == 0) {
                if (pageAccess.prevPage == null || pageAccess.prevPage.items.size() >= this.pageSize) {
                    Page<T> p = new Page<>();
                    p.items.add(element);
                    this.pages.add(pageAccess.pageNo, p);
                } else {
                    pageAccess.prevPage.items.add(element);
                }
            } else {

                //try item shifting or split a page!!!
                if (pageAccess.nextPage == null || pageAccess.nextPage.items.size() >= this.pageSize) {
                    page = new Page<>();
                    this.pages.add(pageAccess.pageNo, page);
                    page.items.add(element);
                }

                throw new IllegalStateException();
            }

        }
    }

    @Override
    public T remove(int index) {
        PageAccess<T> pageAccess = this.getPageAccess(index);
        T removed = pageAccess.page.items.remove(pageAccess.index);
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
        ListIterator<T> iter = this.listIterator();
        int lastIndex = -1;
        while (iter.hasNext()) {
            int index = iter.nextIndex();
            T next = iter.next();
            if (Objects.equals(next, o)) {
                lastIndex = index;
            }
        }
        return lastIndex;
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
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String toString() {
        String s = "";
        for (Page p : pages) {
            s += p.toString();
        }
        return s;
    }

}
