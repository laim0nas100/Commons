package lt.lb.commons.func.unchecked;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface UnsafeSupplier<T> extends Supplier<T>, Callable<T> {

    @Override
    public default T call() throws Exception {
        try {
            return this.unsafeGet();
        } catch (Throwable e) {
            Throwable real = NestedException.unwrap(e);
            if(real instanceof Exception){
                throw (Exception)e;
            }
            throw NestedException.of(e);
        }
    }

    @Override
    public default T get() {

        try {
            return this.unsafeGet();
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }
    
    

    public T unsafeGet() throws Throwable;
}
