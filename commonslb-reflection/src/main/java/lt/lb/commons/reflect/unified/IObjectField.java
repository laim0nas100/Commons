package lt.lb.commons.reflect.unified;

import java.util.Objects;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface IObjectField<S, T> extends IField<S, T> {

    public default T get(S source) throws IllegalArgumentException, IllegalAccessException {
        Objects.requireNonNull(source, "Source field access object is null");
        return (T) field().get(source);
    }

    public default SafeOpt<T> safeGet(S source) {
        Objects.requireNonNull(source, "Source field access object is null");
        return SafeOpt.ofGet(() -> get(source));
    }

    public default void set(S source, T value) throws IllegalArgumentException, IllegalAccessException {
        Objects.requireNonNull(source, "Source field access object is null");
        field().set(source, value);
    }

    public default SafeOpt<T> safeSet(S source, T value) {
        Objects.requireNonNull(source, "Source field access object is null");
        return SafeOpt.ofGet(() -> {
            set(source, value);
            return null;
        });
    }
}
