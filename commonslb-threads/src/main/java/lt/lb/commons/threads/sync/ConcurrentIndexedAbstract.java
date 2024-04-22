package lt.lb.commons.threads.sync;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.threads.sync.ConcurrentIndexedAbstract.Cell;

/**
 *
 * Lock-free write-intense concurrent unordered data structure, for short life
 * cycle objects. The user must manually skip null objects while iterating;
 *
 * Cell-wide read locking is used compared to {@link ConcurrentIndexedBag}
 *
 * @author laim0nas100
 */
public abstract class ConcurrentIndexedAbstract<T, C extends Cell<T>> implements Iterable<T> {

    public static abstract class Cell<T> {

        public volatile T value;

        public abstract boolean isOccupied();
    }

    private final C[] array;
    private final AtomicInteger insertIndex = new AtomicInteger(0);
    private final int size;

    public ConcurrentIndexedAbstract(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Minimum size is 1, now is:" + size);
        }
        this.size = size;

        array = newCellArray(size);
    }

    protected abstract C[] newCellArray(int size);

    /**
     * Same as {@link ConcurrentIndexedAbstract#insert(java.lang.Object) }
     *
     * @param value
     * @return
     */
    public int add(T value) {
        return insert(value);
    }

    /**
     * Insert value, Loops over cells using global index (shared across threads)
     * to find empty, and puts the value there. Returns cell index, for the
     * value to be removed.
     *
     * Negative index means value was not inserted.
     *
     * @param value
     * @return
     */
    public int insert(T value) {
        Objects.requireNonNull(value, "null elements are not allowed");
        int count = 0;
        while (true) {

            int i = insertIndex.getAndUpdate(x -> (x + 1) % size);
            C cell = array[i];
            if (insertLogic(cell, value)) {
                return i;
            }
            count++;
            if (count > size * 2) {
                return -1;
            }

        }
    }

    protected abstract boolean insertLogic(C cell, T value);

    public static enum CAS {
        CONT, FAILED, OK;
    }

    /**
     * Remove the value at given index. [0 ; size).
     *
     * @param idx
     * @return
     */
    public boolean remove(int idx) {
        C cell = array[idx];
        while (true) {
            CAS op = removeIndexLogic(cell);
            if (op == CAS.OK) {
                return true;
            }
            if (op == CAS.FAILED) {
                return false;
            }
        }
    }

    protected abstract CAS removeIndexLogic(C cell);

    /**
     * If you lost the index, then you can try to remove the value by scanning
     * every cell, results may vary.
     *
     * @param value
     * @return
     */
    public boolean remove(T value) {
        Objects.requireNonNull(value);
        int count = 0;
        while (count < 5) {
            for (int i = 0; i < size; i++) {
                C cell = array[i];
                CAS op = removeValueLogic(cell, value);
                if (op == CAS.OK) {
                    return true;
                }
                if (op == CAS.FAILED) {
                    return false;
                }

            }
            count++;
        }

        return false;
    }

    protected abstract CAS removeValueLogic(C cell, T value);

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int index = -1;

            @Override
            public boolean hasNext() {
                return index + 1 < size;
            }

            @Override
            public T next() {
                index++;
                Cell<T> cell = array[index];
                if (cell.isOccupied()) {
                    return cell.value;
                }
                return null;
            }
        };
    }

}
