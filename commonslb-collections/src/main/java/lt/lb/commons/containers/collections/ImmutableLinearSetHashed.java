package lt.lb.commons.containers.collections;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 *
 * Linear lookup immutable set with hashing, so 2x speedup over linear lookup,
 * for up to 16 elements speed is the same as {@link LinkedHashSet}. Smaller
 * memory footprint.
 *
 * @author laim0nas100
 */
public class ImmutableLinearSetHashed<T> extends AbstractCollection<T> implements Set<T>, Serializable, Cloneable {

    protected final Object[] data;
    protected int[] hashes;

    protected final int hashMult;

    public ImmutableLinearSetHashed(Object... data) {
        this(true, true, data);
    }

    public ImmutableLinearSetHashed(boolean check, boolean findBestHash, Object[] data) {
        Objects.requireNonNull(data);
        if (check) {
            assertDistinct(data);
        }
        this.data = data;
        this.hashes = new int[data.length];
        int bestHashed = computeHashes(data, hashes, 1);
        int bestMulti = 1;
        for (int i = 2; findBestHash && i < data.length; i++) {//dont bother with fiding best hash or
            if (bestHashed * 2 < data.length) {// less than 50% collision rate, settle
                break;
            }
            int[] potentialHash = new int[data.length];
            int computeHashes = computeHashes(data, potentialHash, i);
            if (computeHashes < bestHashed) {
                bestHashed = computeHashes;
                hashes = potentialHash;
                bestMulti = i;
            }
        }
        hashMult = bestMulti;

    }

    private static int hash(Object ob, int hm) {
        return Objects.hashCode(ob) * hm;
    }

    public static int computeHashes(Object[] data, int[] h, int hm) {
        Arrays.fill(h, -1);
        int maxShift = 0;
        for (int i = 0; i < data.length; i++) {

            Object ob = data[i];
            int index = Math.floorMod(hash(ob, hm), data.length);
            int shift = index;
            int shifting = 0;
            while (h[shift] != -1) {
                shifting++;
                shift = (shift + 1) % data.length;
            }
            h[shift] = i;
            if (shifting > maxShift) {
                maxShift = shifting;
            }
        }
        return maxShift;
    }

    public static void assertDistinct(Object[] data) {
        Set<Object> set = new HashSet<>(data.length);
        for (int i = 0; i < data.length; i++) {
            if (!set.add(data[i])) {
                throw new IllegalArgumentException("duplicate elements not allowed at index:" + i + " element:" + data);
            }
        }
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public boolean isEmpty() {
        return data.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        int first = hashes[Math.floorMod(hash(o, hashMult), data.length)];
        for (int i = 0; i < data.length; i++) {
            int h = (first + i) % data.length;
            if (Objects.equals(o, data[h])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size();
            }

            @Override
            public T next() {
                if (cursor >= size()) {
                    throw new NoSuchElementException(cursor + " size:" + size());
                }
                return (T) data[cursor++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("ImmutableLinearSet");
            }

        };
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException("ImmutableLinearSet");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("ImmutableLinearSet");
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("ImmutableLinearSet");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("ImmutableLinearSet");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("ImmutableLinearSet");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ImmutableLinearSet");
    }

    @Override
    public Object clone() {
        try {
            return super.clone();//all primitive fields or arrays
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
    }
}
