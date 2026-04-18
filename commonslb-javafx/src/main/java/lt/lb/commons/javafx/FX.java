package lt.lb.commons.javafx;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import lt.lb.commons.threads.Futures;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.concurrent.SafeOptAsync;
import lt.lb.uncheckedutils.concurrent.Submitter;
import lt.lb.uncheckedutils.func.UncheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Platform executor central submit point and init
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

    public final static Executor platformExecutor = (Runnable r) -> {
        if (isFXthread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    };

    public static Optional<Throwable> runAndWait(UncheckedRunnable run) {
        return runAndWait((Runnable) run);
    }

    public static Optional<Throwable> runAndWait(Runnable run) {
        if (isFXthread()) {
            return Checked.checkedRun(run);
        }
        return Futures.runAndAwait(platformExecutor, run).getError().asOptional();
    }

    public static CompletableFuture<Void> submit(Runnable run) {
        return CompletableFuture.runAsync(run, platformExecutor);
    }

    public static CompletableFuture<Void> submit(UncheckedRunnable run) {
        return CompletableFuture.runAsync(run, platformExecutor);
    }

    public static CompletableFuture<Void> submit(UncheckedRunnable run, Consumer<Throwable> handler) {
        return CompletableFuture.runAsync(() -> Checked.uncheckedRunWithHandler(handler, run), platformExecutor);
    }

    public static <T> CompletableFuture<T> submit(Callable<T> call, Consumer<Throwable> handler) {
        return Futures.submitAsync(call, handler, platformExecutor);
    }

    private static Logger logger = LoggerFactory.getLogger(FX.class);

    public static void withAlert(UncheckedRunnable run) {
        Checked.checkedRun(run).ifPresent(ex -> {
            logger.error("Error in withAlert", ex);
            FX.runAndWait(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            });
        });

    }

    public static final Submitter FX_SUBMITTER = new Submitter() {
        @Override
        public boolean continueInPlace(SafeOptAsync.AsyncWork task) {
            return FX.isFXthread();
        }

        /**
         * Work only happens in as single FX platform thread.
         */
        @Override
        public void submit(SafeOptAsync.AsyncWork task) {
            FX.platformExecutor.execute(task);
        }
    };

    private static final SafeOpt dummy = SafeOpt.of(0);

    public static SafeOpt asyncFxStarter() {
        return new SafeOptAsync(FX_SUBMITTER, dummy);
    }

    public static <T> SafeOpt<T> asyncFxStarter(T val) {
        return SafeOpt.ofAsync(FX_SUBMITTER, val);
    }

}
