package lt.lb.commons.threads.sync;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 */
public class ThreadLocalParkSpace<T> implements Iterable<T> {

    private ThreadLocal<Integer> reservedIndex = ThreadLocal.withInitial(() -> -1);

    private final AtomicInteger slidingIndex;

    private final Object[] items;
    private final int size;

    public ThreadLocalParkSpace(int maxThreads) {
        size = maxThreads;
        items = new Object[size];
        slidingIndex = new AtomicInteger(size);
    }

    public int park(T item) {
        Object toPark = item == null ? Nulls.EMPTY_OBJECT : item;
        int index = reservedIndex.get();
        while (index < 0) { // new thread

            index = slidingIndex.accumulateAndGet(0, (current, discard) -> {
                return (current + 1) % size;
            });
            if (items[index] == null) {
                break;
            }else{
                index = -1;
            }

        }
        reservedIndex.set(index);
        if (items[index] == null) {
            items[index] = toPark;
        } else {
            reservedIndex.set(-1);
            return -1;
        }

        return index;
    }

    public boolean park(int index, T item) {
        Object toPark = item == null ? Nulls.EMPTY_OBJECT : item;
        if (items[index] == null) {
            items[index] = toPark;
            return true;
        }
        return false;

    }

    public boolean unpark(int index) {
        if (index < 0) {
            if ((index = reservedIndex.get()) < 0) {
                return false;
            }
        }
        if (items[index] == null) {
            return false;
        }
        items[index] = null;
        return true;
    }

    public List<T> getPlacedItems() {
        ArrayList<T> arrayList = new ArrayList<>();
        for (Object item : items) {
            if (item != null) {
                arrayList.add(item == Nulls.EMPTY_OBJECT ? null : (T) item);
            }
        }
        return arrayList;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int i = -1;

            @Override
            public boolean hasNext() {
                return i + 1 < size;
            }

            @Override
            public T next() {
                Object item = items[++i];
                return item == Nulls.EMPTY_OBJECT ? null : (T) item;
            }
        };
    }

}
