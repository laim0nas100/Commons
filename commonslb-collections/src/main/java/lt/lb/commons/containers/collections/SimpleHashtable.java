package lt.lb.commons.containers.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import lt.lb.commons.Equator;
import lt.lb.commons.iteration.streams.MakeStream;

/**
 *
 * Reasonable implementation, immutable keyset and value collection. About 40%
 * performance downgrade compared to HashHap.
 *
 * Adapted from Johnathan Blow implementation.
 *
 * Overridable equals and hashcode operation.
 *
 * @author laim0nas100
 */
public class SimpleHashtable<K, V> implements Map<K, V> {

    protected final Equator<K> equator;

    public static final int DEFAULT_SIZE = 16;
    public static final int SIZE_TO_CHECK = DEFAULT_SIZE - (DEFAULT_SIZE / 2);
    public static final float DEFAULT_LOAD_FACTOR = 0.70f;
    protected SHEntry[] table;

    protected final boolean ordered;
    protected final float loadFactor;
    protected final int loadFactorPercent;

    public SimpleHashtable() {
        this(DEFAULT_SIZE, true, DEFAULT_LOAD_FACTOR, Equator.simpleHashEquator());
    }

    public SimpleHashtable(int size) {
        this(size, true, DEFAULT_LOAD_FACTOR, Equator.simpleHashEquator());
    }

    public SimpleHashtable(int size, boolean ordered, float loadFactor) {
        this(size, ordered, loadFactor, Equator.simpleHashEquator());
    }

    public SimpleHashtable(int size, boolean ordered, float loadFactor, Equator<K> equator) {
        if (size < 1) {
            throw new IllegalArgumentException("Invalid size:" + size);
        }
        table = new SHEntry[nextPowerOfTwo(DEFAULT_SIZE, Math.max(size, DEFAULT_SIZE))];
        this.ordered = ordered;
        this.loadFactor = Math.max(Math.min(loadFactor, .90f), .20f);
        this.loadFactorPercent = (int) (100 * this.loadFactor);
        this.equator = equator;
        this.counter = Integer.MIN_VALUE;
    }

    public static class SHEntry<K, V> implements Entry<K, V>, Comparable<SHEntry> {

        private int hash;
        private K key;
        private V value;
        private boolean occupied = true;
        private int order;

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public int compareTo(SHEntry o) {
            return Integer.compare(this.order, o.order);
        }

        @Override
        public String toString() {
            return getKey() + " => " + getValue();
        }

    }
    int count;
    int counter;

