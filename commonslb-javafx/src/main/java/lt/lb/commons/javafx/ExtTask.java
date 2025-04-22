package lt.lb.commons.javafx;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public abstract class ExtTask<T> implements RunnableFuture {

    public final ReadOnlyBooleanProperty canceled = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty paused = new SimpleBooleanProperty(false);
    public final DoubleProperty progress = new SimpleDoubleProperty(0D);
    public final ReadOnlyBooleanProperty done = new SimpleBooleanProperty(false);
    public final ReadOnlyBooleanProperty failed = new SimpleBooleanProperty(false);
    public final ReadOnlyBooleanProperty interrupted = new SimpleBooleanProperty(false);
    public final ReadOnlyBooleanProperty running = new SimpleBooleanProperty(false);

    protected AtomicBoolean runningAtomic = new AtomicBoolean(false);

    public HashMap<String, Object> valueMap = new HashMap<>();
    public ExtTask childTask;
    protected LinkedBlockingDeque resultDeque = new LinkedBlockingDeque();
    protected Object result;
    protected Thread currentThread;
    protected InvokeChildTask onInterrupted, onDone, onFailed, onCanceled, onSucceded;
    protected int timesToRun = 1;
    protected int timesRan = 0;
    protected Throwable error;

    public static interface InvokeChildTask {

        public void handle(Runnable r) throws Exception;
    }

    public ExtTask(int timesToRun) {
        this.timesToRun = timesToRun;
    }

    public ExtTask() {
        this(1);
    }

    private void setProperty(ReadOnlyBooleanProperty prop, boolean value) {
        SimpleBooleanProperty p = (SimpleBooleanProperty) prop;
        p.set(value);

    }

    public void reset() {
        if (runningAtomic.get()) {
            return;
        }
        setProperty(done, false);
        setProperty(canceled, false);
        setProperty(interrupted, false);
        setProperty(failed, false);
        setProperty(paused, false);
        result = null;
        resultDeque.clear();
    }

    @Override
    public final void run() {
        if (runningAtomic.get() || (timesRan >= timesToRun && timesToRun > 0)) {
            return;
        }
        if (!canceled.get()) {
            if (!runningAtomic.compareAndSet(false, true)) {
                return;
            }
            timesRan++;
            currentThread = Thread.currentThread();
            setProperty(running, true);
            try {
                resultDeque.add(Nulls.requireNonNullElse(call(), Nulls.EMPTY_OBJECT));

            } catch (InterruptedException ex) {
                setProperty(interrupted, true);
                tryRun(onInterrupted);
            } catch (Throwable ex) {
                error = ex;
                setProperty(failed, true);
                tryRun(onFailed);
            }
        }
        if (canceled.get() || interrupted.get()) {
            tryRun(onCanceled);
        } else if (!failed.get()) {
            tryRun(onSucceded);
        }
        setProperty(done, true);
        tryRun(onDone);
        setProperty(running, false);
        runningAtomic.set(false);
    }

    private void tryRun(InvokeChildTask r) {
        if (r != null) {
            try {
                r.handle(childTask);
            } catch (Throwable e) {
            }
        }
    }

    @Override
    public boolean cancel(boolean interrupt) {
        setProperty(canceled, true);
        if (interrupt) {
            if (currentThread != null) {
                currentThread.interrupt();
            }
        }
        return false;
    }

    public boolean cancel() {
        return cancel(true);
    }

    @Override
    public boolean isCancelled() {
        return canceled.get();
    }

    @Override
    public boolean isDone() {
        return done.get();
    }

    @Override
    public T get() throws InterruptedException {
        if (done.get()) {
            return Nulls.castOrNullIfEmptyObject(result);
        } else {
            result = resultDeque.takeLast();
            return Nulls.castOrNullIfEmptyObject(result);
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException {
        if (done.get()) {
            return Nulls.castOrNullIfEmptyObject(result);
        } else {
            result = resultDeque.pollLast(timeout, unit);
            return Nulls.castOrNullIfEmptyObject(result);
        }
    }

    protected abstract T call() throws Exception;

    public final void setOnFailed(InvokeChildTask handle) {
        onFailed = handle;
    }

    public final void setOnSucceeded(InvokeChildTask handle) {
        onSucceded = handle;
    }

    public final void setOnCancelled(InvokeChildTask handle) {
        onCanceled = handle;
    }

    public final void setOnInterrupted(InvokeChildTask handle) {
        onInterrupted = handle;
    }

    public final void setOnDone(InvokeChildTask handle) {
        onDone = handle;
    }

    public final void appendOnDone(InvokeChildTask handle) {
        if (onDone == null) {
            setOnDone(handle);
        } else {
            InvokeChildTask old = onDone;
            onDone = s -> {
                old.handle(childTask);
                handle.handle(childTask);
            };
        }
    }

    public Thread toThread() {
        return new Thread(this);
    }

    public static ExtTask<Void> create(Runnable run) {
        Objects.requireNonNull(run, "runnable is null");
        return new ExtTask<Void>() {
            @Override
            protected Void call() throws Exception {
                run.run();
                return null;
            }
        };
    }

    public static <T> ExtTask<T> create(Callable<T> call) {
        Objects.requireNonNull(call, "callable is null");
        return new ExtTask<T>() {
            @Override
            protected T call() throws Exception {
                return call.call();
            }
        };
    }

    public void addObject(String key, Object object) {
        this.valueMap.put(key, object);
    }

    public Throwable getException() {
        return this.error;
    }

}
