package lt.lb.commons;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.containers.tuples.Pair;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.func.StreamMapper;
import lt.lb.commons.func.StreamMapper.StreamDecorator;
import lt.lb.commons.func.StreamMappers;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.iteration.Iter.IterMap;
import lt.lb.commons.iteration.Iter.IterMapNoStop;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.threads.Promise;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.func.unchecked.UnsafeSupplier;

/**
 *
 * @author laim0nas100
 */
public class F {

    /**
     * Convenience wrapped null check instead of ? operator avoid duplication of
     * object when using ? operator. If java 9 is available, use
     * Object.requireNotNullElse
     *
     * @param <T>
     * @param object
     * @param nullCase
     * @return
     */
    public static <T> T nullWrap(T object, T nullCase) {
        return object == null ? Objects.requireNonNull(nullCase, "nullCase is null") : object;
    }

    /**
     * Convenience wrapped null check instead of ? operator avoid duplication of
     * object when using ? operator. If java 9 is available, use
     * Object.requireNotNullElseGet
     *
     * @param <T>
     * @param object
     * @param nullCaseSup
     * @return
     */
    public static <T> T nullSupp(T object, Supplier<T> nullCaseSup) {
        if (object == null) {
            return Objects.requireNonNull(Objects.requireNonNull(nullCaseSup, "supplier").get(), "supplier.get()");
        }
        return object;
    }

    /**
     * Convenience wrapped if check instead of ? operator avoid duplication of
     * trueCase when using ? operator
     *
     * @param <T>
     * @param trueCase
     * @param falseCase
     * @param pred
     * @return
     */
    public static <T> T ifWrap(T trueCase, T falseCase, Predicate<T> pred) {
        return pred.test(trueCase) ? trueCase : falseCase;
    }

    /**
     *
     * Apply function on closeable and then close it. Ignore exceptions both
     * times. Return null if error occurred during mapper function execution.
     *
     * @param <T>
     * @param <U>
     * @param closeable
     * @param mapper
     * @return
     */
    public static <T extends Closeable, U> U safeClose(T closeable, Function<? super T, ? extends U> mapper) {
        U val = F.checkedCallNoExceptions(() -> mapper.apply(closeable));
        F.checkedRun(() -> {
            closeable.close();
        });
        return val;

    }

