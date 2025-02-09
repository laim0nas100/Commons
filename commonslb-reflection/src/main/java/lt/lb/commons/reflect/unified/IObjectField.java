package lt.lb.commons.reflect.unified;

import java.util.Objects;
import lt.lb.commons.reflect.Refl;
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

    public default SafeOpt<T> safeAccessableGet(S source) {
        Objects.requireNonNull(source, "Source field access object is null");
        return SafeOpt.ofGet(() -> (T) Refl.fieldAccessableGet(field(), source));
    }

    public default SafeOpt<T> safeGet(S source) {
        Objects.requireNonNull(source, "Source field access object is null");
        return SafeOpt.ofGet(() -> get(source));
    }

    public default void set(S source, T value) throws IllegalArgumentException, IllegalAccessException {
        Objects.requireNonNull(source, "Source field access object is null");
        field().set(source, value);
    }

    public default SafeOpt<Void> safeAccessableSet(S source, T value) {
        Objects.requireNonNull(source, "Source field access object is null");
        return SafeOpt.ofGet(() -> {
            Refl.fieldAccessableSet(field(), source, value);
            return null;
        });
    }

    public default SafeOpt<Void> safeSet(S source, T value) {
        Objects.requireNonNull(source, "Source field access object is null");
        return SafeOpt.ofGet(() -> {
            set(source, value);
            return null;
        });
    }
}
