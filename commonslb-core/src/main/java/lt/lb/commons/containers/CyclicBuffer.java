package lt.lb.commons.containers;

/**
 *
 * @author laim0nas100
 */
public class CyclicBuffer<T> {

    public final int length;
    protected Object[] array;
    protected int pos = 0;

    public CyclicBuffer(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Minimum size is 8");
        }
        this.length = length;
        array = new Object[length];
    }

    public int add(T item) {
        int insertedPos = pos;
        array[insertedPos] = item;
        pos = (pos + 1) % length;
        return insertedPos;
    }

    protected T rawGet(int index) {
        return (T) array[index];
    }

    public T get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(index + " length:" + length);
        }
        return rawGet(index);
    }

    protected int relativeIndex(int inc) {
        return Math.floorMod(pos + inc, length);
    }

    public T getLastAdded() {
        return rawGet(relativeIndex(-1));
    }

    /**
     * Get item based on relative positions index (-1 gets last added, -2 get
     * second last added, etc... length-1 gets the same as last added, 0 gets
     * the the oldest element, or the one to be replaced by the nest add)
     *
     * @param index
     * @return
     */
    public T getRelative(int inc) {
        return get(relativeIndex(inc));
    }

    public T poll() {
        pos = Math.floorMod(pos - 1, length);
        return (T) array[pos];
    }

}
