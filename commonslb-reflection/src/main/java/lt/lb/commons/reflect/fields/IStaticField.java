package lt.lb.commons.reflect.fields;

import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface IStaticField<S, T> extends IField<S, T> {

    public default T get() throws IllegalArgumentException, IllegalAccessException {
        return (T) rawField().get(null);
    }

    public default SafeOpt<T> safeGet() {
        return SafeOpt.ofGet(() -> get());
    }

}
