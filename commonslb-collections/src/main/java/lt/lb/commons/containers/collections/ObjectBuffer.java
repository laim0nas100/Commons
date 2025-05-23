package lt.lb.commons.containers.collections;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class ObjectBuffer<T> {

    private ArrayList<T> buffer = new ArrayList<>();
    private Collection<T> flushHere;
    private int flushingSize = 1;

    public void add(T object) {
        this.buffer.add(object);
        attemptFlush();
    }

    private void attemptFlush() {
        if (buffer.size() >= flushingSize) {
            flush();
        }
    }

    public void addAll(Collection<T> col) {
        this.buffer.addAll(col);
        attemptFlush();

    }

    public ObjectBuffer(Collection<T> flushHere, int flushingSize) {
        this.flushHere = flushHere;
        this.flushingSize = Math.max(1, flushingSize);
    }

    public void flush() {
        flushHere.addAll(buffer);
        buffer.clear();
    }
}
