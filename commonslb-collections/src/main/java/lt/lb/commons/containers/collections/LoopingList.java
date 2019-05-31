package lt.lb.commons.containers.collections;

import java.util.ArrayList;

/**
 *
 * @author laim0nas100
 */
public class LoopingList<T> extends ArrayList<T> {

    public int index = -1;

    public <T> LoopingList() {
        super();
    }

    public T next() {
        if (!this.isEmpty()) {
            index++;
            index %= size();
            return this.get(index);
        } else {
            return null;
        }
    }

    public T prev() {
        if (!this.isEmpty()) {
            index--;
            if (index < 0) {
                index = size() - 1;
            }
            return this.get(index);
        } else {
            return null;
        }
    }
}
