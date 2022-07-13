package lt.lb.commons.containers.values;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 * @param <T>
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
    public default T setAndGetSupl(Supplier<? extends T> func) {
        Objects.requireNonNull(func, "Supplier is null");
        set(func.get());
        return get();
    }

    /**
     *
     * @param newVal new value
     * @return updated value
     */
    public default T setAndGet(T newVal) {
        set(newVal);
        return get();
    }

    /**
     * @param func new value
     * @return old value
     */
    public default T getAndSetSupl(Supplier<? extends T> func) {
        Objects.requireNonNull(func, "Supplier is null");
        T got = this.get();
        set(func.get());
        return got;
    }

    /**
     * @param newVal new value
     * @return old value
     */
    public default T getAndSet(T newVal) {
        T got = this.get();
        set(newVal);
        return got;
    }

    /**
     * Calls set with newVal only if it's different from the old one
     *
     * @param newVal
     * @return old value
     */
    public default T getAndChangeIfNew(T newVal) {
        T got = this.get();
        compareNotAndSet(newVal);
        return got;
    }

    /**
     * Calls set with newVal only if it's the expected value from the old one
     *
     * @param expecting
     * @param newVal
     * @return if the value was equal to expected
     */
    public default boolean compareAndSet(T expecting, T newVal) {
        T got = this.get();
        if (Objects.equals(got, expecting)) {
            set(newVal);
            return true;
        }
        return false;
    }

    /**
     * Calls set with newVal only if it is different value from the old one
     *
     * @param newVal
     * @return if the value was not equal to the old one
     */
    public default boolean compareNotAndSet(T newVal) {
        T got = this.get();
        if (!Objects.equals(got, newVal)) {
            set(newVal);
            return true;
        }
        return false;
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
