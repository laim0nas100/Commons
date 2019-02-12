/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package lt.lb.commons.iteration;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public class StreamROI<T> extends BaseROI<T>{

    protected Spliterator<T> spliterator;
    protected Stream<T> stream;
    protected LinkedList<T> queue = new LinkedList<>();
    protected Consumer<T> cons = (item) -> {
        queue.addLast(item);
    };

    public StreamROI(Stream<T> stream) {
        this.stream = stream;
        this.spliterator = stream.spliterator();
    }

    @Override
    public boolean hasNext() {
        if (queue.isEmpty()) {
            spliterator.tryAdvance(cons);
        }
        return !queue.isEmpty();
    }

    @Override
    public T next() {
        if (queue.isEmpty()) {
            if (!spliterator.tryAdvance(cons)) {
                throw new NoSuchElementException();
            }
        }
        T next = queue.pollFirst();
        index++;
        return setCurrent(next);
    }

    @Override
    public void close(){
        stream.close();
    }

}
