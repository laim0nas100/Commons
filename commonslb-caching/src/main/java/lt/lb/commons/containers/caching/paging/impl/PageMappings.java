package lt.lb.commons.containers.caching.paging.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lt.lb.commons.containers.caching.paging.LoadedPage;
import lt.lb.commons.containers.caching.paging.SeqLoadedPage;
import lt.lb.commons.containers.collections.readonly.ReadOnlyList;
import lt.lb.commons.containers.collections.readonly.ReadOnlyMap;

/**
 *
 * @author laim0nas100
 */
public class PageMappings {

    public static class MapMapping<K, T> implements ReadOnlyMap<K, T> {

        protected LoadedPage<K, T> page;
        protected Map<K, T> loaded = new HashMap<>();

        public MapMapping(LoadedPage<K, T> page) {
            this.page = Objects.requireNonNull(page);

        }

        @Override
        public int size() {
            return Long.valueOf(page.size()).intValue();
        }

        @Override
        public boolean containsKey(Object key) {
            return page.contains((K) key);
        }

        @Override
        public boolean containsValue(Object value) {
            return loaded.containsValue(value);
        }

        @Override
        public T get(Object key) {
            return loaded.computeIfAbsent((K) key, k -> page.get(k));
        }

        @Override
        public Set<K> keySet() {
            return loaded.keySet();
        }

        @Override
        public Collection<T> values() {
            return loaded.values();
        }

        @Override
        public Set<Entry<K, T>> entrySet() {
            return loaded.entrySet();
        }

    }

    public static class ListMapping<T> implements ReadOnlyList<T> {

        protected SeqLoadedPage<T> page;

        public ListMapping(SeqLoadedPage<T> page) {
            this.page = Objects.requireNonNull(page);
        }

        @Override
        public int size() {
            return Long.valueOf(page.size()).intValue();
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public int indexOf(Object o) {
            int i = -1;
            Iterator<T> it = iterator();
            while (it.hasNext()) {
                i++;
                if (Objects.equals(it.next(), o)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            int i = -1;
            int found = -1;
            Iterator<T> it = iterator();
            while (it.hasNext()) {
                i++;
                if (Objects.equals(it.next(), o)) {
                    found = i;
                }
            }
            return found;
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
            List<T> result = new ArrayList<>(toIndex - fromIndex);
            for (int idx = fromIndex; idx < toIndex; idx++) {
                result.add(get(idx));
            }
            return result;
        }

        @Override
        public boolean contains(Object o) {
            return stream().filter(item -> Objects.equals(item, o)).findAny().isPresent();
        }

        @Override
        public Iterator<T> iterator() {
            return new Itr();
        }

        @Override
        public Object[] toArray() {
            return StreamSupport.stream(spliterator(), false).toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return StreamSupport.stream(spliterator(), false).collect(Collectors.toList()).toArray(a);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return stream().collect(Collectors.toList()).containsAll(c);
        }

        @Override
        public T get(int index) {
            return page.get(index);
        }

        private class Itr implements Iterator<T> {

            protected int cursor = 0;

            @Override
            public boolean hasNext() {
                return (this.cursor < ListMapping.this.size());
            }

            @Override
            public T next() {
                try {
                    return ListMapping.this.get(this.cursor++);
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
                return this.cursor > 0;
            }

            @Override
            public T previous() {
                try {
                    return ListMapping.this.get(cursor--);
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

    public static class MappedPage<K, V, T> implements LoadedPage<K, T> {

        protected LoadedPage<K, V> page;
        protected Function<V, T> func;

        public MappedPage(LoadedPage<K, V> page, Function<V, T> func) {
            this.page = page;
            this.func = func;
        }

        @Override
        public T get(K id) {
            return func.apply(page.get(id));
        }

        @Override
        public long size() {
            return page.size();
        }

        @Override
        public boolean contains(K id) {
            return page.contains(id);
        }

    }

    public static class MappedSeqPage<V, T> extends MappedPage<Long, V, T> implements SeqLoadedPage<T> {

        public MappedSeqPage(SeqLoadedPage<V> page, Function<V, T> func) {
            super(page, func);
        }

    }

    public static class CachingPage<K, V> implements LoadedPage<K, V> {

        protected Map<K, V> map;
        protected LoadedPage<K, V> page;

        public CachingPage(LoadedPage<K, V> page) {
            this(new HashMap<>(), page);
        }

        public CachingPage(Map<K, V> map, LoadedPage<K, V> page) {
            this.map = map;
            this.page = page;
        }

        @Override
        public V get(K id) {
            return map.computeIfAbsent(id, i -> page.get(i));
        }

        @Override
        public long size() {
            return page.size();
        }

        @Override
        public boolean contains(K id) {
            if (map.containsKey(id)) {
                return true;
            }
            return page.contains(id);
        }

    }
}
