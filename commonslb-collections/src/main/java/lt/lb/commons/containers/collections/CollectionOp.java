package lt.lb.commons.containers.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.Equator;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.tuples.Pair;
import lt.lb.commons.containers.tuples.PairLeft;
import lt.lb.commons.containers.tuples.PairRight;
import lt.lb.commons.iteration.EmptyImmutableList;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.commons.misc.Range;
import lt.lb.uncheckedutils.Checked;

/**
 *
 * @author laim0nas100
 */
public class CollectionOp {

    /**
     * Contains operation but using identity instead of equals.
     *
     * @param <T>
     * @param col
     * @param item
     * @return
     */
    public static boolean containsIdentity(Collection col, Object item) {
        for (Object member : col) {
            if (member == item) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replace every item with new.Same as calling {@link Collection#clear()}
     * and {@link Collection#addAll(java.util.Collection)}
     *
     * @param <T>
     * @param <C>
     * @param collection
     * @param newItems
     * @return changed collection
     */
    public static <T, C extends Collection<T>> C replace(C collection, Collection<T> newItems) {
        collection.clear();
        collection.addAll(newItems);
        return collection;
    }

    /**
     * Replace every item with new.Same as calling {@link Collection#clear()}
     * and {@link Collection#add(java.lang.Object)} repeatedly.
     *
     * @param <T>
     * @param <C>
     * @param collection
     * @param newItems
     * @return changed collection
     */
    public static <T, C extends Collection<T>> C replace(C collection, Iterable<T> newItems) {
        collection.clear();
        for (T item : newItems) {
            collection.add(item);
        }
        return collection;
    }

    /**
     * Replace every entry with new.Same as calling {@link Map#clear()} and
     * {@link Map#putAll(java.util.Map)}
     *
     * @param <K>
     * @param <V>
     * @param <M>
     * @param map
     * @param newItems
     * @return changed map
     */
    public static <K, V, M extends Map<K, V>> M replace(M map, Map<K, V> newItems) {
        map.clear();
        map.putAll(newItems);
        return map;
    }

    /**
     * Swap item in an array
     *
     * @param <T>
     * @param arr
     * @param i
     * @param j
     */
    public static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * Swap items in a {@link List}
     *
     * @param arr
     * @param i
     * @param j
     */
    public static void swap(List arr, int i, int j) {
        arr.set(i, arr.set(j, arr.get(i)));
    }

    /**
     * Merge 2 {@link Iterable} to single {@link Collection} based on provided
     * {@link Comparator}.
     *
     * @param <T>
     * @param l1
     * @param l2
     * @param addTo
     * @param cmp
     */
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
    public static <T> ArrayList<T> filterInPlace(Collection<T> col, Predicate<T> pred) {
        ArrayList<T> removed = new ArrayList<>();
        filterInPlace(col, pred, removed::add);
        return removed;
    }

    /**
     *
     * @param <T> object type
     * @param <C> collection type
     * @param col collection to modify
     * @param pred predicate to satisfy
     * @param removedSink to collect removed items
     */
    public static <T, C extends Collection<T>> void filterInPlace(C col, Predicate<T> pred, Consumer<? super T> removedSink) {
        Iterator<T> iterator = col.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (!pred.test(next)) {
                iterator.remove();
                removedSink.accept(next);
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
                boolean sat = pred.test(item);
                satisfied[fi] = sat;
                if (sat) {
                    satisfiedCount.incrementAndGet();
                }
                return null;

            });
            deque.add(task);
            exe.execute(task);
            i++;
        }

        Checked.uncheckedRun(() -> {
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
            replace(col, kept);
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
     * Return common items in provided collections using given {@link Equator}
     * to determine equality. Intersection is based on first {@link Collection}.
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

    /**
     * * Return common items in provided collections using given
     * {@link Equator} to determine equality. Retains original items in
     * {@link Pair} object with item in corresponding slot, which identifies in
     * which {@link Collection} it was contained.
     *
     * @param <T>
     * @param c1
     * @param c2
     * @param eq
     * @return
     */
    public static <T> ArrayList<Pair<T>> intersectionPairs(Collection<T> c1, Collection<T> c2, Equator<T> eq) {

        LinkedHashMap<Equator.EqualityProxy<T>, T> m1 = new LinkedHashMap<>();
        LinkedHashMap<Equator.EqualityProxy<T>, T> m2 = new LinkedHashMap<>();

        for (T obj1 : c1) {
            m1.putIfAbsent(new Equator.EqualityProxy<>(obj1, eq), obj1);
        }
        for (T obj2 : c2) {
            m2.putIfAbsent(new Equator.EqualityProxy<>(obj2, eq), obj2);
        }
        m1.keySet().retainAll(m2.keySet());
        m2.keySet().retainAll(m1.keySet());
        //should be the same (based on equator) element set, but in optionally different order
        ArrayList<Pair<T>> common = new ArrayList<>(m1.size());
        for (Equator.EqualityProxy<T> key : m1.keySet()) {
            common.add(new Pair<>(m1.get(key), m2.get(key)));
        }
        return common;
    }

    /**
     * Return disjoint sets with their respective position signifying origin.
     * For example [0,1,3,5,7,8,9] [1,2,5,6,7,8] will return [0,3,9] and [2,6]
     * sets as {@link List} of {@link PairLeft} or {@link PairRight} as such
     * [(0,null), (3,null), (9,null), (null,2), (null,6)].
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
        MakeStream.from(left).map(m -> new Equator.EqualityProxy<>(m, eq))
                .notIn(m2).map(m -> m.getValue()).map(PairLeft::new).forEach(disjoined::add);
        MakeStream.from(right).map(m -> new Equator.EqualityProxy<>(m, eq))
                .notIn(m2).map(m -> m.getValue()).map(PairRight::new).forEach(disjoined::add);

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

    /**
     * If master collection has an item that is in any of the provided
     * {@link Iterable}. If either of arguments if empty as specified by
     * {@link CollectionOp#isEmpty} then returns {@code false}
     *
     * @param <T>
     * @param master
     * @param iterables
     * @return
     */
    public static <T> boolean containsAny(Collection master, Iterable... iterables) {
        if (isEmpty(master)) {
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

    /**
     * If master collection has an item that is in any of the provided
     * {@link Iterable}. If either of arguments if empty as specified by
     * {@link CollectionOp#isEmpty} then returns {@code false}
     *
     * @param master
     * @param arrays
     * @return
     */
    public static boolean containsAny(Collection master, Object[]... arrays) {
        if (isEmpty(master)) {
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

    /**
     * Return {@code true} when the argument is null, length is 0, or no
     * non-null or empty arrays.
     *
     * @param arrays
     * @return
     */
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
        return true;
    }

    /**
     * Return {@code true} when the argument is null, length is 0, or no
     * non-null or empty iterables.
     *
     * @param iterables
     * @return
     */
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
        return true;
    }

    /**
     * If master collection has an item that is in any of the provided
     * {@link Iterable}. If either of arguments if empty as specified by
     * {@link CollectionOp#isEmpty} then returns {@code false}
     *
     * @param master
     * @param iterables
     * @return
     */
    public static boolean containsAll(Collection master, Iterable... iterables) {
        if (isEmpty(master)) {
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

    /**
     * If master collection has an item that is in any of the provided arrays.
     * If either of arguments if empty as specified by
     * {@link CollectionOp#isEmpty} then returns {@code false}
     *
     * @param master
     * @param arrays
     * @return
     */
    public static boolean containsAll(Collection master, Object[]... arrays) {
        if (isEmpty(master)) {
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

    /**
     * Lazily add all items. Make {@link Collection} if given collection is null
     * AND there are some items to add, even if the item is null.
     *
     * @param <T>
     * @param <C>
     * @param maker
     * @param col
     * @param items
     * @return
     */
    public static <T, C extends Collection<T>> C lazyAdd(Supplier<C> maker, C col, T... items) {
        if (items.length == 0) {
            return col;
        } else {
            if (col == null) {
                col = maker.get();
            }
        }
        if (items.length == 1) {
            col.add(items[0]);
        } else {
            col.addAll(Arrays.asList(items));
        }

        return col;
    }

    /**
     * Lazily add all items. Make {@link Collection} if given collection is null
     * AND there are some items to add, even if the item is null.
     *
     * @param <T>
     * @param <C>
     * @param maker
     * @param col
     * @param items
     * @return
     */
    public static <T, C extends Collection<T>> C lazyAdd(Supplier<C> maker, C col, Iterable<T> items) {

        if (items == null || (!items.iterator().hasNext())) {
            return col;
        } else {
            if (col == null) {
                col = maker.get();
            }
        }

        for (T item : items) {
            col.add(item);
        }

        return col;
    }

    /**
     * Checks if collection is null or empty then returns a null, otherwise
     * makes a collection, adds all given items to it and returns it
     *
     * @param <T>
     * @param <C>
     * @param maker
     * @param items
     * @return
     */
    public static <T, C extends Collection<T>> C lazyInit(Function<C, C> maker, C items) {

        if (items == null || items.isEmpty()) {
            return null;
        } else {
            return maker.apply(items);
        }
    }

    /**
     * Checks if collection is null or empty and returns empty immutable list to
     * iterate, otherwise returns a given collection
     *
     * @param <T>
     * @param <C>
     * @param col
     * @return
     */
    public static <T, C extends Collection<T>> Iterable<T> lazyIterable(C col) {
        if (col == null || col.isEmpty()) {
            return EmptyImmutableList.getInstance();
        }
        return col;
    }

    /**
     * Do a batching operation using maps.
     *
     * @param <K>
     * @param <V>
     * @param batch size of a batch
     * @param myMap the map to take items from
     * @param consMap what to do with items
     */
    public static <K, V> void doBatchMap(int batch, Map<K, V> myMap, Consumer<Map<K, V>> consMap) {
        Objects.requireNonNull(myMap);
        doBatch(batch,
                () -> new LinkedHashMap<>(batch),
                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                myMap.entrySet().iterator(),
                consMap
        );
    }

    /**
     * Do a batching operation using lists.
     *
     * @param <T>
     * @param batch size of a batch
     * @param collection the collection to take items from
     * @param cons what to do with items
     */
    public static <T> void doBatchList(int batch, Collection<T> collection, Consumer<List<T>> cons) {
        Objects.requireNonNull(collection);
        doBatch(batch,
                () -> new ArrayList<>(batch),
                (list, item) -> {
                    list.add(item);
                },
                collection.iterator(),
                cons
        );
    }

    /**
     * Do a batching operation using sets.
     *
     * @param <T>
     * @param batch size of a batch
     * @param collection the collection to take items from
     * @param cons what to do with items
     */
    public static <T> void doBatchSet(int batch, Collection<T> collection, Consumer<Set<T>> cons) {
        Objects.requireNonNull(collection);
        doBatch(batch,
                () -> new LinkedHashSet<>(batch),
                (set, item) -> {
                    set.add(item);
                },
                collection.iterator(),
                cons
        );
    }

    /**
     * Do a batching operation.
     *
     * @param <T>
     * @param <C>
     * @param batch size of a batch
     * @param bagSupply new bag supplier
     * @param addFunc how to add to a bag
     * @param iterator items to batch iterator
     * @param cons what to do with items
     */
    public static <T, C> void doBatch(int batch, Supplier<C> bagSupply, BiConsumer<C, T> addFunc, Iterator<T> iterator, Consumer<C> cons) {
        Objects.requireNonNull(bagSupply);
        Objects.requireNonNull(addFunc);
        Objects.requireNonNull(iterator);
        Objects.requireNonNull(cons);
        if (batch <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        if (!iterator.hasNext()) {
            return;
        }
        long i = 0;
        C bag = bagSupply.get();
        while (iterator.hasNext()) {
            T next = iterator.next();
            addFunc.accept(bag, next);
            i++;
            if (i % batch == 0) {
                cons.accept(bag);
                bag = bagSupply.get();
            }

        }
        if (i % batch != 0) { // some items left
            cons.accept(bag);
        }
    }

    /**
     * Each iteration tests the middle element (high - low) / 2.If its found,
     * return the index, otherwise fire 2 recursive shots dividing the search
     * bounds in half like so [mid + 1, high] and [low, mid - 1].
     *
     * @param <T>
     * @param iter maximum amount of recursive iterations left
     * @param maxDiff maximum difference (high - low) allowed, basically
     * subdivision granularity.
     * @param preferLast whether to search in the end of the list first
     * @param list the list in question
     * @param suitable predicate
     * @param low lower bound
     * @param high higher bound
     * @return
     */
    public static <T> int recursiveBinarySearchShot(int iter, int maxDiff, boolean preferLast, List<T> list, Predicate<T> suitable, int low, int high) {
        if (iter > 0 && low < high && maxDiff <= high - low) {
            iter--;
            int mid = (low + high) >> 1;
            boolean test = suitable.test(list.get(mid));
            if (test) {
                return mid;
            } else {
                // shoot 2 directions

                if (preferLast) {
                    int foundHigh = recursiveBinarySearchShot(iter, maxDiff, preferLast, list, suitable, mid + 1, high);
                    return foundHigh >= 0 ? foundHigh : recursiveBinarySearchShot(iter, maxDiff, preferLast, list, suitable, low, mid - 1);
                } else {
                    int foundLow = recursiveBinarySearchShot(iter, maxDiff, preferLast, list, suitable, low, mid - 1);
                    return foundLow >= 0 ? foundLow : recursiveBinarySearchShot(iter, maxDiff, preferLast, list, suitable, mid + 1, high);
                }

            }
        }
        return -1;
    }

    /**
     * Not suitable for searching individual elements. For that use the function
     * {@link CollectionOp#recursiveBinarySearchShot(int, int, java.util.List, java.util.function.Predicate, int, int)}
     *
     * Provide a list where there are a continuous run of elements that satisfy
     * some condition.This algorithm tries to return the last/first element of
     * the continuous run by first detecting the run using broad binary search
     * and the using binary search to find the end of the found run.
     *
     * @param <T>
     * @param iter how many recursive iterations to do
     * @param last to find first or last element of the continuous run
     * @param list
     * @param suitable predicate that defines the continuous run
     * @return
     */
    public static <T> int binarySearchContinuousRunSuitable(int iter, boolean last, List<T> list, Predicate<T> suitable) {
        //binary search to find last suitable
        Nulls.requireNonNulls(list, suitable);

        if (list.isEmpty()) {
            return -1;
        }
        if (list.size() == 1) {
            return suitable.test(list.get(0)) ? 0 : -1;
        }

        int low = 0;
        int high = list.size() - 1;

        int maxDiff = Range.of(1, 32).clamp(list.size() / 10);// roughly 10% of the list
        int firstFound = recursiveBinarySearchShot(iter, maxDiff, last, list, suitable, low, high);

        int lastSuitable = firstFound;

        int mid = firstFound >= 0 ? firstFound : (low + high) / 2;
        while (iter > 0 && low <= high) {
            iter--;
            boolean test = suitable.test(list.get(mid));

            if (test) {
                lastSuitable = mid;
                if (last) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            } else {
                if (last) {
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            }

            mid = (low + high) >> 1;
        }

        return lastSuitable;
    }
}
