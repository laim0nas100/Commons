package lt.lb.commons.containers.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import lt.lb.commons.F;
import lt.lb.commons.containers.tuples.Pair;
import lt.lb.commons.containers.tuples.PairLeft;
import lt.lb.commons.containers.tuples.PairRight;
import lt.lb.commons.Equator;
import lt.lb.commons.iteration.streams.StreamMapper;
import lt.lb.commons.iteration.streams.StreamMapper.StreamDecorator;
import lt.lb.commons.iteration.streams.StreamMappers;
import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author laim0nas100
 */
public class CollectionOp {

    public static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static void swap(List arr, int i, int j) {
        arr.set(i, arr.set(j, arr.get(i)));
    }

    public static <T> void merge(Iterable<T> l1, Iterable<T> l2, Collection<T> addTo, Comparator<T> cmp) {
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
        ArrayDeque<Future> deque = new ArrayDeque<>(size);
        int i = 0;
        for (T item : col) {
            final int fi = i;
            FutureTask<Void> task = new FutureTask<>(() -> {
                satisfied[fi] = pred.test(item);
                if (satisfied[fi]) {
                    satisfiedCount.incrementAndGet();
                }
                return null;

            });
            deque.add(task);
            exe.execute(task);
            i++;
        }

        F.unsafeRun(() -> {
            for (Future future : deque) {
                future.get();
            }
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
            int i = 0;
            for (T item : col) {
                if (satisfied[i]) {
                    kept.add(item);
                } else {
                    removed.add(item);
                }
                i++;
            }
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
        m2.retainAll(m1);//should be the same (based on equator) element set, but in optionally different order
        ArrayList<Pair<T>> common = new ArrayList<>(m1.size());
        for (Equator.EqualityProxy<T> pro : m1) {
            common.add(new Pair<>(pro.getValue(), null));
        }
        int i = 0;
        for (Equator.EqualityProxy<T> pro : m2) {
            common.get(i).setG2(pro.getValue());
            i++;
        }

        return common;
    }

    /**
     * Return disjoint sets with their respective position as origin. For
     * example [1,3,5,7,8,9] [1,2,5,6,7,8] will return [3,9] and [2,6]
     *
     * @param <T>
     * @param left
     * @param right
     * @param eq
     * @return
     */
    public static <T> ArrayList<Pair<T>> disjointPairs(Collection<T> left, Collection<T> right, Equator<T> eq) {

        LinkedHashSet<Equator.EqualityProxy<T>> m1 = new LinkedHashSet<>();
        LinkedHashSet<Equator.EqualityProxy<T>> m2 = new LinkedHashSet<>();

        for (T obj1 : left) {
            m1.add(new Equator.EqualityProxy<>(obj1, eq));
        }
        for (T obj1 : right) {
            m2.add(new Equator.EqualityProxy<>(obj1, eq));
        }
        m1.retainAll(m2);
        m2.retainAll(m1); // get union
        ArrayList<Pair<T>> disjoined = new ArrayList<>();

        StreamMapper<T, T> map = new StreamDecorator<T>()
                .map(m -> new Equator.EqualityProxy<>(m, eq))
                .apply(StreamMappers.filterNotIn(m2))
                .map(m -> m.getValue());

        map.map(m -> new PairLeft<>(m)).forEach(item -> disjoined.add(item)).startingWithOpt(left);
        map.map(m -> new PairRight<>(m)).forEach(item -> disjoined.add(item)).startingWithOpt(right);

        return disjoined;
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

    public static <T> boolean containsAny(Collection master, Iterable... iterables) {
        if(isEmpty(master)){
            return false;
        }

        if (isEmpty(iterables)) {
            return false;
        }

        for (Iterable iter : iterables) {
            for (Object ob : iter) {
                if (master.contains(ob)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsAny(Collection master, Object[]... arrays) {
        if(isEmpty(master)){
            return false;
        }
        if (isEmpty(arrays)) {
            return false;
        }

        for (Object[] array : arrays) {
            for (Object t : array) {
                if (master.contains(t)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isEmpty(Object[]... arrays) {
        if (arrays == null) {
            return true;
        }
        if (arrays.length == 0) {
            return true;
        }
        for (Object[] array : arrays) {
            if (array != null && array.length > 0) {
                return false;
            }
        }
        return false;
    }

    public static boolean isEmpty(Iterable... iterables) {
        if (iterables == null) {
            return true;
        }
        if (iterables.length == 0) {
            return true;
        }
        for (Iterable iterable : iterables) {
            if (iterable != null) {
                for (Object o : iterable) {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean containsAll(Collection master, Iterable... iterables) {
        if(isEmpty(master)){
            return false;
        }

        if (isEmpty(iterables)) {
            return false;
        }

        for (Iterable iter : iterables) {
            for (Object ob : iter) {
                if (!master.contains(ob)) {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean containsAll(Collection master, Object[]... arrays) {
        if(isEmpty(master)){
            return false;
        }

        if (isEmpty(arrays)) {
            return false;
        }

        for (Object[] array : arrays) {
            for (Object t : array) {
                if (!master.contains(t)) {
                    return true;
                }
            }
        }
        return false;
    }
}
