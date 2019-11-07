package lt.lb.commons;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.func.unchecked.UnsafeBiFunction;
import lt.lb.commons.func.unchecked.UnsafeFunction;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 *
 * {@code Optional} equivalent and in-place replacement, but with exception
 * capturing mapping.
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
     * Return {@code true} if there is no value present, otherwise
     * {@code false}.
     *
     * @return {@code true} if there is no value present, otherwise
     * {@code false}
     */
    public boolean isEmpty() {
        return val == null;
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present
     * @return this object
     * @throws NullPointerException if value is present and {@code consumer} is
     * null
     */
    public SafeOpt<T> ifPresent(Consumer<? super T> consumer) {
        if (val != null) {
            consumer.accept(val);
        }
        return this;
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the given empty-based action.
     *
     * @param action the action to be performed, if a value is present
     * @param emptyAction the empty-based action to be performed, if no value is
     * present
     * @return this object
     * @throws NullPointerException if a value is present and the given action
     * is {@code null}, or no value is present and the given empty-based action
     * is {@code null}.
     */
    public SafeOpt<T> ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (val != null) {
            action.accept(val);
        } else {
            emptyAction.run();
        }
        return this;
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the same action with the given default value
     *
     * @param def default value
     * @param action the action to be performed
     * @return this object
     * @throws NullPointerException if a value is present and the given action
     * is {@code null}. It is up to the caller to ensure that a passed default value is not null.
     */
    public SafeOpt<T> ifPresentOrDefault(T def, Consumer<? super T> action) {
        action.accept(val != null ? val : def);
        return this;
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
                return SafeOpt.empty(NestedException.unwrap(t));
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
                return SafeOpt.empty(NestedException.unwrap(t));
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
    public <U> SafeOpt<U> select(Class<? extends U> clazz) {
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
        Objects.requireNonNull(mapper,"Mapping function was null");
        if (!isPresent()) {
            return SafeOpt.empty(this.threw);
        } else {
            try {
                SafeOpt<U> apply = mapper.apply(val);
                if (apply != null) {
                    return apply;
                }
            } catch (Throwable t) {
                return SafeOpt.empty(NestedException.unwrap(t));
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
        Objects.requireNonNull(mapper,"Mapping function was null");
        if (!isPresent()) {
            return SafeOpt.empty(this.threw);
        } else {
            try {
                return SafeOpt.ofOptional(mapper.apply(val));
            } catch (Throwable t) {
                return SafeOpt.empty(t);
            }
        }
    }

    /**
     * If a value is present, returns an {@code SafeOpt} describing the value,
     * otherwise returns an {@code SafeOpt} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code Optional}
     * @return returns an {@code SafeOpt} describing the value of this
     * {@code SafeOpt}, if a value is present, otherwise an wrapped
     * {@code SafeOpt} produced by the supplying function.
     * @throws NullPointerException if the supplying function is {@code null}
     */
    public SafeOpt<T> or(Supplier<? extends Optional<T>> supplier) {
        Objects.requireNonNull(supplier,"Supplier was null");
        if (isPresent()) {
            return this;
        } else {
            return SafeOpt.ofOptional(supplier.get());
        }
    }

    /**
     * If a value is present, returns an {@code SafeOpt} describing the value,
     * otherwise returns an {@code SafeOpt} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code SafeOpt}
     * @return returns an {@code SafeOpt} describing the value of this
     * {@code SafeOpt}, if a value is present, otherwise an wrapped
     * {@code SafeOpt} produced by the supplying function.
     * @throws NullPointerException if the supplying function is {@code null}
     */
    public SafeOpt<T> orSafe(Supplier<? extends SafeOpt<T>> supplier) {
        Objects.requireNonNull(supplier,"Supplier was null");
        if (isPresent()) {
            return this;
        } else {
            return SafeOpt.READY.flatMap(m -> supplier.get());
        }
    }

    /**
     * If both values are present (in this {@code SafeOpt} and provided
     * {@code SafeOpt with} object), then proceed with combining those values in
     * a safe manner, capturing any exceptions. With and mapper must be
     * explicitly not null.
     *
     * @param <U>
     * @param <P>
     * @param with
     * @param mapper
     * @return
     */
    public <U, P> SafeOpt<U> mapCombine(SafeOpt<? extends P> with, UnsafeBiFunction<? super T, ? super P, ? extends U> mapper) {
        return mapCombine(with, (BiFunction<T, P, U>) mapper);
    }

    /**
     * If both values are present (in this {@code SafeOpt} and provided
     * {@code SafeOpt with} object, then proceed with combining those values in
     * a safe manner, capturing any exceptions. With and mapper must be
     * explicitly not null.
     *
     * @param <U>
     * @param <P>
     * @param with
     * @param mapper
     * @return
     */
    public <U, P> SafeOpt<U> mapCombine(SafeOpt<? extends P> with, BiFunction<? super T, ? super P, ? extends U> mapper) {
        Objects.requireNonNull(with, "Null with object");
        Objects.requireNonNull(mapper, "Null map function");

        if (!isPresent()) {
            return SafeOpt.empty(this.threw);
        } else if (!with.isPresent()) {
            return SafeOpt.empty(with.threw);
        } else {
            try {
                return SafeOpt.ofNullable(mapper.apply(this.val, with.val));
            } catch (Throwable t) {
                return SafeOpt.empty(NestedException.unwrap(t));
            }
        }
    }

    /**
     * If a value is present, returns a sequential {@link Stream} containing
     * only that value, otherwise returns an empty {@code Stream}.
     *
     * @return the optional value as a {@code Stream}
     */
    public Stream<T> stream() {
        if (!isPresent()) {
            return Stream.empty();
        } else {
            return Stream.of(val);
        }
    }

    /**
     * Select first {@code SafeOpt} which is present, otherwise return empty;
     *
     * @param <U>
     * @param options
     * @return
     */
    public static <U> SafeOpt<U> selectFirstPresent(SafeOpt<U>... options) {
        Objects.requireNonNull(options, "Null options");
        for (SafeOpt<U> opt : options) {
            if (opt.isPresent()) {
                return opt;
            }
        }
        return SafeOpt.empty();
    }

    /**
     * If a value is present in this {@code SafeOpt}, returns the value,
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
     * @param <Ex> Type of the exception to be thrown
     * @param type
     * @return the present value
     * @throws Ex if there is such exception
     */
    public <Ex extends Throwable> T throwIfErrorOrGet(Class<Ex> type) throws Ex {
        if (threw != null) {
            Throwable t = NestedException.unwrap(threw);
            if (Ins.ofNullable(threw).instanceOf(type)) {
                throw (Ex) t;
            }
        }
        return get();
    }
    
    /**
     * If error have occurred, terminate by throwing such error wrapped in NestedException
     *
     * @return returns an {@code SafeOpt} describing the value of this
     * {@code SafeOpt}, if a value is present
     * @throws NestedException if any error was present
     */
    public SafeOpt<T> throwIfErrorNested() {
        if(threw != null){
           throw NestedException.of(threw);
        }
        return this;
    }
    
    /**
     * Throw matching type of exception if present
     *
     *
     * @param <Ex> Type of the exception to be thrown
     * @param type
     * @return this object
     * @throws Ex if there is such exception
     */
    public <Ex extends Throwable> SafeOpt<T> throwIfError(Class<Ex> type) throws Ex {
        if (threw != null) {
            Throwable t = NestedException.unwrap(threw);
            if (Ins.ofNullable(threw).instanceOf(type)) {
                throw (Ex) t;
            }
        }
        return this;
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
