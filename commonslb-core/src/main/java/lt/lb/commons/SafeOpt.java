package lt.lb.commons;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.func.unchecked.UnsafeFunction;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 *
 * Optional equivalent, but with exception ignoring mapping
 */
public class SafeOpt<T> implements Supplier<T> {

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final T val;

    /**
     * If non-null, the exception; if null, indicates no exception is present
     */
    private final Throwable threw;

    private static final SafeOpt<?> READY = new SafeOpt<>(new Object(), null);

    private static final SafeOpt<?> EMPTY = new SafeOpt<>();

    private SafeOpt() {
        this(null, null);
    }

    private SafeOpt(T value, Throwable throwable) {
        val = value;
        threw = throwable;
    }

    /**
     * Returns an {@code SafeOpt} with the specified present non-null value.
     *
     * @param <T> the class of the value
     * @param val the value to be present, which must be non-null
     * @return an {@code SafeOpt} with the value present
     * @throws NullPointerException if value is null
     */
    public static <T> SafeOpt<T> of(T val) {
        Objects.requireNonNull(val);
        return new SafeOpt(val, null);
    }

    /**
     * Returns an {@code SafeOpt} with the specified present non-null value.
     *
     * @param <T> the class of the value
     * @param sup the value supplier to be present
     * @return an {@code SafeOpt} with the value present, or an empty
     * {@code SafeOpt} if supplier or it's value is null. If exception occurred
     * anywhere, then it will be captured and empty {@code SafeOpt} with such
     * exception will be returned
     */
    public static <T> SafeOpt<T> ofGet(Supplier<T> sup) {
        return READY.map(m -> sup.get());
    }

    /**
     * Returns an empty {@code SafeOpt} instance.
     *
     * @param <T> Type of the non-existent value
     * @return an empty {@code SafeOpt}
     */
    public static <T> SafeOpt<T> empty() {
        return (SafeOpt<T>) EMPTY;
    }

    /**
     * Returns an empty {@code SafeOpt} instance with given error.
     *
     * @param <T> Type of the non-existent value
     * @return an empty {@code SafeOpt} with an error.
     */
    private static <T> SafeOpt<T> empty(Throwable error) {
        return new SafeOpt<>(null, error);
    }

    /**
     * Returns an {@code SafeOpt} describing the specified value, if non-null,
     * otherwise returns an empty {@code Optional}.
     *
     * @param <T> the class of the value
     * @param val the possibly-null value to describe
     * @return an {@code SafeOpt} with a present value if the specified value is
     * non-null, otherwise an empty {@code Optional}
     */
    public static <T> SafeOpt<T> ofNullable(T val) {
        if (val == null) {
            return SafeOpt.empty();
        } else {
            return SafeOpt.of(val);
        }

    }

    /**
     * Simple mapping from {@code Optional} to {@code SafeOpt}
     *
     * @param <T>
     * @param opt
     * @return
     */
    public static <T> SafeOpt<T> ofOptional(Optional<T> opt) {
        return opt.map(m -> SafeOpt.of(m)).orElse(SafeOpt.empty());
    }

