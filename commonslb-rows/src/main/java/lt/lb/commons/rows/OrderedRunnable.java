package lt.lb.commons.rows;

import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class OrderedRunnable implements Runnable {

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

    public static Comparator<OrderedRunnable> asPriority(){
        return (a,b) -> Integer.compare(b.order, a.order);
    }
    
    public static Comparator<OrderedRunnable> asOrder(){
        return (a,b) -> Integer.compare(a.order, b.order);
    }

}
