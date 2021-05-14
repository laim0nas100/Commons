package lt.lb.commons.containers.values;

import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class ThreadLocalValue<T> extends ThreadLocal<T> implements ValueProxy<T> {

    protected final Supplier<T> initialValSupl;

    public ThreadLocalValue(Supplier<T> supl) {
        initialValSupl = Objects.requireNonNull(supl);
    }

    public ThreadLocalValue() {
        this(() -> null);
    }

    public ThreadLocalValue(T val) {
        this(() -> val);
    }

    @Override
    protected T initialValue() {
        return initialValSupl.get();
    }

}
