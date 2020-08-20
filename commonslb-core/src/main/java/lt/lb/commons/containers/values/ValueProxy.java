package lt.lb.commons.containers.values;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface ValueProxy<T> extends Supplier<T>, Consumer<T> {

    public static <T> ValueProxy<T> quickProxy(Supplier<? extends T> supl, Consumer<? super T> cons) {
        return new ValueProxy<T>() {
            @Override
            public T get() {
                return supl.get();
            }

            @Override
            public void set(T v) {
                cons.accept(v);
            }
        };
    }

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
     * {@code true} if value is not null
     *
     * @return
     */
    public default boolean isNotNull() {
        return this.get() != null;
    }

    /**
     * {@code true} if value is null
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

    /**
     * Construct {@code Optional} with this value
     *
     * @return constructed Optional
     */
    public default Optional<T> toOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * Construct {@code SafeOpt} with this value
     *
     * @return constructed SafeOpt
     */
    public default SafeOpt<T> toSafeOpt() {
        return SafeOpt.ofNullable(get());
    }

    /**
     * Construct {@code SafeOpt} with this value
     *
     * @return constructed SafeOpt
     */
    public default Stream<T> toStream() {
        return Stream.of(get());
    }
}
