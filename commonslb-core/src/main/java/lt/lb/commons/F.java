package lt.lb.commons;

import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.func.unchecked.UnsafeSupplier;

/**
 *
 * @author laim0nas100
 */
public class F {

    /**
     * De facto empty object to be used instead of null, so that null becomes
     * available as a value.
     */
    public static final Object EMPTY_OBJECT = new Object() {
        @Override
        public String toString() {
            return "Empty object";
        }
    };

    /**
     * Convenience wrapped null check instead of ? operator avoid duplication of
     * object when using ? operator. If java 9 is available, use
     * Object.requireNotNullElse
     *
     * @param <T>
     * @param object
     * @param nullCase
     * @return
     */
    public static <T> T nullWrap(T object, T nullCase) {
        return object == null ? Objects.requireNonNull(nullCase, "nullCase is null") : object;
    }

    /**
     * Convenience wrapped null check instead of ? operator avoid duplication of
     * object when using ? operator. If java 9 is available, use
     * Object.requireNotNullElseGet
     *
     * @param <T>
     * @param object
     * @param nullCaseSup
     * @return
     */
    public static <T> T nullSupp(T object, Supplier<T> nullCaseSup) {
        if (object == null) {
            return Objects.requireNonNull(Objects.requireNonNull(nullCaseSup, "supplier").get(), "supplier.get()");
        }
        return object;
    }

    /**
     * Convenience wrapped if check instead of ? operator avoid duplication of
     * trueCase when using ? operator
     *
     * @param <T>
     * @param trueCase
     * @param falseCase
     * @param pred
     * @return
     */
    public static <T> T ifWrap(T trueCase, T falseCase, Predicate<T> pred) {
        return pred.test(trueCase) ? trueCase : falseCase;
    }

    /**
     *
     * Apply function on closeable and then close it. Ignore exceptions both
     * times. Return null if error occurred during mapper function execution.
     *
     * @param <T>
     * @param <U>
     * @param closeable
     * @param mapper
     * @return
     */
    public static <T extends Closeable, U> U safeClose(T closeable, Function<? super T, ? extends U> mapper) {
        U val = F.checkedCallNoExceptions(() -> mapper.apply(closeable));
        F.checkedRun(() -> {
            closeable.close();
        });
        return val;

    }

    /**
     * Run with wrapping exception
     *
     * @param r
     * @throws NestedException
     */
    public static void unsafeRun(UnsafeRunnable r) throws NestedException {
        try {
            r.unsafeRun();
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }

    /**
     * Call with wrapping exception
     *
     * @param <T>
     * @param call
     * @return
     * @throws NestedException
     */
    public static <T> T unsafeCall(UnsafeSupplier<T> call) throws NestedException {
        try {
            return call.unsafeGet();
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }

    /**
     * Run with wrapping exception inside handler
     *
     * @param cons
     * @param run
     */
    public static void unsafeRunWithHandler(Consumer<Throwable> cons, UnsafeRunnable run) {
        try {
            run.unsafeRun();
        } catch (Throwable e) {
            cons.accept(NestedException.unwrap(e));
        }
    }

    /**
     * Call with wrapping exception inside handler
     *
     * @param <T>
     * @param cons
     * @param call
     * @return result or {@code null} if exception was thrown
     */
    public static <T> T unsafeCallWithHandler(Consumer<Throwable> cons, UnsafeSupplier<T> call) {
        try {
            return call.unsafeGet();
        } catch (Throwable e) {
            cons.accept(NestedException.unwrap(e));
        }
        return null;
    }

    /**
     * Call with ignoring all exceptions. Returns null, if execution fails.
     *
     * @param <T>
     * @param call
     * @return result or {@code null} if exception was thrown
     */
    public static <T> T checkedCallNoExceptions(UnsafeSupplier<T> call) {
        try {
            return call.unsafeGet();
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * Run and catch any possible error
     *
     * @param r
     * @return
     */
    public static Optional<Throwable> checkedRun(UnsafeRunnable r) {
        try {
            r.unsafeRun();
            return Optional.empty();
        } catch (Throwable t) {
            return Optional.of(t).map(m -> NestedException.unwrap(m));
        }
    }

    /**
     * Run and catch any possible error
     *
     * @param r
     * @return
     */
    public static Optional<Throwable> checkedRun(Runnable r) {
        try {
            r.run();
            return Optional.empty();
        } catch (Throwable t) {
            return Optional.of(t).map(m -> NestedException.unwrap(m));
        }
    }

    /**
     * Call and catch any possible error alongside with optional error. Null
     * values are treated as not present.
     *
     * @param call
     * @return
     */
    public static <T> SafeOpt<T> checkedCall(UnsafeSupplier<T> call) {
        return SafeOpt.ofGet(call);
    }

    /**
     * Static cast function. Cast operation is quite significant, so this makes
     * is searchable.
     *
     * @param <T>
     * @param <E>
     * @param ob
     * @return
     * @throws ClassCastException
     */
    public static <T extends E, E> T cast(E ob) throws ClassCastException {
        return (T) ob;
    }

    public static double lerp(double start, double end, double percent) {
        return start + percent * (end - start);
    }

}