    /**
     * Run with wrapping exception
     *
     * @param r
     * @throws NestedException
     */
    public static void unsafeRun(UnsafeRunnable r) throws NestedException {
        try {
            r.unsafeRun();
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }

    /**
     * Call with wrapping exception
     *
     * @param <T>
     * @param call
     * @return
     * @throws NestedException
     */
    public static <T> T unsafeCall(UnsafeSupplier<T> call) throws NestedException {
        try {
            return call.unsafeGet();
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }

    /**
     * Run with wrapping exception inside handler
     *
     * @param cons
     * @param run
     */
    public static void unsafeRunWithHandler(Consumer<Throwable> cons, UnsafeRunnable run) {
        try {
            run.unsafeRun();
        } catch (Throwable e) {
            cons.accept(NestedException.unwrap(e));
        }
    }

    /**
     * Call with wrapping exception inside handler
     *
     * @param <T>
     * @param cons
     * @param call
     * @return result or {@code null} if exception was thrown
     */
    public static <T> T unsafeCallWithHandler(Consumer<Throwable> cons, UnsafeSupplier<T> call) {
        try {
            return call.unsafeGet();
        } catch (Throwable e) {
            cons.accept(NestedException.unwrap(e));
        }
        return null;
    }

    /**
     * Call with ignoring all exceptions. Returns null, if execution fails.
     *
     * @param <T>
     * @param call
     * @return result or {@code null} if exception was thrown
     */
    public static <T> T checkedCallNoExceptions(UnsafeSupplier<T> call) {
        try {
            return call.unsafeGet();
        } catch (Throwable e) {
        }
        return null;
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
            return Optional.of(t).map(m -> NestedException.unwrap(m));
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
            return Optional.of(t).map(m -> NestedException.unwrap(m));
        }
    }

    /**
     * Static cast function. Cast operation is quite significant, so this makes
     * is searchable.
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

    /**
     *
     * @param <T> object type
     * @param col collection to modify
     * @param pred predicate to satisfy
     * @return ArrayList of removed items
     */
    public static <T> ArrayList filterInPlace(Collection<T> col, Predicate<T> pred) {
        ArrayList<T> removed = new ArrayList<>();
        filterInPlace(col, pred, removed);
        return removed;
    }

    /**
     *
     * @param <T> object type
     * @param <C> collection type
     * @param col collection to modify
     * @param pred predicate to satisfy
     * @param removed collection to collect removed items
     */
    public static <T, C extends Collection<T>> void filterInPlace(C col, Predicate<T> pred, C removed) {
        Iterator<T> iterator = col.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (!pred.test(next)) {
                iterator.remove();
                removed.add(next);
            }
        }
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
            new Promise(() -> {
                satisfied[i] = pred.test(item);
                if (satisfied[i]) {
                    satisfiedCount.incrementAndGet();
                }

            }).collect(deque).execute(exe);
        });

        F.unsafeRun(() -> {
            new Promise().waitFor(deque).execute(exe).get();
        });
        int remove = size - satisfiedCount.get();
        if (remove > 0) {
            return removeByConditionIndex(col, satisfied, remove);
        }
        return new ArrayList<>();

    }

    private static <T> ArrayList<T> removeByConditionIndex(Collection<T> col, boolean[] satisfied, int removedSize) {
        ArrayList<T> removed = new ArrayList<>(removedSize);
        if (col instanceof RandomAccess) { // rewrite
            ArrayList<T> kept = new ArrayList<>(satisfied.length - removedSize);
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

        LinkedHashSet<Equator.EqualityProxy<T>> m1 = new LinkedHashSet<>();
        LinkedHashSet<Equator.EqualityProxy<T>> m2 = new LinkedHashSet<>();

        for (T obj1 : c1) {
            m1.add(new Equator.EqualityProxy<>(obj1, eq));
        }
        for (T obj1 : c2) {
            m2.add(new Equator.EqualityProxy<>(obj1, eq));
        }
        m1.retainAll(m2);
        ArrayList<T> common = new ArrayList<>(m1.size());
        for (Equator.EqualityProxy<T> pro : m1) {
            common.add(pro.getValue());
        }
        return common;
    }

    public static <T> ArrayList<Pair<T>> intersectionPairs(Collection<T> c1, Collection<T> c2, Equator<T> eq) {

        LinkedHashSet<Equator.EqualityProxy<T>> m1 = new LinkedHashSet<>();
        LinkedHashSet<Equator.EqualityProxy<T>> m2 = new LinkedHashSet<>();

        for (T obj1 : c1) {
            m1.add(new Equator.EqualityProxy<>(obj1, eq));
        }
        for (T obj1 : c2) {
            m2.add(new Equator.EqualityProxy<>(obj1, eq));
        }
        m1.retainAll(m2);
        m2.retainAll(m1);
        ArrayList<Pair<T>> common = new ArrayList<>(m1.size());
        for (Equator.EqualityProxy<T> pro : m1) {
            common.add(new Pair<>(pro.getValue(), null));
        }
        F.iterate(m2, (i, pro) -> {
            common.get(i).setG2(pro.getValue());
        });
        
        return common;
    }
    
    public static <T> ArrayList<Pair<T>> disjunctionPairs(Collection<T> c1, Collection<T> c2, Equator<T> eq) {

        LinkedHashSet<Equator.EqualityProxy<T>> m1 = new LinkedHashSet<>();
        LinkedHashSet<Equator.EqualityProxy<T>> m2 = new LinkedHashSet<>();

        for (T obj1 : c1) {
            m1.add(new Equator.EqualityProxy<>(obj1, eq));
        }
        for (T obj1 : c2) {
            m2.add(new Equator.EqualityProxy<>(obj1, eq));
        }
        m1.retainAll(m2);
        m2.retainAll(m1);
        ArrayList<Pair<T>> disjunct = new ArrayList<>();
        
        StreamMapper<T, T> map = new StreamDecorator<T>()
                .map(m-> new Equator.EqualityProxy<>(m, eq))
                .apply(StreamMappers.filterIn(m2))
                .map(m->m.getValue());
        
        List<Pair<T>> opt1 = map.map(m-> new Pair<>(m,null)).collectToList().startingWithOpt(c1);
        List<Pair<T>> opt2 = map.map(m-> new Pair<>(null,m)).collectToList().startingWithOpt(c1);
                
        disjunct.addAll(opt1);
        disjunct.addAll(opt2);
        return disjunct;
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
        from = Math.max(from, array.length - 1);
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

    public static <T> Optional<Tuple<Integer, T>> find(Collection<T> list, Integer from, Integer to, Iter<T> iter) {
        return find(ReadOnlyIterator.of(list), from, to, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Collection<T> list, Integer from, Iter<T> iter) {
        return find(ReadOnlyIterator.of(list), from, -1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Collection<T> list, Iter<T> iter) {
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

    public static <T> void iterate(Collection<T> list, Integer from, Integer to, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(list), from, to, iter);
    }

    public static <T> void iterate(Collection<T> list, Integer from, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(list), from, -1, iter);
    }

    public static <T> void iterate(Collection<T> list, Iter.IterNoStop<T> iter) {
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