    @Override
    public int size() {
        return count;
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    private static int nextPowerOfTwo(int current, int desiredSize) {
        int size = current;
        if (current <= desiredSize) { // bigger
            while (size < desiredSize) {
                size += size;
            }
        } else {
            while (size / 2 > desiredSize) {
                size = size / 2;
            }
        }
        return size;

    }

    protected void tableResize(int reqSize) {
        if (reqSize < SIZE_TO_CHECK) {
            return;
        }

        // filled / allocated >= 70/100 ... therefore
        // filled * 100 >= allocated * 70
        int newSize = table.length;
        if ((reqSize * 100) >= table.length * loadFactorPercent) {
            newSize = newSize + newSize;
        } else {
            if ((table.length - reqSize) * loadFactorPercent > (reqSize + reqSize) * 100) { // too empty
                newSize = newSize / 2;
            }
        }

        newSize = Math.max(DEFAULT_SIZE, newSize);
        if (newSize == table.length) {
            return;
        }
//        System.out.println("Size need at least " + reqSize + " so change " + table.length + " => " + newSize);
        SHEntry[] newtable = new SHEntry[newSize];

        for (SHEntry<K, V> entry : table) {
            if (entry == null) {
                continue;
            }
            if (!entry.occupied) {
                continue;
            }
            //rehash
            putInbounds(newtable, entry, false);
        }
        table = newtable;

    }

    protected void tableResizeOld(int reqSize) {
        if (reqSize < DEFAULT_SIZE) {
            return;
        }

        int expectedSize = (int) (reqSize / loadFactor);
        int newSize = nextPowerOfTwo(table.length, expectedSize);

        newSize = Math.max(DEFAULT_SIZE, newSize);
        if (newSize == table.length) {
            return;
        }
        SHEntry[] newtable = new SHEntry[newSize];

        for (SHEntry<K, V> entry : table) {
            if (entry == null) {
                continue;
            }
            if (!entry.occupied) {
                continue;
            }
            //rehash
            putInbounds(newtable, entry, false);
        }
        table = newtable;

    }

    public static class Returnable<T> {

        public final T val;
        public final boolean exit;

        public static final Returnable emptyContinue = new Returnable(null, false);
        public static final Returnable emptyExit = new Returnable(null, true);

        private Returnable(T val, boolean exit) {
            this.val = val;
            this.exit = exit;
        }

        public static <T> Returnable<T> end(T val) {
            if (val == null) {
                return end();
            }
            return new Returnable<>(val, true);
        }

        public static <T> Returnable<T> cont(T val) {
            if (val == null) {
                return cont();
            }
            return new Returnable<>(val, false);
        }

        public static <T> Returnable<T> end() {
            return emptyExit;
        }

        public static <T> Returnable<T> cont() {
            return emptyContinue;
        }

    }

    protected <T> T walkTable(SHEntry[] tab, final int hash, Function<Integer, Returnable<T>> check) {
        int probeIncrement = 1;
        final int mask = tab.length - 1;
        int index = hash & mask;
        Returnable<T> apply = check.apply(index);
        while (!apply.exit) {
            index = (index + probeIncrement) & mask;
            probeIncrement += 1;
            apply = check.apply(index);
        }
        return apply.val;
    }

    protected int findFirstIndex(SHEntry[] tab, final int hash, final boolean replace) {
        int probeIncrement = 1;
        final int mask = tab.length - 1;
        int index = hash & mask;

        while (true) {
            if (tab[index] == null) { // easy case, empty spot
                return index;
            }
            if (replace && tab[index].occupied) {
                return index;
            } else {
                index = (index + probeIncrement) & mask;
                probeIncrement += 1;
            }

        }
    }

    protected SHEntry<K, V> putInbounds(SHEntry[] tab, SHEntry entry, final boolean incrementCount) {
        return walkTable(tab, entry.hash, index -> {
            SHEntry<K, V> slot = tab[index];
            if (slot == null) {//easy
                tab[index] = entry;

                count += incrementCount ? 1 : 0;
                return Returnable.end(); // exit;
            }
            if (!slot.occupied) {
                tab[index] = entry;
                count += incrementCount ? 1 : 0;
                return Returnable.end(slot);
            }
            if (slot.occupied && equate(slot.key, entry.key)) { // replace
                tab[index] = entry;
                // count unchanged
                return Returnable.end(slot);
            }
            return Returnable.cont();
        });
    }

    protected SHEntry<K, V> put(SHEntry[] tab, K key, V val) {
        SHEntry<K, V> entry = new SHEntry<>();
        entry.hash = hash(key);
        entry.key = key;
        entry.value = val;
        entry.occupied = true;
        entry.order = counter;
        counter += 1;
        return putInbounds(tab, entry, true);
    }

    protected <T> T iterateEntries(SHEntry[] tab, Function<SHEntry<K, V>, Returnable<T>> consumer) {
        for (SHEntry<K, V> entry : tab) {
            if (entry == null) {
                continue;
            }
            if (!entry.occupied) {
                continue;
            }
            Returnable<T> apply = consumer.apply(entry);
            if (apply.exit) {
                return apply.val;
            }

        }
        return null;

    }

    protected boolean equate(Object k1, Object k2) {
        return equator.equate((K) k1, (K) k2);
    }

    protected int hash(Object key) {
        return equator.hash((K) key);
    }

    @Override
    public boolean containsKey(Object key) {
        return walkTable(table, hash(key), index -> {
            SHEntry slot = table[index];
            if (slot != null && slot.occupied && equate(slot.key, key)) {
                return Returnable.end(true);
            }
            return Returnable.cont(false);
        });
    }

    @Override
    public boolean containsValue(Object value) {

        return null != iterateEntries(table, entry -> {
            if (equate(value, entry.value)) {
                Returnable.end(true);
            }
            return Returnable.cont(false);
        });
    }

    @Override
    public V get(Object key) {

        return walkTable(table, hash(key), index -> {
            SHEntry<K, V> slot = table[index];
            if (slot == null) {
                return Returnable.end(); // no such value
            } else if (!slot.occupied) {
                return Returnable.cont(); // could be later
            } else if (slot.occupied && equate(slot.key, key)) {
                return Returnable.end(slot.value);
            }
            return Returnable.cont();
        });
    }

    @Override
    public V put(K key, V value) {
        tableResize(count + 1);
        SHEntry<K, V> put = put(table, key, value);
        if (put == null) {
            return null;
        }
        return put.value;

    }

    @Override
    public V remove(Object key) {
        SHEntry[] tab = table;
        int hash = hash(key);
        SHEntry<K, V> old = walkTable(tab, hash, index -> {
            SHEntry slot = tab[index];
            if (slot == null) {
                return Returnable.end(); // exit;
            } else if (!slot.occupied) {//maybe try again
                return Returnable.cont();
            } else if (slot.occupied && equate(slot.key, key)) { // found occupied
                slot.occupied = false;
                count -= 1;
                // count unchanged
                return Returnable.end(slot);
            }
            return Returnable.cont();
        });
        if (old == null) {
            return null;
        } else {
            tableResize(count);
        }
        return old.value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        table = new SHEntry[DEFAULT_SIZE];
        counter = Integer.MIN_VALUE;
        count = 0;
    }

    protected List<SHEntry<K, V>> entries() {

        List<SHEntry<K, V>> entries = new ArrayList<>(count);

        iterateEntries(table, entry -> {
            entries.add(entry);
            return Returnable.cont();
        });
        if (ordered) {
            Collections.sort(entries);
        }
        return entries;
    }

    @Override
    public Set<K> keySet() {
        LinkedHashSet<K> toCollection = MakeStream.from(entries()).map(m -> m.key).toCollection(LinkedHashSet::new);
        return Collections.unmodifiableSet(toCollection);
    }

    @Override
    public Collection<V> values() {
        return MakeStream.from(entries()).map(m -> m.value).toUnmodifiableList();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        LinkedHashSet<SHEntry<K, V>> collection = MakeStream.from(entries()).toCollection(LinkedHashSet::new);
        Set<Entry<K, V>> unmodifiableSet = Collections.unmodifiableSet(collection);
        return unmodifiableSet;
    }

    @Override
    public String toString() {
        return "SimpleHashtable{" + "loadFactor=" + loadFactor + ", count=" + count + ", counter=" + counter + '}';
    }

}
