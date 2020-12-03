package lt.lb.commons.iteration;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lt.lb.commons.containers.tuples.Tuple;

/**
 *
 * @author laim0nas100
 * Iterator which passes index and value
 *
 */
@Deprecated
public interface Iter<Type> {

    /**
     *
     * @param index
     * @param value
     * @return true = break, false = continue
     */
    public boolean visit(Integer index, Type value);

    public static interface IterNoStop<T> extends Iter<T> {

        @Override
        public default boolean visit(Integer index, T value) {
            this.continuedVisit(index, value);
            return false;
        }

        public void continuedVisit(Integer index, T value);
    }

    public static interface IterMap<K, V> {

        public boolean visit(K key, V value);
    }

    public static interface IterMapNoStop<K, V> extends IterMap<K, V> {

        @Override
        public default boolean visit(K key, V value) {
            this.continuedVisit(key, value);
            return false;
        }

        public void continuedVisit(K key, V value);
    }
    
    
    
    
    
    public static <K, V> Optional<Tuple<K, V>> find(Map<K, V> map, IterMap<K, V> iter) {
        Set<Map.Entry<K, V>> entrySet = map.entrySet();
        for (Map.Entry<K, V> entry : entrySet) {
            K k = entry.getKey();
            V v = entry.getValue();
            if (iter.visit(k, v)) {
                return Optional.of(new Tuple<>(k, v));
            }
        }
        return Optional.empty();
    }

