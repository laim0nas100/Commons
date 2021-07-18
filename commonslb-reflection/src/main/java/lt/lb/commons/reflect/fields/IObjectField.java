package lt.lb.commons.reflect.fields;

import java.util.Objects;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface IObjectField<S,T> extends IField<S, T>{
    public default T get(S source) throws IllegalArgumentException, IllegalAccessException {
        Objects.requireNonNull(source, "Source field access object is null");
        return (T) rawField().get(source);
    }

    public default SafeOpt<T> safeGet(S source) {
        Objects.requireNonNull(source, "Source field access object is null");
        return SafeOpt.ofGet(() -> get(source));
    }
}
