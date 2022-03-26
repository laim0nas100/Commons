package lt.lb.commons.reflect.unified;

import java.lang.reflect.InvocationTargetException;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface IObjectMethod<S, T> extends IMethod<S, T> {

    public default T invoke(S source, Object... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return (T) method().invoke(source, args);
    }

    public default SafeOpt<T> safeInvoke(S source, Object... args) {
        return SafeOpt.ofGet(() -> invoke(source, args));
    }
}
