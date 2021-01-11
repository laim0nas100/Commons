package lt.lb.commons.javafx;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 *
 * Platform substitute
 *
 * @author laim0nas100
 */
public class FX {
    
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    
    public static boolean initFxRuntime() {
        if (initialized.compareAndSet(false, true)) {
            new JFXPanel();
            return true;
        } else {
            return false;
        }
        
    }
    
    public static boolean isFxInitialized() {
        return initialized.get();
    }
    
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
        futures.forEach(f -> {
            F.unsafeRun(() -> {
                f.get();
            });
        });
    }
    
    public static Logger logger = LogManager.getLogger(FX.class);
    
    public static void withAlert(UnsafeRunnable run) {
        F.checkedRun(run).ifPresent(ex -> {
            logger.error("Error in withAlert", ex);
            FX.submit(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            });
        });
        
    }
    
}