    /**
     * Returns an {@code Optional} of current {@code SafeOpt} dropping
     * exception.
     *
     * @return an {@code Optional} with a present value if the specified value
     * is non-null, otherwise an empty {@code Optional}
     */
    public Optional<T> asOptional() {
        return Optional.ofNullable(val);
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return val != null;
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present
     * @throws NullPointerException if value is present and {@code consumer} is
     * null
     */
    public void ifPresent(Consumer<? super T> consumer) {
        if (val != null) {
            consumer.accept(val);
        }
    }

    /**
     * If a value is present, and the value matches the given predicate, return
     * an {@code SafeOpt} describing the value, otherwise return an empty
     * {@code SafeOpt}. If any exception occurs inside predicate, just returns
     * empty {@code SafeOpt} with captured exception.
     *
     * @param predicate a predicate to apply to the value, if present
     * @return an {@code SafeOpt} describing the value of this {@code SafeOpt}
     * if a value is present and the value matches the given predicate,
     * otherwise an empty {@code SafeOpt}
     * @throws NullPointerException if the predicate is null
     */
    public SafeOpt<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "Null predicate");
        if (!isPresent()) {
            return SafeOpt.empty(this.threw);
        } else {
            try {
                return predicate.test(val) ? this : empty();
            } catch (Throwable t) {
                return SafeOpt.empty(t);
            }
        }
    }

    /**
     * If a value is present, apply the provided mapping function to it, and if
     * the result is non-null, return an {@code SafeOpt} describing the result.
     * Otherwise return an empty {@code SafeOpt}. If any exception occurs, just
     * returns empty {@code SafeOpt} with captured exception.
     *
     * @param <U> The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present
     * @return an {@code SafeOpt} describing the result of applying a mapping
     * function to the value of this {@code SafeOpt}, if a value is present,
     * otherwise an empty {@code SafeOpt}
     * @throws NullPointerException if the mapping function is null
     */
    public <U> SafeOpt<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "Null map function");
        if (!isPresent()) {
            return SafeOpt.empty(this.threw);
        } else {
            try {
                return SafeOpt.ofNullable(mapper.apply(val));
            } catch (Throwable t) {
                return SafeOpt.empty(t);
            }
        }
    }

    /**
     * If a value is present, apply the provided mapping function to it, and if
     * the result is non-null, return an {@code SafeOpt} describing the result.
     * Otherwise return an empty {@code SafeOpt}. If any exception occurs, just
     * returns empty {@code SafeOpt} with captured exception.
     *
     * @param <U> The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present
     * @return an {@code SafeOpt} describing the result of applying a mapping
     * function to the value of this {@code SafeOpt}, if a value is present,
     * otherwise an empty {@code SafeOpt}
     * @throws NullPointerException if the mapping function is null
     */
    public <U> SafeOpt<U> map(UnsafeFunction<? super T, ? extends U> mapper) {
        return map((Function<T, U>) mapper);
    }

    /**
     * Aggregation of {@code filter(clazz::isInstance).map(t -> (U) t);}
     *
     * @param <U> The type of the result of the mapping function
     * @param clazz instance to filter value
     * @return an {@code SafeOpt} of given action aggregation
     * @throws NullPointerException if the clazz is null
     */
    public <U> SafeOpt<U> select(Class<U> clazz) {
        Objects.requireNonNull(clazz);
        return filter(clazz::isInstance).map(t -> (U) t);
    }

    /**
     * If a value is present, apply the provided {@code SafeOpt}-bearing mapping
     * function to it, return that result, otherwise return an empty
     * {@code SafeOpt}. This method is similar to {@link #map(Function)}, but
     * the provided mapper is one whose result is already an {@code SafeOpt},
     * and if invoked, {@code flatMap} does not wrap it with an additional
     * {@code SafeOpt}.
     *
     * @param <U> The type parameter to the {@code SafeOpt} returned by
     * @param mapper a mapping function to apply to the value, if present the
     * mapping function
     * @return the result of applying an {@code SafeOpt}-bearing mapping
     * function to the value of this {@code SafeOpt}, if a value is present,
     * otherwise an empty {@code SafeOpt}
     * @throws NullPointerException if the mapping function is null
     */
    public <U> SafeOpt<U> flatMap(Function<? super T, SafeOpt<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return SafeOpt.empty(this.threw);
        } else {
            try {
                SafeOpt<U> apply = mapper.apply(val);
                if (apply != null) {
                    return apply;
                }
            } catch (Throwable t) {
                return SafeOpt.empty(t);
            }
            return SafeOpt.empty();
        }
    }

    /**
     * If a value is present, apply the provided {@code Optional}-bearing
     * mapping function to it, return that result, otherwise return an empty
     * {@code SafeOpt}. This method is similar to {@link #map(Function)}, but
     * the provided mapper is one whose result is already an {@code SafeOpt},
     * and if invoked, {@code flatMap} does not wrap it with an additional
     * {@code SafeOpt}.
     *
     * @param <U> The type parameter to the {@code SafeOpt} returned by
     * @param mapper a mapping function to apply to the value, if present the
     * mapping function
     * @return the result of applying an {@code Optional}-bearing mapping
     * function to the value of this {@code SafeOpt}, if a value is present,
     * otherwise an empty {@code SafeOpt}
     * @throws NullPointerException if the mapping function is null
     */
    public <U> SafeOpt<U> flatMapOpt(Function<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return SafeOpt.empty(this.threw);
        } else {
            try {
                SafeOpt<U> apply = SafeOpt.ofOptional(mapper.apply(val));
                if (apply != null) {
                    return apply;
                }
            } catch (Throwable t) {
                return SafeOpt.empty(t);
            }
            return SafeOpt.empty(this.threw);
        }
    }

    /**
     * If a value is present in this {@code Optional}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null value held by this {@code Optional}
     * @throws NoSuchElementException if there is no value present
     *
     * @see SafeOpt#isPresent()
     */
    public T get() {
        if (isPresent()) {
            return val;
        }
        throw new NoSuchElementException("No value present");
    }

    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present, may
     * be null
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return val != null ? val : other;
    }

    /**
     * Return the value if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code Supplier} whose result is returned if no value is
     * present
     * @return the value if present otherwise the result of {@code other.get()}
     * @throws NullPointerException if value is not present and {@code other} is
     * null
     */
    public T orElseGet(Supplier<? extends T> other) {
        return val != null ? val : other.get();
    }

    /**
     * Return the contained value, if present, otherwise throw an exception to
     * be created by the provided supplier.
     *
     * @apiNote A method reference to the exception constructor with an empty
     * argument list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to
     * be thrown
     * @return the present value
     * @throws X if there is no value present
     * @throws NullPointerException if no value is present and
     * {@code exceptionSupplier} is null
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (val != null) {
            return val;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Throw matching type of exception if present, or return contained value
     * (if no contained value, throws NoSuchElementException)
     *
     *
     * @param <X> Type of the exception to be thrown
     * @param type
     * @return the present value
     * @throws X if there is such exception
     */
    public <X extends Throwable> T throwIfErrorOrGet(Class<X> type) throws X {
        if (threw != null) {
            Throwable t = NestedException.unwrap(threw);
            if (Ins.of(threw).instanceOf(type)) {
                throw (X) t;
            }
        }
        return get();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SafeOpt)) {
            return false;
        }

        SafeOpt<?> other = (SafeOpt<?>) obj;
        return Objects.equals(val, other.val);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.val);
    }

    @Override
    public String toString() {
        return val != null
                ? String.format("SafeOpt[%s]", val)
                : "SafeOpt.empty";
    }

    public Optional<Throwable> getError() {
        return Optional.ofNullable(threw);
    }

}
