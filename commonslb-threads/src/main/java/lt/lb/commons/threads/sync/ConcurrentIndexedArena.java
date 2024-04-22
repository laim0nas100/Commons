package lt.lb.commons.threads.sync;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Lock-free write-intense concurrent unordered data structure, for short life
 * cycle objects. The user must manually skip null objects while iterating;
 *
 * Cell-wide read locking is used.
 *
 * @author laim0nas100
 */
public class ConcurrentIndexedArena<T> extends ConcurrentIndexedAbstract<T, ConcurrentIndexedArena.Cell<T>> {

    public static class Cell<T> extends ConcurrentIndexedAbstract.Cell<T> {

        public AtomicBoolean read = new AtomicBoolean(false);
        public volatile boolean occupied = false;

        @Override
        public boolean isOccupied() {
            return occupied;
        }
    }

    public ConcurrentIndexedArena(int size) {
        super(size);
    }

    @Override
    protected Cell<T>[] newCellArray(int size) {
        Cell<T>[] arr = new Cell[size];
        for(int i = 0; i < size; i++){
            arr[i] = new Cell<>();
        }
        return arr;
    }

    @Override
    protected boolean insertLogic(Cell<T> cell, T value) {
        if (cell.read.compareAndSet(false, true)) {//can change
            boolean ok = false;
            if (!cell.occupied) {
                ok = true;
                cell.occupied = true;
                cell.value = value;
            }
            cell.read.set(false);
            if (ok) {
                return true;
            }

        }
        return false;
    }

    @Override
    protected CAS removeIndexLogic(Cell<T> cell) {
        if (cell.read.compareAndSet(false, true)) {
            boolean rem = false;
            if (cell.occupied) {
                cell.occupied = false;
                rem = true;
            }
            cell.read.set(false);
            return rem ? CAS.OK : CAS.FAILED;
        }
        return CAS.CONT;
    }

    @Override
    protected CAS removeValueLogic(Cell<T> cell, T value) {
        if (cell.read.compareAndSet(false, true)) {
            boolean found = false;
            if (cell.occupied && Objects.equals(cell.value, value)) {
                cell.occupied = false;
                found = true;
            }
            cell.read.set(false);
            return found ? CAS.OK : CAS.CONT;
        }
        return CAS.CONT;
    }

}
