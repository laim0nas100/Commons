package lt.lb.commons.threads.sync;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Lock-free write-intense concurrent unordered data structure, for short life
 * cycle objects. The user must manually skip null objects while iterating;
 *
 * No cell-wide locking is used;
 *
 * @author laim0nas100
 */
public class ConcurrentIndexedBag<T> extends ConcurrentIndexedAbstract<T, ConcurrentIndexedBag.Cell<T>> {

    public static class Cell<T> extends ConcurrentIndexedAbstract.Cell<T> {

        public AtomicBoolean occupied = new AtomicBoolean(false);

        @Override
        public boolean isOccupied() {
            return occupied.get();
        }
    }

    public ConcurrentIndexedBag(int size) {
        super(size);
    }

    @Override
    protected Cell<T>[] newCellArray(int size) {
        Cell<T>[] arr = new Cell[size];
        for (int i = 0; i < size; i++) {
            arr[i] = new Cell<>();
        }
        return arr;
    }

    @Override
    protected boolean insertLogic(Cell<T> cell, T value) {
        if (cell.occupied.compareAndSet(false, true)) {//can change
            cell.value = value;
            return true;
        }
        return false;
    }

    @Override
    protected CAS removeIndexLogic(Cell<T> cell) {
        if (cell.occupied.compareAndSet(true, false)) {
            return CAS.OK;
        }
        return CAS.FAILED;
    }

    @Override
    protected CAS removeValueLogic(Cell<T> cell, T value) {
        if (Objects.equals(cell.value, value) && cell.occupied.compareAndSet(true, false)) {
            return CAS.OK;
        }
        return CAS.CONT;
    }

}
