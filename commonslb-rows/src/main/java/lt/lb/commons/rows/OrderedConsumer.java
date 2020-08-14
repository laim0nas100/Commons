package lt.lb.commons.rows;

import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author laim0nas100
 */
public class OrderedConsumer<T> implements Consumer<T>, Comparable<OrderedConsumer> {

    public final Consumer<T> delegated;
    public final int order;

    public OrderedConsumer(int order, Consumer<T> delegated) {
        this.delegated = Objects.requireNonNull(delegated);
        this.order = order;
    }

    public OrderedConsumer(Consumer<T> delegated) {
        this(0, delegated);
    }

    @Override
    public void accept(T t) {
        delegated.accept(t);
    }

    @Override
    public int compareTo(OrderedConsumer o) {
        return Integer.compare(order, o.order);
    }

}
