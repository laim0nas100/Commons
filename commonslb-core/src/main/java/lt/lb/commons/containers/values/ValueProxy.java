package lt.lb.commons.containers.values;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public interface ValueProxy<T> extends Supplier<T>, Consumer<T> {

    /**
     * Main get method
     *
     * @return
     */
    @Override
    public T get();

    /**
     * Main set method
     *
     * @param v
     */
    public void set(T v);

    public default T getValue() {
        return this.get();
    }

    public default void setValue(T v) {
        this.set(v);
    }

    @Override
    public default void accept(T v) {
        this.set(v);
    }

    /**
     * {@code true} if value is null
     *
     * @return
     */
    public default boolean isNotNull() {
        return this.get() != null;
    }

    /**
     * {@code true} if value is not null
     *
     * @return
     */
    public default boolean isEmpty() {
        return this.get() == null;
    }

    /**
     *
     * @param func new value
     * @return updated value
     */
    public default T setAndGet(Supplier<T> func) {
        set(func.get());
        return get();
    }

    /**
     * @param func new value
     * @return old value
     */
    public default T getAndSet(Supplier<T> func) {
        T got = this.get();
        set(func.get());
        return got;
    }

    public default Optional<T> toOptional() {
        return Optional.ofNullable(get());
    }
}
