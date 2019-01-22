package lt.lb.commons;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lt.lb.commons.containers.tuples.Pair;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.interfaces.Equator.HashEquator;
import lt.lb.commons.interfaces.Iter;
import lt.lb.commons.interfaces.Iter.IterMap;
import lt.lb.commons.interfaces.Iter.IterMapNoStop;
import lt.lb.commons.interfaces.ReadOnlyIterator;
import lt.lb.commons.threads.Promise;
import lt.lb.commons.threads.UnsafeRunnable;

/**
 *
 * @author laim0nas100
 */
public class F {

    public static void unsafeRun(UnsafeRunnable r) throws RuntimeException {
        try {
            r.unsafeRun();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unsafeRunWithHandler(Consumer<Throwable> cons, UnsafeRunnable run) {
        try {
            run.unsafeRun();
        } catch (Throwable e) {
            cons.accept(e);
        }
    }

    /**
     * Run and catch any possible error
     *
     * @param r
     * @return
     */
    public static Optional<Throwable> checkedRun(UnsafeRunnable r) {
        try {
            r.unsafeRun();
            return Optional.empty();
        } catch (Throwable t) {
            return Optional.of(t);
        }
    }

    /**
     * Run and catch any possible error
     *
     * @param r
     * @return
     */
    public static Optional<Throwable> checkedRun(Runnable r) {
        try {
            r.run();
            return Optional.empty();
        } catch (Throwable t) {
            return Optional.of(t);
        }
    }

    /**
     * Static cast function. This operation is quite major, so this makes is
     * searchable.
     *
     * @param <T>
     * @param <E>
     * @param ob
     * @return
     * @throws ClassCastException
     */
    public static <T extends E, E> T cast(E ob) throws ClassCastException {
        return (T) ob;
    }

    public static boolean instanceOf(Object ob, Class... cls) {
        if (ob == null) {
            return false;
        }
        Class obClass = ob.getClass();
        return instanceOf(obClass, cls);
    }

    public static boolean instanceOf(Class obClass, Class... cls) {
        if (obClass == null) {
            return false;
        }
        return ArrayOp.any(c -> c.isAssignableFrom(obClass), cls);
    }

    public static <T> void merge(List<T> l1, List<T> l2, List<T> addTo, Comparator<T> cmp) {
        Iterator<T> i1 = l1.iterator();
        Iterator<T> i2 = l2.iterator();
        Integer c = null;
        T o1 = null;
        T o2 = null;
        while (i1.hasNext() || i2.hasNext()) {

            if (!i1.hasNext()) {
                addTo.add(i2.next());
            } else if (!i2.hasNext()) {
                addTo.add(i1.next());
            } else {
                if (c == null) {
                    o1 = i1.next();
                    o2 = i2.next();
                } else {
                    if (c > 0) {//added o2
                        o2 = i2.next();
                    } else {
                        o1 = i1.next();
                    }
                }
                c = cmp.compare(o1, o2);
                if (c > 0) {
                    addTo.add(o2);
                } else {
                    addTo.add(o1);
                }

            }
        }

    }

    public static double lerp(double start, double end, double percent) {
        return start + percent * (end - start);
    }

    public static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static void swap(List arr, int i, int j) {
        arr.set(i, arr.set(j, arr.get(i)));
    }

    public static double sigmoid(final double x) {
        return 2.0 / (1.0 + Math.exp(-4.9 * x)) - 1.0;
//        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static int StringNumCompare(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        if (len1 == len2) {
            return s1.compareTo(s2);
        }
        return len1 - len2;
    }

    public static <T> ArrayList filterInPlace(Collection<T> col, Predicate<T> pred) {
        Iterator<T> iterator = col.iterator();
        ArrayList<T> removed = new ArrayList<>();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (!pred.test(next)) {
                iterator.remove();
                removed.add(next);
            }
        }
        return removed;
    }

    /**
     * Execute filter condition check in parallel, then modify given collection.
     * Uses RandomAccess interface check to optimize removal.
     *
     * @param <T>
     * @param col
     * @param pred
     * @param exe
     * @return
     */
    public static <T> ArrayList<T> filterParallel(Collection<T> col, Predicate<T> pred, Executor exe) {
        int size = col.size();
        boolean[] satisfied = new boolean[size];

        AtomicInteger satisfiedCount = new AtomicInteger(0);
        ArrayDeque<Promise> deque = new ArrayDeque<>(size);
        F.iterate(col, (i, item) -> {
            Promise<Void> prom = new Promise(() -> {
                satisfied[i] = pred.test(item);
                if (satisfied[i]) {
                    satisfiedCount.incrementAndGet();
                }

            }).collect(deque).execute(exe);
        });

        Promise waiter = new Promise().waitFor(deque).execute(exe);

        F.unsafeRun(() -> {
            waiter.get();
        });
        if (size > satisfiedCount.get()) {
            return removeByConditionIndex(col, satisfied);
        }
        return new ArrayList<>();

    }

