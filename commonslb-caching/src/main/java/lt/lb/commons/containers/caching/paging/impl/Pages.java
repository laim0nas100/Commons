package lt.lb.commons.containers.caching.paging.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import lt.lb.commons.containers.caching.paging.LoadedPage;
import lt.lb.commons.containers.caching.paging.SeqLoadedPage;

/**
 *
 * @author laim0nas100
 */
public abstract class Pages {

    public static class SimpleMapPage<K, V> implements LoadedPage<K, V> {

        protected Map<K, V> map;

        public SimpleMapPage(Map<K, V> map) {
            this.map = Objects.requireNonNull(map);
        }

        @Override
        public V get(K id) {
            return map.get(id);
        }

        @Override
        public long size() {
            return map.size();
        }

        @Override
        public boolean contains(K id) {
            return map.containsKey(id);
        }
    }

    public static class SimpleListPage<V> implements SeqLoadedPage<V> {

        protected List<V> list;

        public SimpleListPage(List<V> list) {
            this.list = Objects.requireNonNull(list);
        }

        @Override
        public V get(Long index) {
            return list.get(index.intValue());
        }

        @Override
        public V get(int index) {
            return list.get(index);
        }

        @Override
        public boolean contains(int index) {
            return index >= 0 && index < size();
        }

        @Override
        public boolean contains(Long index) {
            int i = index.intValue();
            return i >= 0 && i < size();
        }

        @Override
        public long size() {
            return list.size();
        }

    }

    public static class ConcatSeqPage<V> implements SeqLoadedPage<V> {

        protected long shift;
        protected List<SeqLoadedPage<V>> seqLoadedPage;

        public ConcatSeqPage(List<SeqLoadedPage<V>> seqLoadedPage) {
            this(0L, seqLoadedPage);
        }

        public ConcatSeqPage(long shift, List<SeqLoadedPage<V>> seqLoadedPage) {
            if (shift < 0) {
                throw new IllegalArgumentException("shift must be non-negative, now " + shift);
            }
            this.shift = shift;
            this.seqLoadedPage = Objects.requireNonNull(seqLoadedPage);
        }

        @Override
        public V get(Long id) {
            long f = shift;
            for (SeqLoadedPage<V> page : seqLoadedPage) {
                long isize = page.size();
                long l = f + isize;
                if (id >= f && id < l) {
                    return page.get(id - f);
                }
                f += isize;

            }
            return returnEmpty();
        }

        @Override
        public long size() {
            return seqLoadedPage.stream().mapToLong(m -> m.size()).sum();
        }

    }

    public static class ConcatSeqPageStatic<V> extends ConcatSeqPage<V> {

        long size;
        long[] sizes;

        public ConcatSeqPageStatic(List<SeqLoadedPage<V>> seqLoadedPage) {
            this(0L, seqLoadedPage);
        }

        public ConcatSeqPageStatic(long shift, List<SeqLoadedPage<V>> seqLoadedPage) {
            super(shift, seqLoadedPage);
            sizes = seqLoadedPage.stream().mapToLong(m -> m.size()).toArray();
            size = LongStream.of(sizes).sum();
        }

        @Override
        public V get(Long id) {
            long f = shift;
            for (int i = 0; i < sizes.length; i++) {
                long isize = sizes[i];
                long l = f + isize;
                if (id >= f && id < l) {
                    return seqLoadedPage.get(i).get(id - f);
                }
                f += isize;

            }
            return returnEmpty();
        }

        @Override
        public long size() {
            return size;
        }

    }

    public static class SubSeqPage<V> implements SeqLoadedPage<V> {

        protected long from;
        protected long to;

        protected SeqLoadedPage<V> page;

        public SubSeqPage(SeqLoadedPage<V> page, long from, long to) {
            this.page = Objects.requireNonNull(page);
            this.from = from;
            this.to = to;
        }

        @Override
        public V get(Long index) {
            return page.get(from + index);
        }

        @Override
        public long size() {
            return from - to;
        }

    }

}
