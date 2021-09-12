package lt.lb.commons.jpa.lazy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface BufferedIterator<T> {

    /**
     * Read data. new data is expected to replace currently buffered one.
     *
     * @return if any data was read.
     * @throws Exception
     */
    public boolean readToBuffer() throws Exception;

    /**
     *
     * @return if internal buffer is not empty and current buffer index is not
     * out of reach. If this is false, probably need to call
     * {@link readToBuffer()}
     */
    public boolean hasCurrentBufferedItem();

    /**
     * The first item in the buffer instantly after {@link readToBuffer}, or
     * currently buffered item.
     *
     * @return
     * @throws Exception
     */
    public T getCurrentBufferedItem() throws Exception;

    /**
     *
     * @return if buffer is not empty and is bigger than the current buffered
     * item index.
     */
    public boolean hasNextBufferedItem();

    /**
     * This is usually the default method to call if buffer contains more than
     * one items. If buffer is not empty and is bigger than the current buffered
     * increment buffer index and return the next item in the buffer according
     * to the index.
     *
     * @return
     * @throws Exception
     */
    public T getNextBufferedItem() throws Exception;

    /**
     * Iterate through every item.
     *
     * @param consumer
     * @throws Exception
     */
    public default void produceItems(Consumer<T> consumer) throws Exception {
        while (true) {
            if (!hasNextBufferedItem()) {
                if (readToBuffer()) {
                    if (hasCurrentBufferedItem()) {
                        consumer.accept(getCurrentBufferedItem());
                    }
                } else {
                    break;
                }
            } else {
                consumer.accept(getNextBufferedItem());
            }
        }

    }

    /**
     * Iterate through every item and place them in the list.
     *
     * @return
     * @throws Exception
     */
    public default List<T> produceItems() throws Exception {
        ArrayList<T> list = new ArrayList<>();
        produceItems(list::add);
        return list;
    }

    /**
     *
     * @return The conventional style (same as {@link Iterator}) with relevant
     * methods for clarity.
     */
    public default SimplifiedBufferedIterator<T> toSimplifiedIterator() {
        return new SimplifiedBufferedIterator<>(this);
    }

    public static class SimplifiedBufferedIterator<T> implements Iterable<T> {

        protected BufferedIterator<T> iter;
        protected ArrayList<T> unsubmitted = new ArrayList<>(1);

        public SimplifiedBufferedIterator(BufferedIterator<T> iter) {
            this.iter = Objects.requireNonNull(iter);
        }

        /**
         * Same as {@link Iterator#hasNext() }
         *
         * @return
         * @throws Exception
         */
        public boolean hasNext() throws Exception {
            return !unsubmitted.isEmpty() || iter.hasNextBufferedItem() || appendToUnsubmitted();
        }

        protected boolean appendToUnsubmitted() throws Exception {
            while (true) {
                if (iter.readToBuffer()) {
                    if (iter.hasCurrentBufferedItem()) {
                        unsubmitted.add(iter.getCurrentBufferedItem());
                        return true;
                    }//can be empty buffer, so try again until non-empty buffer or no buffer at all is found
                } else {
                    return false;
                }
            }
        }

        /**
         * Same as {@link Iterator#next() }
         *
         * @return
         * @throws Exception
         */
        public T next() throws Exception {
            if (!unsubmitted.isEmpty()) {
                return unsubmitted.remove(0);
            }
            if (iter.hasNextBufferedItem()) {
                return iter.getNextBufferedItem();
            }
            if (!appendToUnsubmitted()) {
                throw new NoSuchElementException("No more items in this buffer");
            }
            return unsubmitted.remove(0);
        }

        /**
         *
         * @return {@link Iterator} with exceptions masked as
         * {@link IllegalStateException}.
         */
        @Override
        public Iterator<T> iterator() {
            return new SimplifiedIterator<>(this);
        }
    }

    public static class SimplifiedIterator<T> implements Iterator<T> {

        protected final SimplifiedBufferedIterator<T> sbi;

        public SimplifiedIterator(SimplifiedBufferedIterator<T> sbi) {
            this.sbi = Objects.requireNonNull(sbi, "SimplifiedBufferedIterator must not be null");
        }

        @Override
        public boolean hasNext() {
            try {
                return sbi.hasNext();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public T next() {
            try {
                return sbi.next();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
