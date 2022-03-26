package lt.lb.commons.reflect.unified;

import java.lang.reflect.InvocationTargetException;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface IStaticMethod<S, T> extends IMethod<S, T> {

    public default T invoke(Object... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return (T) method().invoke(null, args);
    }

    public default SafeOpt<T> safeInvoke(Object... args) {
        return SafeOpt.ofGet(() -> invoke(args));
    }
}
