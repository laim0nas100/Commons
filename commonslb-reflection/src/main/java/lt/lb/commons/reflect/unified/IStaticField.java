package lt.lb.commons.reflect.unified;

import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface IStaticField<S, T> extends IField<S, T> {

    public default T get() throws IllegalArgumentException, IllegalAccessException {
        return (T) field().get(null);
    }

    public default SafeOpt<T> safeGet() {
        return SafeOpt.ofGet(() -> get());
    }

    public default void set(T value) throws IllegalArgumentException, IllegalAccessException {
        field().set(null, value);
    }

    public default SafeOpt<T> safeSet(T value) {
        return SafeOpt.ofGet(() -> {
            set(value);
            return null;
        });
    }

}
