package lt.lb.commons.containers.caching.paging.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.containers.caching.paging.SeqLoadedPage;
import lt.lb.commons.containers.caching.paging.SeqPageLoader;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class BulkPageLoader<T> implements SeqPageLoader<T> {

    protected SeqPageLoader<T> pageLoader;
    protected int bulkPageSize = 1_000;
    protected int maxListSize = 2_000_000_000;
    protected Map<Long, BulkPage<T>> pages = new HashMap<>();

    public BulkPageLoader(SeqPageLoader<T> pageLoader, int pageSize, int maxListSize) {
        this.pageLoader = Objects.requireNonNull(pageLoader);
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive, now " + pageSize);
        }
        if (maxListSize < pageSize) {
            throw new IllegalArgumentException("Max list size must not be lower than pageSize now:" + maxListSize+" pageSize:"+pageSize);
        }
        this.bulkPageSize = pageSize;
        this.maxListSize = maxListSize;
    }

    @Override
    public SeqLoadedPage<T> loadPage(Long index, long size) {
        preparePages(index, size);
        if (size > maxListSize) {
            List<SeqLoadedPage<T>> loaded = new ArrayList<>();
            long sizeLoaded = 0L;
            while (sizeLoaded < size) {
                int toLoad = (int) (Math.min(maxListSize, size - sizeLoaded));
                List<T> fillList = fillList(index + sizeLoaded, toLoad);
                sizeLoaded += fillList.size();
                loaded.add(new Pages.SimpleListPage<>(fillList));
                if(toLoad > fillList.size() || toLoad < maxListSize){//loaded everything available
                    break;
                }
            }
            
            return new Pages.ConcatSeqPageStatic<>(loaded);

        } else {
            int isize = (int) size;

            List<T> fillList = fillList(index, isize);
            return new Pages.SimpleListPage<>(fillList);
        }
    }

    public static class BulkPage<V> implements Comparable<BulkPage> {

        public final List<V> list;
        public final long from;

        public BulkPage(long from, List<V> list) {
            this.list = list;
            this.from = from;
        }

        public long to() {
            return from + list.size();
        }

        public int size() {
            return list.size();
        }

        public V getRelative(long i) {
            int shifted = (int) (i - from);
            return list.get(shifted);
        }

        @Override
        public int compareTo(BulkPage o) {
            return Long.compare(from, o.from);
        }
    }

    public static long[] getPageIdx(long position, long size, int pageSize) {

        long first = position;
        long last = position + size;
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive, now " + pageSize);
        }
        if (first < 0) {
            throw new IllegalArgumentException("Position size must be positive, now " + position);
        }
        long page = (position / pageSize);

        List<Long> indexes = new ArrayList<>();
        boolean singlePage = true;
        while (true) {
            long pFirst = page * pageSize;
            long pLast = pFirst + pageSize;

            if (singlePage) {
                if (pFirst <= first && pLast >= last) { // all fits in a page, easy
                    indexes.add(page);
                    return new long[]{page};
                }
                if (pFirst <= first && first < pLast && pLast < last) { // start first, but not the end
                    indexes.add(page);
                    singlePage = false;
                }
            } else {
                if (pLast < last) {
                    indexes.add(page);
                } else if (pLast >= last) {
                    indexes.add(page);
                    long[] arr = new long[indexes.size()];
                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = indexes.get(i);
                    }
                    return arr;
                }
            }
            page++;
        }
    }

    protected List<BulkPage<T>> getPages(long[] pidx) {
        List<BulkPage<T>> list = new ArrayList<>(pidx.length);
        for (long p : pidx) {
            list.add(pages.get(p));
        }
        return list;
    }

    protected List<T> fillList(long position, int size) { // assume pages are present
        List<T> list = new ArrayList<>(size);

        List<BulkPage<T>> bulk = getPages(getPageIdx(position, size, bulkPageSize));

        long pos = position;
        for (BulkPage<T> page : bulk) {
            if (page.from <= pos && page.to() > pos) {
                while (list.size() < size && pos < page.to()) {
                    list.add(page.getRelative(pos++));
                }
            }
            if (list.size() >= size) {
                return list;
            }
        }
        return list;
    }

    public void preparePages(long position, long size) {

        long[] pageIdx = getPageIdx(position, size, bulkPageSize);
        for (long p : pageIdx) {
            if (pages.containsKey(p)) {
                continue;
            }
            long fromP = p * bulkPageSize;
            SeqLoadedPage<T> loadPage = pageLoader.loadPage(fromP, bulkPageSize);
            long bigSize = loadPage.size();
            if (bigSize <= bulkPageSize) {// if lower, assume we ran out of items
                pages.put(p, new BulkPage<>(fromP, loadPage.toList()));
            } else {
                long[] nestedPages = getPageIdx(position, bigSize, bulkPageSize);
                int skip = 0;
                List<T> bigList = loadPage.toList();
                for (long pp : nestedPages) {
                    int to = Math.min(skip + bulkPageSize, bigList.size());
                    final int newPageSize = Math.max(to - skip, 0);
                    BulkPage<T> bulkPage = pages.computeIfAbsent(pp, k -> new BulkPage<>(k, new ArrayList<>(newPageSize)));

                    int from = skip + bulkPage.size();

                    if (from < to) {
                        bulkPage.list.addAll(bigList.subList(from, to));
                    }
                    skip += to;
                }
            }
        }
    }

}
