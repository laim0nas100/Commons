/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Containers;

import java.util.*;
import lt.lb.commons.Log;

/**
 *
 * @author Lemmin
 */
public class PagedHashList<T> implements List<T> {

    protected long pageID;

    protected long maxRepeatedGet = 1;
    protected long repeatedGet = 0;
    protected Page<T> cachedPage;

    protected int initialPageSize = 4;
    protected int pageSize = initialPageSize;
    protected int fullSize = 0;
    protected ArrayList<T> cachedList;

    protected Map<Integer, Page<T>> pageHash = new HashMap<>();
//    public PrefillArrayMap<Page<T>> pageHash = new PrefillArrayMap<>();
//    public Map<Integer, Page<T>> pageHash = new PrefillArrayMap<Page<T>>().asMap();

    public PagedHashList() {
    }

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
    }

    private void shiftHole(Page nextPage, int holeIndex) {
        while (true) {
            //fill hole
            Log.print("Put:", holeIndex, "Page:", nextPage);
            this.pageHash.put(holeIndex, nextPage);
            nextPage.shiftBy(-1);
            holeIndex = nextPage.to();
            int nextPageIndex = holeIndex + 1;
            if (!this.pageHash.containsKey(nextPageIndex)) {

                Page<T> remove = this.pageHash.remove(holeIndex);
                Log.print("Removed=" + holeIndex, remove);
                break;
            }
            nextPage = this.pageHash.get(nextPageIndex);
        }
    }

    private static class Page<E> {

        public int from;
        public List<E> items;

        public Page(int pageSize) {
            items = new ArrayList<>(pageSize);
        }

        public Page() {
            this(10);
        }

        public int to() {
            return this.from + this.size();
        }

        public int size() {
            return items.size();
        }

        public boolean inBounds(int index) {
            return (index >= from) && (index < to());
        }

        public E getAbsolute(int index) {
            if (inBounds(index)) {
                int sub = index - this.from;
                return this.items.get(sub);
            } else {
                throw new IndexOutOfBoundsException(from + " " + to() + " index:" + index);
            }

        }

        public E setAbsolute(int index, E newElem) {
            if (inBounds(index)) {
                return this.items.set(getSubIndex(index), newElem);
            } else {
                throw new IndexOutOfBoundsException(from + " " + to() + " index:" + index);
            }
        }

        public boolean add(E elem) {
            return this.items.add(elem);
        }

        public void addAbsolute(int index, E elem) {
            if (this.inBounds(index)) {
                this.items.add(getSubIndex(index), elem);
            } else {
                throw new IndexOutOfBoundsException(from + " " + to() + " index:" + index);
            }

        }

        public int getSubIndex(int index) {
            return index - from;
        }

        public void shiftBy(int shift) {
            this.from += shift;
        }

        @Override
        public String toString() {
            return this.from + " : " + this.to() + " = " + items.toString();
        }

        public E removeAbsoluteWithChange(int index) {
            if (inBounds(index)) {
                return items.remove(getSubIndex(index));
            } else {
                throw new IndexOutOfBoundsException(from + " " + to() + " index:" + index);
            }
        }
    }

    protected void boundCheck(int i) {
        if (i > this.size() || i < 0) {
            throw new IndexOutOfBoundsException("Size:" + this.size() + " Index:" + i);
        }
    }

    protected void maybeChangeSize() {
        if (fullSize < 100) {
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

    private List<Page<T>> getPages(int startingIndexPage) {
        Log.print("GET PAGES");
        if (this.pageHash.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<Page<T>> list = new ArrayList<>();
        Page lastPage = null;
        Page p = this.pageHash.get(startingIndexPage);
        Log.print("Page:" + p);
        for (int i = 0; i < pageHash.size(); i++) {
            p = pageHash.get(i);
            if (lastPage != p && p != null) {
                lastPage = p;
                list.add(p);
            }

//            if (this.pageHash.containsKey(p.to())) {
//                p = this.pageHash.get(p.to());
//                Log.print("Got page:" + p);
//            } else {
//                break;
//            }
        }
        return list;
    }

    private List<Page<T>> getPages() {
        return this.getPages(0);
    }

    protected void mergePages() {
//        Page<T> adding = null;
//        ListIterator<Page<T>> iter = this.pages.listIterator();
//        while (iter.hasNext()) {
//            Page<T> next = iter.next();
//            if (adding == null) {
//                adding = next;
//                continue;
//            }
//            int nextSize = next.items.size();
//            if (adding.items.size() + nextSize < this.pageSize) {
//                adding.items.addAll(next.items);
//                iter.remove();
//            } else {
//                adding = next;
//            }
//        }

    }

    @Override
    public int size() {
        return this.pageHash.size();
    }

    @Override
    public boolean isEmpty() {
        return this.pageHash.isEmpty();
    }

    private Page<T> getPage(int fromIndex) {

        if (fromIndex >= this.size() || fromIndex < 0) {
            throw new NoSuchElementException();
        }
        if (!this.pageHash.containsKey(fromIndex)) {
            throw new NoSuchElementException();
        }
        if (this.cachedPage == null) {
            this.cachedPage = this.pageHash.get(fromIndex);
        } else if (!this.cachedPage.inBounds(fromIndex)) {
            this.cachedPage = this.pageHash.get(fromIndex);
        }
        return this.cachedPage;

    }

    @Override
    public boolean contains(Object o) {

        return this.getPages().stream().parallel().anyMatch(p -> p.items.contains(o));
    }

    public ArrayList<T> toArrayList() {
        ArrayList<T> array = new ArrayList<>(this.fullSize);
        for (Page p : this.getPages()) {
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

    private void insertPageHash(int index, Page p) {
        int pSize = p.items.size();
        if (this.pageHash.isEmpty()) {
            p.from = 0;
            int to = p.from + pSize;
            for (int i = p.from; i < to; i++) {
                this.pageHash.put(i, p);
            }
        } else {
            int hashSize = this.pageHash.size();
            if (index == hashSize) { //insert to end
                Page<T> prevPage = this.pageHash.get(index - 1);
                p.from = prevPage.to();
                int to = p.from + pSize;
                for (int i = p.from; i < to; i++) {
                    this.pageHash.put(i, p);
                }
            } else { //do the shift
                Page<T> lastPage = this.pageHash.get(this.pageHash.size() - 1);

            }
        }

    }

    @Override
    public boolean add(T e) {
        if (this.isEmpty()) {
            Page p = new Page<>(this.pageSize);
            p.items.add(e);
            p.from = 0;
            this.repeatedGet = 0;
            this.fullSize++;
            this.pageHash.put(0, p);
        } else {
            this.maybeChangeSize();
            int lastIndex = size() - 1;
            Page<T> lastPage = this.pageHash.get(lastIndex);

            if (lastPage.items.size() >= this.pageSize) {
                lastPage = new Page<>(this.pageSize);
                lastPage.from = lastIndex + 1;
                lastPage.items.add(e);
            } else {
                lastPage.add(e);
            }
            this.repeatedGet = 0;
            this.fullSize++;

            this.pageHash.put(lastIndex + 1, lastPage);

        }
        return true;
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
        this.repeatedGet = 0;
        this.fullSize = 0;
        this.pageHash.clear();
        this.pageSize = initialPageSize;
    }

    @Override
    public T get(int index) {
//        this.boundCheck(index);
        return this.pageHash.get(index).getAbsolute(index);
    }

    @Override
    public T set(int index, T element) {
        this.boundCheck(index);
        return this.pageHash.get(index).setAbsolute(index, element);
    }

    @Override
    public void add(int index, T element) {
        this.boundCheck(index);
        this.maybeChangeSize();

        //add to last
        if (index == this.size() - 1) {
            Log.print("Add to last page");
            add(element);
            return;
        }

        Page<T> page = this.pageHash.get(index);
        if (page.size() < this.pageSize) {
            //are we at the last page
            if (page.to() == this.size()) {
                page.addAbsolute(index, element);
                this.pageHash.put(page.to(), page);
            } else {

                page.addAbsolute(index, element);
                // replace loop
                /*
                    insert in page A with no change
                loop:
                    get page B by A.to // break here
                    put page A in B.from
                 */
                Page A = page;
                Page B = null;

                while (true) {
                    int nextPageIndex = A.to() - 1;
                    if (!this.pageHash.containsKey(nextPageIndex)) { //
                        this.pageHash.put(nextPageIndex, A);
                        break;
                    }
                    B = this.pageHash.get(nextPageIndex);

                    this.pageHash.put(B.from, A);
                    B.shiftBy(1);
                    A = B;
                }
            }

        } else {
            boolean isPageEnd = page.getSubIndex(index) == page.size() - 1;
            boolean isPageStart = page.getSubIndex(index) == 0;
            //page full
            //try previous page
            if (isPageStart && (this.getPage(index - 1).size() < this.pageSize)) {
                Page prevPage = this.getPage(index - 1);
                prevPage.add(element);

                Page A = prevPage;
                Page B = null;

                while (true) {
                    int nextPageIndex = A.to() - 1;
                    if (!this.pageHash.containsKey(nextPageIndex)) { //
                        this.pageHash.put(nextPageIndex, A);
                        break;
                    }
                    B = this.pageHash.get(nextPageIndex);

                    this.pageHash.put(B.from, A);
                    B.shiftBy(1);
                    A = B;
                }
                //page full
                //try next page
            } else if (isPageEnd && (this.getPage(index + 1).size() < this.pageSize)) {//try next page
                Page nextPage = this.getPage(index + 1);
                if (nextPage.size() < this.pageSize) {
                    nextPage.addAbsolute(nextPage.from, element);

                    Page A = nextPage;
                    Page B = null;

                    while (true) {
                        int nextPageIndex = A.to() - 1;
                        if (!this.pageHash.containsKey(nextPageIndex)) { //
                            this.pageHash.put(nextPageIndex, A);
                            break;
                        }
                        B = this.pageHash.get(nextPageIndex);

                        this.pageHash.put(B.from, A);
                        B.shiftBy(1);
                        A = B;
                    }
                }
            } else {
                //try insert new page in the middle
                if (isPageEnd || isPageStart) {
                    Page<T> p = new Page<>();
                    p.add(element);
                    p.from = index;

                    Page A = p;
                    Page B = null;

                    while (true) {
                        int nextPageIndex = A.to() - 1;
                        if (!this.pageHash.containsKey(nextPageIndex)) { //
                            this.pageHash.put(nextPageIndex, A);
                            break;
                        }
                        B = this.pageHash.get(nextPageIndex);

                        this.pageHash.put(B.from, A);
                        B.shiftBy(1);
                        A = B;
                    }

                } else {//alas, we have to split
//                    if (true) {
//                        throw new UnsupportedOperationException("Page slicing not yet supported");
//                    }

                    Page lastPage = this.getPage(size() - 1);
                    //TODO
                    while (lastPage.from > page.from) {// shift from end 
                        this.pageHash.put(lastPage.to(), lastPage);
                        Page newLastPage = this.getPage(lastPage.from-1);
                        lastPage.shiftBy(1);
                        lastPage = newLastPage;
                        
                    }

                    Page left = new Page(this.pageSize);
                    Page right = new Page(this.pageSize);
                    int i = 0;
                    int subIndex = page.getSubIndex(index);
                    for (; i < subIndex; i++) {
                        left.items.add(page.items.get(i));
                        this.pageHash.put(i + index, left);
                    }
                    int itemsSize = page.items.size();
                    for (; i < itemsSize; i++) {
                        right.items.add(page.items.get(i));
                        this.pageHash.put(i + index, right);
                    }
                    left.from = page.from;
                    right.from = left.to();

                }

                //Splitting 
//            Page left = new Page(this.pageSize);
//            Page right = new Page(this.pageSize);
//            int i = 0;
//            for (; i < pageAccess.index; i++) {
//                left.items.add(page.items.get(i));
//            }
//            for (; i < page.items.size(); i++) {
//                right.items.add(page.items.get(i));
//            }
//            page.items = right.items;
//            this.pages.add(pageAccess.pageNo, left);
//            left.items.add(element);
            }

        }

        fullSize++;
        this.repeatedGet = 0;
    }

    @Override
    public T remove(int index) {
        T removed = null;
        this.maybeChangeSize();
        int lastIndex = this.pageHash.size() - 1;
        if (index == lastIndex) {
            Log.print("Remove last");
            return this.pageHash.remove(index).removeAbsoluteWithChange(index);
        }
        Page<T> page = this.pageHash.get(index);
        //are we at the last page?
        if (page.to() == this.size()) {
            Log.print("Hit last page");
            removed = page.removeAbsoluteWithChange(index);
            this.pageHash.remove(page.to());
            return removed;
        }

        //maybe we hit page end?
        if (index == page.to() - 1) {
            Log.print("hit page end");

            removed = page.removeAbsoluteWithChange(index);
            int nextPageIndex = index + 1;
            Page<T> nextPage = this.pageHash.get(nextPageIndex);

            int holeIndex = page.to();
            this.shiftHole(nextPage, holeIndex);
        } else {

            Log.print("Shifting required at index:", index);
            //shift current page end to the hole
            int nextPageIndex = page.to();
            removed = page.removeAbsoluteWithChange(index);
            Page<T> nextPage = this.pageHash.get(nextPageIndex);
            int holeIndex = page.to();
            this.shiftHole(nextPage, holeIndex);

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

    public List<String> getMappings() {
        ArrayList<String> map = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            if (this.pageHash.containsKey(i)) {
                map.add(i + " ->" + this.pageHash.get(i).toString());
            } else {
                map.add(i + " -> null");
            }
        }
        return map;
    }

    @Override
    public String toString() {
        String s = "";
        for (Page p : this.getPages()) {
            s += p.toString() + "; ";
        }
        return s;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getPageCount() {
        return this.getPages().size();
    }

    public String getPageRepresentation() {
        String s = "";
        for (Page p : this.getPages()) {
            s += " [" + p.items.size() + "]";
        }
        return s;
    }

}
