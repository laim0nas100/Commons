package lt.lb.commons.caller;

import java.util.List;

/**
 *
 * @author laim0nas100
 */
public class CastList<T> {

    private List<T> args;

    CastList(List<T> list) {
        int i = 0;
        this.args = list;
        if (list == null) {
            //all null
            _0 = null;
            _1 = null;
            _2 = null;
            _3 = null;
            _4 = null;
            _5 = null;
            _6 = null;
            return;
        } else {
            //all null
            int size = list.size();
            if (size >= 1) {
                _0 = list.get(0);
            } else {
                _0 = null;
            }
            if (size >= 2) {
                _1 = list.get(1);
            } else {
                _1 = null;
            }
            if (size >= 3) {
                _2 = list.get(2);
            } else {
                _2 = null;
            }
            if (size >= 4) {
                _3 = list.get(3);
            } else {
                _3 = null;
            }
            if (size >= 5) {
                _4 = list.get(4);
            } else {
                _4 = null;
            }
            if (size >= 6) {
                _5 = list.get(5);
            } else {
                _5 = null;
            }
            if (size >= 7) {
                _6 = list.get(6);
            } else {
                _6 = null;
            }
        }
    }

    
    public T get(int i) {
        return args.get(i);
    }

    public <R> R cget(int i) {
        return (R) get(i);
    }
    public final T _0;
    public final T _1;
    public final T _2;
    public final T _3;
    public final T _4;
    public final T _5;
    public final T _6;

}
