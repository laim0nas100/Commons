package lt.lb.commons.rows;

import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class OrderedRunnable implements Runnable, Comparable<OrderedRunnable> {

    public final Runnable delegated;
    public final int order;

    public OrderedRunnable(int order, Runnable delegated) {
        this.delegated = Objects.requireNonNull(delegated);
        this.order = order;
    }

    public OrderedRunnable(Runnable delegated) {
        this(0, delegated);
    }

    @Override
    public void run() {
        delegated.run();
    }

    @Override
    public int compareTo(OrderedRunnable o) {
        return Integer.compare(order, o.order);
    }

}
