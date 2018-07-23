/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Containers;

import java.util.*;
import lt.lb.commons.Log;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;

/**
 *
 * @author Lemmin
 */
public class PagedList<T> implements List<T> {
    
    

    protected long maxRepeatedGet = 1;
    protected long repeatedGet = 0;
    protected PageAccess<T> cachedPageAccess;
    
    protected int initialPageSize = 16;
    protected int pageSize = initialPageSize;
    protected int fullSize = 0;
    protected ArrayList<T> cachedList;

    public PagedList() {
    }

    protected IList<Page<T>> pages = new GapList<>();

    
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

    private static class Page<E> {

        public List<E> items;

        public Page(int pageSize) {
            items = new GapList<>(pageSize);
        }

        public Page() {
            this(10);
        }

        @Override
        public String toString() {
            return items.toString();
        }
    }

    
    protected void maybeChangeSize(){
        if(fullSize < 100){
            return;
        }
        double divPage = this.size() / (double)pageSize;
        if(pageSize < divPage){
            pageSize*=2;
            mergePages();
        }else if(divPage*2 <pageSize ){
            pageSize/=2;
            mergePages();
        }
    }
    
    protected void mergePages(){
        Page<T> adding = null;
        ListIterator<Page<T>> iter = this.pages.listIterator();
        while(iter.hasNext()){
            Page<T> next = iter.next();
            if(adding == null){
                adding = next;
                continue;
            }
            int nextSize = next.items.size();
            if(adding.items.size() + nextSize < this.pageSize){
                adding.items.addAll(next.items);
                iter.remove();
            }else{
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
        if(this.cachedPageAccess != null && (this.cachedPageAccess.from >= fromIndex && this.cachedPageAccess.to < fromIndex)){
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
            this.repeatedGet = 0;
            this.fullSize++;
            return this.pages.add(p);
        } else {
            this.maybeChangeSize();
            Page page = this.pages.getLast();
            if (page.items.size() >= this.pageSize) {
                page = new Page<>(this.pageSize);
                this.pages.add(page);
            }
            this.repeatedGet = 0;
            this.fullSize++;
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
        this.repeatedGet = 0;
        this.fullSize = 0;
        this.pages.clear();
        this.pageSize = initialPageSize;
    }

    @Override
    public T get(int index) {
//        this.repeatedGet++;
//        if(this.repeatedGet > this.maxRepeatedGet){
//            if(this.cachedList == null){
//                this.cachedList = this.toArrayList();
//            }
//            
//            return this.cachedList.get(index);
//        }else if(this.cachedList != null){
//            cachedList = null;
//        }
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

                if (pageAccess.index == page.items.size() - 1) {//is last index
                    if (pageAccess.nextPage == null || pageAccess.nextPage.items.size() >= this.pageSize) {
                        Page<T> p = new Page<>(this.pageSize);
                        p.items.add(element);
                        this.pages.add(pageAccess.pageNo + 1, p);
                    } else {
                        pageAccess.nextPage.items.add(0, element);
                    }

                } else {

                    //try item shifting or split a page!!!
                    if (pageAccess.nextPage == null || pageAccess.nextPage.items.size() >= this.pageSize) {
                        Page p = new Page(this.pageSize);
                        this.pages.add(pageAccess.pageNo,p );
                        p.items.add(element);
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

        }
        fullSize++;
        this.repeatedGet = 0;
    }

    @Override
    public T remove(int index) {
        this.maybeChangeSize();
        PageAccess<T> pageAccess = this.getPageAccess(index);
        T removed = pageAccess.page.items.remove(pageAccess.index);
        this.fullSize--;
        this.repeatedGet = 0;
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
    
    public int getPageSize(){
        return this.pageSize;
    }
    
    public int getPageCount(){
        return this.pages.size();
    }
    
    public String getPageRepresentation(){
        String s = "";
        for(Page p:this.pages){
            s+= " ["+p.items.size()+"]";
        }
        return s;
    }

}
