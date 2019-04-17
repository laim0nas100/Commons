package lt.lb.commons.interfaces;

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

    public default Optional<T> toOptional() {
        return Optional.ofNullable(get());
    }
}
