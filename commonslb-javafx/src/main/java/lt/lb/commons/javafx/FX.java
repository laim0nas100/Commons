package lt.lb.commons.javafx;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javafx.application.Platform;
import lt.lb.commons.containers.Value;
import lt.lb.commons.F;
import lt.lb.commons.threads.UnsafeRunnable;

/**
 *
 *
 * Platform substitute
 *
 * @author laim0nas100
 */
public class FX {

    public static boolean isFXthread() {
        return Platform.isFxApplicationThread();
    }

    private final static Executor platformExecutor = (Runnable r) -> {
        if (isFXthread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    };

    public static CompletableFuture<Void> submit(Runnable run) {
        return CompletableFuture.runAsync(run, platformExecutor);
    }

    public static CompletableFuture<Void> submit(UnsafeRunnable run) {
        return CompletableFuture.runAsync(run, platformExecutor);
    }

    public static CompletableFuture<Void> submit(UnsafeRunnable run, Consumer<Throwable> handler) {
        return CompletableFuture.runAsync(() -> F.unsafeRunWithHandler(handler, run), platformExecutor);
    }

    public static <T> CompletableFuture<T> submit(Callable<T> call, Consumer<Throwable> handler) {
        return submitAsync(call, handler, platformExecutor);
    }

    private static <T> CompletableFuture<T> submitAsync(Callable<T> call, Consumer<Throwable> handler, Executor exe) {
        return CompletableFuture.supplyAsync(() -> {
            Value<T> val = new Value<>();
            F.unsafeRunWithHandler(
                    handler,
                    () -> {
                        val.set(call.call());
                    }
            );
            return val.get();
        }, exe);
    }

    public static CompletableFuture<Void> submitAsync(Runnable run, Executor exe) {
        return CompletableFuture.runAsync(run, exe);
    }

    public static CompletableFuture<Void> submitAsync(UnsafeRunnable run, Executor exe) {
        return CompletableFuture.runAsync(run, exe);
    }

    public static void join(Future... futures) {
        join(Arrays.asList(futures));
    }

    public static void join(Collection<Future> futures) {
        F.iterate(futures, (i, f) -> {
            F.unsafeRun(() -> {
                f.get();
            });
        });
    }

}
