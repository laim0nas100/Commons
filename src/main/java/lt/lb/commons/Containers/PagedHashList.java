/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Containers;

import java.util.*;
import lt.lb.commons.Log;
import lt.lb.commons.Misc.Pair;
import lt.lb.commons.Misc.Tuple;

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

//    protected Map<Integer, Page<T>> pageHash = new HashMap<>();
    public PrefillArrayMap<Page<T>> pageHash = new PrefillArrayMap<>();
//    public Map<Integer, Page<T>> pageHash = new PrefillArrayMap<Page<T>>().asMap();

    public PagedHashList() {
    }

    private void shiftHole(Page nextPage, int holeIndex) {
        while (true) {
            //fill hole
//            Log.print("Put:", holeIndex, "Page:", nextPage);
            this.pageHash.put(holeIndex, nextPage);
            nextPage.shiftBy(-1);
            holeIndex = nextPage.to();
            int nextPageIndex = holeIndex + 1;
            if (!this.pageHash.containsKey(nextPageIndex)) {

                Page<T> remove = this.pageHash.remove(holeIndex);
//                Log.print("Removed=" + holeIndex, remove);
                break;
            }
            nextPage = this.pageHash.get(nextPageIndex);
        }
    }

    private void shiftEndForwards(Page startFrom) {
        Page A = startFrom;
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
        if (i >= this.size() || i < 0) {
            throw new IndexOutOfBoundsException("Size:" + this.size() + " Index:" + i);
        }
    }

    protected void boundCheckInclusive(int i) {
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
        if (this.pageHash.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<Page<T>> list = new ArrayList<>();
        Page lastPage = null;
        Page p = this.pageHash.get(startingIndexPage);
        for (int i = 0; i < pageHash.innerSize(); i++) {
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

    protected static class PageIntervalRef {

        Page page;
        Pair<Integer> changeInterval;
    }

    protected void mergePages() {
        if (this.isEmpty()) {
            return;
        }
        Log.print("Merge pages");
        Log.printLines(this.getMappings());
        List<Page<T>> pages = this.getPages();
        List<Tuple<Page<T>, Pair<Integer>>> changeIntervals = new ArrayList<>();
        int pageCount = pages.size();
        Page<T> adding = pages.get(0);
        Page<T> next = null;
        Pair<Integer> pair = null;
        for (int i = 1; i < pageCount; i++) {
            next = pages.get(i);
            if (adding.size() + next.size() < this.pageSize) {
                adding.items.addAll(next.items);
                if (pair == null) {
                    pair = new Pair<>(next.from, next.to());
                } else {
                    pair.g2 = next.to();
                }
            } else {
                if (pair != null) {
                    changeIntervals.add(new Tuple<>(adding, pair));
                    pair = null;
                }

                adding = next;
            }
        }

        if (pair != null) {
            changeIntervals.add(new Tuple<>(adding, pair));
        }

        //make ref changes
        for (Tuple<Page<T>, Pair<Integer>> ref : changeIntervals) {
            Pair<Integer> interval = ref.g2;
            for (int i = interval.g1; i < interval.g2; i++) {
                this.pageHash.put(i, ref.g1);
            }
            Log.print(ref);
        }
        Log.printLines(this.getMappings());

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
        return this.getPage(index).getAbsolute(index);
    }

    @Override
    public T set(int index, T element) {
        this.boundCheck(index);
        return this.pageHash.get(index).setAbsolute(index, element);
    }

    @Override
    public void add(int index, T element) {
        this.boundCheckInclusive(index);
        this.maybeChangeSize();
        Log.print("Insert index:" + index, "Value:" + element);
        ArrayList<T> safeList = this.toArrayList();
        safeList.add(index, element);

        //add to last
        if (index == this.size()) {
            Log.print("Add to last page");
            add(element);
        } else {

            Page<T> page = this.pageHash.get(index);
            if (page.size() < this.pageSize) {
                Log.print("Current page not full");
                //are we at the last page
                if (page.to() == this.size()) {
                    Log.print("We are at the last page");
                    page.addAbsolute(index, element);
                    this.pageHash.put(page.to() - 1, page);
                } else {
                    Log.print("Generic page shift");
                    page.addAbsolute(index, element);
                    this.shiftEndForwards(page);

                }

            } else {

                Log.print("Page is full");
                //page full
                //are we at the start
                if (index == 0) {//worst case reshifting
                    Log.print("Insert at start");
                    Page<T> p = new Page<>();
                    p.add(element);

                    Page lastPage = this.pageHash.get(size() - 1);
                    while (true) {// shift from end
                        this.pageHash.put(lastPage.to(), lastPage);
                        int prevPageIndex = lastPage.from - 1;
                        lastPage.shiftBy(1);
                        if (prevPageIndex < 0) {
                            break;
                        }

                        lastPage = this.pageHash.get(prevPageIndex);
                    }
                    this.pageHash.put(0, p);

                } else {
                    boolean isPageStart = page.getSubIndex(index) == 0;

                    //try previous page
                    if (isPageStart && (this.pageHash.get(index - 1).size() < this.pageSize)) {
                        Log.print("Insert in page to the left");
                        Page prevPage = this.pageHash.get(index - 1);
                        prevPage.add(element);

                        this.shiftEndForwards(prevPage);
                    } else {
                        //try insert new page in the middle
                        Log.print("Try insert page in the middle");
                        if (isPageStart) {
                            Log.print("Just create new page");
                            Page<T> p = new Page<>();
                            p.add(element);
                            p.from = index;

                            this.shiftEndForwards(p);

                        } else {//alas, we have to split

                            Log.print("WE SPLIT THE PAGE");
                            int size = page.size();

                            int leftSize = (int) Math.floor(size / 2d);

                            Page<T> rightPage = new Page<>();
                            rightPage.from = page.from + leftSize;
                            for (int i = leftSize; i < size; i++) {
                                rightPage.add(page.items.get(i));

                            }
                            for (int i = rightPage.from; i < rightPage.to(); i++) {
                                pageHash.put(i, rightPage);
                            }
                            for (int i = size - 1; i >= leftSize; i--) {
                                page.items.remove(i);
                            }

                            Log.print(page, rightPage);

                            Log.print("Repeat add call");

                            this.add(index, element);

                        }

                    }
                }

            }
        }

        try {
            listEquals(this, safeList);
            Log.print("Lists are equal");
        } catch (IllegalStateException e) {
            Log.print(safeList);
            Log.printLines(this.getMappings());
            throw e;
        }
        Log.printLines(this.getMappings());

        fullSize++;
        this.repeatedGet = 0;

    }

    @Override
    public T remove(int index) {
        T removed = null;
        this.maybeChangeSize();

        Log.print("Remove index:" + index);
        ArrayList<T> safeList = this.toArrayList();
        safeList.remove(index);

        int lastIndex = this.pageHash.size() - 1;
        if (index == lastIndex) {
            Log.print("Remove last");
            removed = this.pageHash.remove(index).removeAbsoluteWithChange(index);
        } else {
            Page<T> page = this.pageHash.get(index);
            //are we at the last page?
            if (page.to() == this.size()) {
                Log.print("Hit last page");
                removed = page.removeAbsoluteWithChange(index);
                this.pageHash.remove(page.to());
            } else if (index == page.to() - 1) {//maybe we hit page end?
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
        }
        try {
            listEquals(this, safeList);
            Log.print("Lists are equal");
        } catch (IllegalStateException e) {
            Log.print(safeList);
            Log.printLines(this.getMappings());
            throw e;
        }
        Log.printLines(this.getMappings());

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

    public double getAveragePageSize() {
        double avg = 0;
        int pageCount = 0;
        for (Page p : this.getPages()) {
            pageCount++;
            avg += p.size();
        }
        return avg / pageCount;
    }

    public String getPageRepresentation() {
        String s = "";
        for (Page p : this.getPages()) {
            s += " [" + p.items.size() + "]";
        }
        return s;
    }

    public static void listEquals(List l1, List l2) {
        if (l1.size() != l2.size()) {
            throw new IllegalStateException("Size is not equal");
        }
        int size = l1.size();
        for (int i = 0; i < size; i++) {
            if (!Objects.equals(l1.get(i), l2.get(i))) {
                throw new IllegalStateException("Not equal at index " + i + " list1:" + l1.get(i) + " list2:" + l2.get(i));
            }
        }
    }

}
