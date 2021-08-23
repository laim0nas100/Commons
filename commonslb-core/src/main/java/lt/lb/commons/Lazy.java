package lt.lb.commons;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lt.lb.uncheckedutils.NestedException;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedSupplier;

/**
 * A way to initialize values lazily, without a constructor or whenever.
 *
 * @author laim0nas100
 * @param <T>
 */
public class Lazy<T> implements UncheckedSupplier<T> {

    protected final Supplier<T> supl;
    protected final Executor exe;
    protected final FutureTask<T> future;
    protected final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Construct a {@link Lazy} with a given value.
     *
     * @param val
     */
    public Lazy(T val) {
        this(() -> val, Runnable::run);
    }

    /**
     * Construct a {@link Lazy} with a given value {@link Supplier};
     *
     * @param supl
     */
    public Lazy(Supplier<T> supl) {
        this(supl, Runnable::run);
    }

    /**
     * Construct a {@link Lazy} with a given value {@link Supplier} and
     * {@link Executor}
     *
     * @param supl
     * @param exe
     */
    public Lazy(Supplier<T> supl, Executor exe) {
        this.supl = Objects.requireNonNull(supl);
        this.exe = Objects.requireNonNull(exe);
        future = new FutureTask<>(() -> {
            T v = supl.get();
            initialized.set(true);
            return v;
        });
    }

    public Lazy<T> eager() {
        if (!future.isDone()) {
            exe.execute(future); // will not run more than once
        }
        return this;
    }

    public boolean initialized() {
        return initialized.get();
    }

    /**
     * Initialize with a value
     *
     * @param <T>
     * @param val
     * @return
     */
    public <T> Lazy ofVal(T val) {
        return new Lazy<>(val);
    }

    /**
     * Initialize with a {@link Supplier}. Populates at the time of access.
     *
     * @param <T>
     * @param supl
     * @return
     */
    public <T> Lazy ofSupply(Supplier<T> supl) {
        return new Lazy<>(supl, Runnable::run);
    }

    /**
     * Initialize with a {@link Supplier}. Populates at the time of access, but
     * using default {@link Executor}.
     *
     * @param <T>
     * @param supl
     * @return
     */
    public <T> Lazy ofSupplyAsync(Supplier<T> supl) {
        return new Lazy<>(supl, ForkJoinPool.commonPool());
    }

    /**
     * Initialize with a {@link Supplier}.Populates at the time of access, but
     * using provided {@link Executor}.
     *
     * @param <T>
     * @param supl
     * @param exe
     * @return
     */
    public <T> Lazy ofSupplyAsync(Supplier<T> supl, Executor exe) {
        return new Lazy<>(supl, exe);
    }

    /**
     * Initialize with a {@link Supplier}. Populates at the time of access.
     *
     * @param <T>
     * @param supl
     * @return
     */
    public <T> Lazy ofSupply(UncheckedSupplier<T> supl) {
        return new Lazy<>(supl, Runnable::run);
    }

    /**
     * Initialize with a {@link Supplier}. Populates at the time of access, but
     * using default {@link Executor}.
     *
     * @param <T>
     * @param supl
     * @return
     */
    public <T> Lazy ofSupplyAsync(UncheckedSupplier<T> supl) {
        return new Lazy<>(supl, ForkJoinPool.commonPool());
    }

    /**
     * Initialize with a {@link Supplier}.Populates at the time of access, but
     * using provided {@link Executor}.
     *
     * @param <T>
     * @param supl
     * @param exe
     * @return
     */
    public <T> Lazy ofSupplyAsync(UncheckedSupplier<T> supl, Executor exe) {
        return new Lazy<>(supl, exe);
    }

    /**
     * Populate and return value, or throw {@link NestedException} if any
     * occurred, waiting given amount of time.
     *
     * @param time
     * @param unit
     * @return
     */
    public T get(long time, TimeUnit unit) {
        try {
            eager();
            return future.get(time, unit);
        } catch (TimeoutException | InterruptedException | ExecutionException th) {
            throw NestedException.of(th);
        }
    }

    /**
     * Populate and return value as a {@link SafeOpt}, waiting given amount of
     * time.
     *
     * @param time
     * @param unit
     * @return
     */
    public SafeOpt<T> getSafe(long time, TimeUnit unit) {
        return SafeOpt.ofGet(() -> get(time, unit));
    }

    @Override
    public T getUnchecked() throws Throwable {
        eager();
        return future.get();
    }

}
