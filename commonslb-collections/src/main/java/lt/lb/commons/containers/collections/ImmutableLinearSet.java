package lt.lb.commons.containers.collections;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 *
 * Linear lookup immutable set, for up to 10 elements speed is the same as {@link LinkedHashSet}.
 * Smaller memory footprint.
 *
 * @author laim0nas100
 */
public class ImmutableLinearSet<T> extends AbstractCollection<T> implements Set<T> {

    Object[] data;

    public ImmutableLinearSet(Object... data) {
        this(true, data);
    }

    public ImmutableLinearSet(boolean check, Object[] data) {
        Objects.requireNonNull(data);
        if (check) {
            assertDistinct(data);
        }
        this.data = data;
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
        for (int i = 0; i < data.length; i++) {
            if(Objects.equals(o, data[i])){
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

}
