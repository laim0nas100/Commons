/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa;

import java.util.Iterator;
import java.util.function.Function;
import lt.lb.commons.iteration.PagedIteration;

/**
 *
 * @author laim0nas100
 */
public class JPAPagedAccess<T> implements PagedIteration<Integer, T>{

    public JPAPagedAccess(Integer pageSize, Integer count, Function<PageAccess, Iterator<T>> itemGetter) {
        this.pageSize = pageSize;
        this.count = count;
        this.itemGetter = itemGetter;
    }

    public static class PageAccess{
        public final int startIndex;
        public final int endIndex;

        public PageAccess(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
        
    }
    private final Integer pageSize;
    private final Integer count;
    private final Function<PageAccess,Iterator<T>> itemGetter;
    
    
    @Override
    public Integer getFirstPage() {
        return 0;
    }

    @Override
    public Iterator<T> getItems(Integer info) {
        return itemGetter.apply(new PageAccess(info, info+pageSize));
    }

    @Override
    public Integer getNextPage(Integer info) {
        return info + pageSize;
    }

    @Override
    public boolean hasNextPage(Integer info) {
        return info < count;
    }
    
}
