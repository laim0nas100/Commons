package lt.lb.commons.jpa.lazy;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public abstract class BufferedIteratorList<T> implements BufferedIterator<T> {

    protected Optional<List<T>> currentList = Optional.empty();
    protected int currentIndex = 0;

    /**
     * This method recieves current list and makes a new list while reading from
     * internal buffer. This method should also save newly produced list. Empty
     * list is not the same as not list, hence the optional.
     *
     * @param currentList
     * @return
     */
    protected abstract Optional<List<T>> produceNextList(Optional<List<T>> currentList) throws Exception;

    protected Optional<List<T>> getCurrentList() {
        return currentList;
    }

    protected void setCurrentList(Optional<List<T>> list) {
        this.currentList = list;
    }

    @Override
    public boolean readToBuffer() throws Exception {
        Optional<List<T>> produceNextList = produceNextList(getCurrentList());
        setCurrentList(produceNextList);
        currentIndex = 0;
        return produceNextList.isPresent();
    }

    @Override
    public boolean hasCurrentBufferedItem() {
        return getCurrentList().filter(f -> f.size() > currentIndex).isPresent();
    }

    @Override
    public T getCurrentBufferedItem() throws Exception {
        return getCurrentList().map(list -> list.get(currentIndex)).get();
    }

    @Override
    public boolean hasNextBufferedItem() {
        return getCurrentList().filter(f -> f.size() > 1 + currentIndex).isPresent();
    }

    @Override
    public T getNextBufferedItem() throws Exception {
        if (hasNextBufferedItem()) {
            this.currentIndex++;
            return getCurrentBufferedItem();
        }
        throw new IllegalStateException("Need to read from input, no more buffered items left");
    }

}
