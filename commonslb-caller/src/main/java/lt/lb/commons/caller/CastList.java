package lt.lb.commons.caller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author laim0nas100 Supports 7 explicitly defined arguments and much more
 * implicitly defined by lists. By default other parameters are null;
 * @param <T> base type or arguments
 */
public class CastList<T> implements Serializable {

    private final List<T> args;
    private static final List emptyArgs = new ArrayList(0);

    public CastList(List<T> list) {
        this.args = list == null ? emptyArgs : list;

        int size = args.size();
        parameterCount = size;
        if (size >= 1) {
            _0 = args.get(0);
        } else {
            _0 = null;
        }
        if (size >= 2) {
            _1 = args.get(1);
        } else {
            _1 = null;
        }
        if (size >= 3) {
            _2 = args.get(2);
        } else {
            _2 = null;
        }
        if (size >= 4) {
            _3 = args.get(3);
        } else {
            _3 = null;
        }
        if (size >= 5) {
            _4 = args.get(4);
        } else {
            _4 = null;
        }
        if (size >= 6) {
            _5 = args.get(5);
        } else {
            _5 = null;
        }
        if (size >= 7) {
            _6 = args.get(6);
        } else {
            _6 = null;
        }
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
     * @return 0-based indexed parameter wither performed cast
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

    /**
     * Amount of parameters passed here
     */
    public final int parameterCount;

}
