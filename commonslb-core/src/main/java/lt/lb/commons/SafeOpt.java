package lt.lb.commons;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 *
 * Optional equivalent, but with exception ignoring mapping
 */
public class SafeOpt<T> {

    @FunctionalInterface
    public interface UnsafeFunction<P, R> extends Function<P, R> {

        public R applyUnsafe(P t) throws Exception;

        @Override
        public default R apply(P t) {
            try {
                return applyUnsafe(t);
            } catch (Throwable e) {
                throw new NestedException(e);
            }
        }
    }

    private final T val;

    private final Throwable threw;

    private static final SafeOpt<?> empty = new SafeOpt<>();

    private SafeOpt() {
        this(null, null);
    }

    private SafeOpt(T value, Throwable throwable) {
        val = value;
        threw = throwable;
    }

    public static <T> SafeOpt<T> of(T val) {
        Objects.requireNonNull(val);
        return new SafeOpt(val, null);
    }

    public static <T> SafeOpt<T> empty() {
        return (SafeOpt<T>) empty;
    }

    private static <T> SafeOpt<T> empty(Throwable error) {
        return new SafeOpt<>(null, error);
    }

    public static <T> SafeOpt<T> ofNullable(T val) {
        if (val == null) {
            return SafeOpt.empty();
        } else {
            return SafeOpt.of(val);
        }

    }

    public static <T> SafeOpt<T> ofOptional(Optional<T> opt) {
        if (opt.isPresent()) {
            return SafeOpt.of(opt.get());
        } else {
            return SafeOpt.empty();
        }
    }

    public Optional<T> asOptional() {
        return Optional.ofNullable(val);
    }

    public boolean isPresent() {
        return val != null;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (val != null) {
            consumer.accept(val);
        }
    }

    public SafeOpt<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "Null predicate");
        if (!isPresent()) {
            return SafeOpt.empty(this.threw);
        } else {
            return predicate.test(val) ? this : empty();
        }
    }

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

    public <U> SafeOpt<U> map(UnsafeFunction<? super T, ? extends U> mapper) {
        return map((Function<T, U>) mapper);
    }

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

    public T get() {
        if (isPresent()) {
            return val;
        }
        throw new NoSuchElementException("No value present");
    }

    public T orElse(T other) {
        return val != null ? val : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return val != null ? val : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (val != null) {
            return val;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Optional)) {
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
