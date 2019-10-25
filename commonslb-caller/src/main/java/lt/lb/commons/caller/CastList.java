package lt.lb.commons.caller;

import java.util.Iterator;
import java.util.List;
import lt.lb.commons.EmptyImmutableList;

/**
 *
 * Supports 7 explicitly defined arguments and much more implicitly defined by
 * lists. By default other parameters are null;
 *
 * @param <T> base type or arguments
 * @author laim0nas100
 */
public class CastList<T> implements Iterable<T> {

    private final List<T> args;
    /**
     * Amount of parameters passed here
     */
    public final int parameterCount;

    private T assign(int index) {
        return parameterCount > index ? args.get(index) : null;
    }

    public CastList(List<T> list) {
        this.args = list == null ? EmptyImmutableList.getInstance() : list;
        parameterCount = args.size();
        _0 = assign(0);
        _1 = assign(1);
        _2 = assign(2);
        _3 = assign(3);
        _4 = assign(4);
        _5 = assign(5);
        _6 = assign(6);
    }

    /**
     * @param i index
     * @return 0-based indexed parameter
     */
    public T get(int i) {
        return args.get(i);
    }

    /**
     *
     * @param <R> Type of cast
     * @param i index
     * @return 0-based indexed parameter with performed cast
     */
    public <R> R cget(int i) {
        return (R) get(i);
    }

    /**
     * 1-st parameter
     */
    public final T _0;
    /**
     * 2-nd parameter
     */
    public final T _1;
    /**
     * 3-rd parameter
     */
    public final T _2;
    /**
     * 4-th parameter
     */
    public final T _3;
    /**
     * 5-th parameter
     */
    public final T _4;
    /**
     * 6-th parameter
     */
    public final T _5;
    /**
     * 7-th parameter
     */
    public final T _6;

    @Override
    public Iterator<T> iterator() {
        Iterator<T> iterator = args.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };

    }

}