    public static <K, V> Optional<Tuple<K, V>> iterate(Map<K, V> map, IterMapNoStop<K, V> iter) {
        return find(map, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(List<T> list, Integer from, Iter<T> iter) {
        ListIterator<T> iterator = list.listIterator(from);
        while (iterator.hasPrevious()) {
            from--;
            T next = iterator.previous();
            if (iter.visit(from, next)) {
                return Optional.of(new Tuple<>(from, next));
            }

        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(List<T> list, Iter<T> iter) {
        return findBackwards(list, list.size(), iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(List<T> list, Integer from, Iter.IterNoStop<T> iter) {
        return findBackwards(list, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(List<T> list, Iter.IterNoStop<T> iter) {
        return findBackwards(list, list.size(), iter);
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(T[] array, Integer from, Iter<T> iter) {
        from = Math.min(from, array.length - 1);
        for (int i = from; i >= 0; i--) {
            if (iter.visit(i, array[i])) {
                return Optional.of(new Tuple<>(i, array[i]));
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(T[] array, Integer from, Iter.IterNoStop<T> iter) {
        return findBackwards(array, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(T[] array, Iter.IterNoStop<T> iter) {
        return findBackwards(array, array.length - 1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(T[] array, Iter<T> iter) {
        return findBackwards(array, array.length - 1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(List<T> list, Integer from, Integer to, Iter<T> iter) {
        Iterator<T> iterator = list.listIterator(from);
        while (iterator.hasNext()) {
            if (to >= 0 && from >= to) {
                return Optional.empty();
            }
            T next = iterator.next();
            if (iter.visit(from, next)) {
                return Optional.of(new Tuple<>(from, next));
            }
            from++;
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> find(List<T> list, Integer from, Iter<T> iter) {
        return find(list, from, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(List<T> list, Iter<T> iter) {
        return find(list, 0, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Iterable<T> list, Integer from, Integer to, Iter<T> iter) {
        return find(ReadOnlyIterator.of(list), from, to, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Iterable<T> list, Integer from, Iter<T> iter) {
        return find(ReadOnlyIterator.of(list), from, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Iterable<T> list, Iter<T> iter) {
        return find(ReadOnlyIterator.of(list), 0, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(ReadOnlyIterator<T> iterator, Integer from, Integer to, Iter<T> iter) {
        Integer index = -1;
        while (iterator.hasNext()) {
            index++;
            T next = iterator.next();
            if (to >= 0 && index >= to) {
                return Optional.empty();
            }
            if (index >= from) {
                if (iter.visit(index, next)) {
                    return Optional.of(new Tuple<>(index, next));
                }
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> find(ReadOnlyIterator<T> iterator, Integer from, Iter<T> iter) {
        return find(iterator, from, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(ReadOnlyIterator<T> iterator, Iter<T> iter) {
        return find(iterator, 0, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Iterator<T> iterator, Integer from, Integer to, Iter<T> iter) {
        Integer index = -1;
        while (iterator.hasNext()) {
            index++;
            T next = iterator.next();
            if (to >= 0 && index >= to) {
                return Optional.empty();
            }
            if (index >= from) {
                if (iter.visit(index, next)) {
                    return Optional.of(new Tuple<>(index, next));
                }
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> find(Iterator<T> iterator, Integer from, Iter<T> iter) {
        return find(iterator, from, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Iterator<T> iterator, Iter<T> iter) {
        return find(iterator, 0, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(T[] array, Integer from, Integer to, Iter<T> iter) {
        if (to < 0) {
            to = array.length;
        }
        for (int i = from; i < to; i++) {
            if (iter.visit(i, array[i])) {
                return Optional.of(new Tuple<>(i, array[i]));
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> find(T[] array, Integer from, Iter<T> iter) {
        return find(array, from, array.length, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(T[] array, Iter<T> iter) {
        return find(array, 0, array.length, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Stream<T> stream, Integer from, Integer to, Iter<T> iter) {
        return find(ReadOnlyIterator.of(stream), from, to, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Stream<T> stream, Integer from, Iter<T> iter) {
        return find(ReadOnlyIterator.of(stream), from, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Stream<T> stream, Iter<T> iter) {
        return find(stream, 0, -1, iter);
    }

    public static <T> void iterate(List<T> list, Integer from, Integer to, Iter.IterNoStop<T> iter) {
        find(list, from, to, iter);
    }

    public static <T> void iterate(List<T> list, Integer from, Iter.IterNoStop<T> iter) {
        find(list, from, -1, iter);
    }

    public static <T> void iterate(List<T> list, Iter.IterNoStop<T> iter) {
        find(list, 0, -1, iter);
    }

    public static <T> void iterate(T[] array, Integer from, Integer to, Iter.IterNoStop<T> iter) {
        find(array, from, to, iter);
    }

    public static <T> void iterate(T[] array, Integer from, Iter.IterNoStop<T> iter) {
        find(array, from, array.length, iter);
    }

    public static <T> void iterate(T[] array, Iter.IterNoStop<T> iter) {
        find(array, 0, array.length, iter);
    }

    public static <T> void iterate(Iterator<T> iterator, Integer from, Integer to, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(iterator), from, to, iter);
    }

    public static <T> void iterate(Iterator<T> iterator, Integer from, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(iterator), from, iter);
    }

    public static <T> void iterate(Iterator<T> iterator, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(iterator), 0, -1, iter);
    }

    public static <T> void iterate(Iterable<T> list, Integer from, Integer to, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(list), from, to, iter);
    }

    public static <T> void iterate(Iterable<T> list, Integer from, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(list), from, -1, iter);
    }

    public static <T> void iterate(Iterable<T> list, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(list), 0, -1, iter);
    }

    public static <T> void iterate(ReadOnlyIterator<T> iterator, Integer from, Integer to, Iter.IterNoStop<T> iter) {
        find(iterator, from, to, iter);
    }

    public static <T> void iterate(ReadOnlyIterator<T> iterator, Integer from, Iter.IterNoStop<T> iter) {
        find(iterator, from, -1, iter);
    }

    public static <T> void iterate(ReadOnlyIterator<T> iterator, Iter.IterNoStop<T> iter) {
        find(iterator, 0, -1, iter);
    }

    public static <T> void iterate(Stream<T> stream, Integer from, Integer to, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(stream), from, to, iter);
    }

    public static <T> void iterate(Stream<T> stream, Integer from, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(stream), from, -1, iter);
    }

    public static <T> void iterate(Stream<T> stream, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(stream), 0, -1, iter);
    }

}
