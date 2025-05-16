package lt.lb.commons.threads.sync;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.misc.numbers.Atomic;

/**
 *
 * @author laim0nas100
 */
public class ConcurrentConsume<T> {

    protected AtomicInteger consume = new AtomicInteger(0);
    protected final ArrayList<T> array;
    protected boolean readOnly = false;

    public ConcurrentConsume(int size) {
        this.array = new ArrayList<>(size);
    }

    public boolean add(T item) {
        if (isReadOnly()) {
            return false;
        }
        array.add(item);
        return true;
    }

    public T consume() {
//        int i = consume.getAndIncrement();
        int i = Atomic.getAndIncrement(consume, 1);
        if (i >= array.size()) {
            return null;
        }
        return array.get(i);
    }

    public void readOnly() {
        readOnly = true;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public int unfinished() {
        return array.size() - consume.get();
    }

}
