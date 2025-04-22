package lt.lb.commons.threads;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;
import lt.lb.commons.containers.values.Value;
import lt.lb.uncheckedutils.NestedException;
import lt.lb.uncheckedutils.func.UncheckedFunction;

/**
 *
 * @author laim0nas100
 */
public class XTask<T> extends FutureTask<T> {

    protected Value<XTask> me;

    public XTask(UncheckedFunction<? super XTask, ? extends T> func) {
        this(func, new Value<>());
    }

    protected XTask(UncheckedFunction<? super XTask, ? extends T> func, Value<XTask> me) {
        super(fromFunc(func, me));
        this.me = me;
        me.set(this);
        
    }

    public static <T> Callable<T> fromFunc(UncheckedFunction<? super XTask, ? extends T> func, Supplier<XTask> me) {
        Objects.requireNonNull(func);
        Objects.requireNonNull(me);
        return new XCall<>(func, me);
    }

    private static class XCall<T> implements Callable<T> {

        final UncheckedFunction<? super XTask, ? extends T> func;
        final Supplier<XTask> ref;

        public XCall(UncheckedFunction<? super XTask, ? extends T> func, Supplier<XTask> ref) {
            this.func = func;
            this.ref = ref;
        }

        @Override
        public T call() throws Exception {
            try {
                return func.applyUnchecked(ref.get());
            } catch (Throwable th) {
                if (th instanceof Error) {
                    throw (Error) th;
                } else {
                    throw NestedException.of(th);
                }
            }
        }
    }

}