    private static <T> ArrayList<T> removeByConditionIndex(Collection<T> col, boolean[] satisfied) {
        ArrayList<T> removed = new ArrayList<>();
        if (col instanceof RandomAccess) { // rewrite
            ArrayList<T> kept = new ArrayList<>(satisfied.length);
            F.iterate(col, (i, item) -> {
                if (satisfied[i]) {
                    kept.add(item);
                } else {
                    removed.add(item);
                }
            });
            col.clear();
            col.addAll(kept);

        } else {
            Iterator<T> iterator = col.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                T next = iterator.next();
                if (!satisfied[i]) {
                    iterator.remove();
                    removed.add(next);
                }
                i++;
            }
        }
        return removed;
    }

    /**
     * Adds all of other collections to the base collection
     *
     * @param <T>
     * @param base collection
     * @param other array of collections to add to base
     * @return modified base collection
     */
    public static <T> Collection<T> mergeCollections(Collection<T> base, Collection<T>... other) {
        for (Collection<T> col : other) {
            base.addAll(col);
        }
        return base;
    }

    /**
     *
     * @param <T>
     * @param c1
     * @param c2
     * @param eq
     * @return
     */
    public static <T> ArrayList<T> intersection(Collection<T> c1, Collection<T> c2, Equator<T> eq) {
        ArrayList<T> common = new ArrayList<>();
        HashSet<Long> map = new HashSet<>();
        long offset = c1.size();
        F.iterate(c1, (i, obj1) -> {
            F.iterate(c2, (j, obj2) -> {
                long k1 = i;
                long k2 = offset + j;
                if (eq.equals(obj1, obj2)) {
                    if (!map.contains(k1)) {
                        map.add(k1);
                        common.add(obj1);
                    }
                    if (!map.contains(k2)) {
                        map.add(k2);
                        common.add(obj2);
                    }
                }
            });
        });
        return common;
    }

    public static <T> ArrayList<Pair<T>> intersection(Collection<T> c1, Collection<T> c2, HashEquator<T> eq) {
        ArrayList<Pair<T>> common = new ArrayList<>();
        LinkedHashMap<Object, T> m1 = new LinkedHashMap<>();

        for (T obj1 : c1) {
            Object key1 = eq.getHashable(obj1);
            m1.put(key1, obj1);
        }
        for (T obj1 : c2) {
            Object key1 = eq.getHashable(obj1);
            if (m1.containsKey(key1)) {
                common.add(new Pair<>(m1.get(key1), obj1));
            }
        }
        return common;
    }

    /**
     * Basic intersection using HashSet.
     *
     * @param <T>
     * @param c1
     * @param c2
     * @return
     */
    public static <T> HashSet<T> intersection(Collection<T> c1, Collection<T> c2) {
        HashSet<T> set = new HashSet<>(c1);
        set.retainAll(c2);
        return set;
    }

    /**
     * Fill Collection of given stream
     *
     * @param <T>
     * @param <E>
     * @param stream
     * @param col
     * @return
     */
    public static <T extends Collection<E>,E> T fillCollection(Stream<E> stream, T col) {
        stream.forEachOrdered(col::add);
        return col;
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
        ListIterator<T> iterator = list.listIterator(list.size());
        int index = list.size() - 1;
        while (iterator.hasPrevious()) {
            T next = iterator.previous();
            if (index <= from) {
                if (iter.visit(index, next)) {
                    return Optional.of(new Tuple<>(index, next));
                }
            }
            index--;
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(List<T> list, Iter.IterNoStop<T> iter) {
        return findBackwards(list, list.size(), iter);
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(List<T> list, Iter<T> iter) {
        return findBackwards(list, list.size(), iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(List<T> list, Integer from, Iter.IterNoStop<T> iter) {
        return findBackwards(list, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(T[] array, Integer from, Iter<T> iter) {
        from = Math.max(from, array.length - 1);
        for (int i = from; i >= 0; i--) {
            if (iter.visit(i, array[i])) {
                return Optional.of(new Tuple<>(i, array[i]));
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(T[] array, Iter<T> iter) {
        return findBackwards(array, array.length - 1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(T[] array, Iter.IterNoStop<T> iter) {
        return findBackwards(array, array.length - 1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(T[] array, Integer from, Iter.IterNoStop<T> iter) {
        return findBackwards(array, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Collection<T> list, Integer from, Iter<T> iter) {
        Iterator<T> iterator = list.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (index >= from) {
                if (iter.visit(index, next)) {
                    return Optional.of(new Tuple<>(index, next));
                }
            }
            index++;
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> find(T[] array, Integer from, Iter<T> iter) {
        for (int i = from; i < array.length; i++) {
            if (iter.visit(i, array[i])) {
                return Optional.of(new Tuple<>(i, array[i]));
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> find(ReadOnlyIterator<T> iterator, Integer from, Iter<T> iter) {
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (iterator.getCurrentIndex() >= from) {
                if (iter.visit(iterator.getCurrentIndex(), next)) {
                    return Optional.of(new Tuple<>(iterator.getCurrentIndex(), next));
                }
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> find(ReadOnlyIterator<T> iterator, Iter<T> iter) {
        return find(iterator, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(T[] array, Iter<T> iter) {
        return find(array, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Collection<T> list, Iter<T> iter) {
        return find(list, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Stream<T> stream, Iter<T> iter) {
        return find(stream, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Stream<T> stream, Integer from, Iter<T> iter) {
        return find(ReadOnlyIterator.of(stream), from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(T[] array, Iter.IterNoStop<T> iter) {
        return find(array, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(Collection<T> list, Iter.IterNoStop<T> iter) {
        return find(list, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(T[] array, Integer from, Iter.IterNoStop<T> iter) {
        return find(array, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(Collection<T> list, Integer from, Iter.IterNoStop<T> iter) {
        return find(list, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(ReadOnlyIterator<T> iterator, Integer from, Iter.IterNoStop<T> iter) {
        return find(iterator, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(ReadOnlyIterator<T> iterator, Iter.IterNoStop<T> iter) {
        return find(iterator, 0, iter);
    }
    
    public static <T> Optional<Tuple<Integer, T>> iterate(Stream<T> stream, Integer from, Iter.IterNoStop<T> iter) {
        return find(stream, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(Stream<T> stream, Iter.IterNoStop<T> iter) {
        return find(stream, 0, iter);
    }

}
