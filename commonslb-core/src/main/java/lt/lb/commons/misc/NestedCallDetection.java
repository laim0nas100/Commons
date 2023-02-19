package lt.lb.commons.misc;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.values.ThreadLocalValue;
import lt.lb.uncheckedutils.func.UncheckedRunnable;
import lt.lb.uncheckedutils.func.UncheckedSupplier;

/**
 *
 * @author laim0nas100
 */
public interface NestedCallDetection {

    public static NestedCallDetection threadLocal() {
        return new NestedCallDetectionThreadLocal();
    }

    public static NestedCallDetection atomicGlobal() {
        return new NestedCallDetectionAtomicGlobal();
    }

    public static class NestedCallDetectionAtomicGlobal implements NestedCallDetection {

        protected final AtomicBoolean inside = new AtomicBoolean(false);

        @Override
        public <T> T fullCall(Supplier<T> onFail, Supplier<T> call) {
            Nulls.requireNonNulls(onFail, call);
            if (inside.compareAndSet(false, true)) {
                try {
                    return call.get();
                } finally {
                    inside.set(false);
                }

            } else {
                return onFail.get();
            }
        }
    }

    public static class NestedCallDetectionThreadLocal implements NestedCallDetection {

        protected final ThreadLocalValue<Boolean> inside = new ThreadLocalValue(false);

        @Override
        public <T> T fullCall(Supplier<T> onFail, Supplier<T> call) {
            Nulls.requireNonNulls(onFail, call);
            if (inside.get()) {
                return onFail.get();
            }
            try {
                inside.set(true);
                return call.get();
            } finally {
                inside.set(false);
            }
        }
    }

    public <T> T fullCall(Supplier<T> onFail, Supplier<T> call);

    public default <T> T fullCall(UncheckedSupplier<T> onFail, UncheckedSupplier<T> call) {
        return fullCall((Supplier<T>) onFail, (Supplier<T>) call);
    }

    public default <T> T call(T onFailure, Supplier<T> call) {
        return fullCall(() -> onFailure, call);
    }

    public default <T> T call(T onFailure, UncheckedSupplier<T> call) {
        return call(onFailure, (Supplier<T>) call);
    }

    public default void run(Runnable run) {
        Objects.requireNonNull(run, "Runnable must not be null");
        call(null, () -> {
            run.run();
            return null;
        });
    }

    public default void run(UncheckedRunnable run) {
        run((Runnable) run);
    }

    public default <T> T call(UncheckedSupplier<T> call) {
        return call(null, (Supplier<T>) call);
    }

    public default <T> T call(Supplier<T> call) {
        return call(null, call);
    }

}
