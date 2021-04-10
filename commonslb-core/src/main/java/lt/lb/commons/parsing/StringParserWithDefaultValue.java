package lt.lb.commons.parsing;

import java.util.List;

/**
 *
 * @author laim0nas100
 */
public interface StringParserWithDefaultValue<T, D> extends StringParserWithValue<T> {

    /**
     * Caller is responsible to throw Exceptions if default value is
     * miss-configured.
     *
     * @return
     */
    D getDefault();

    default String getOrDefaultString() {
        return this.getOptString().orElse((String) getDefault());
    }

    default boolean getOrDefaultBool() {
        return this.getOptBool().orElse((Boolean) getDefault());
    }

    default Integer getOrDefaultInt() {
        return getOptInt().orElse((Integer) getDefault());
    }

    default Long getOrDefaultLong() {
        return getOptLong().orElse((Long) getDefault());
    }

    default Float getOrDefaultFloat() {
        return getOptFloat().orElse((Float) getDefault());
    }

    default Double getOrDefaultDouble() {
        return getOptDouble().orElse((Double) getDefault());
    }

    default List<Integer> getOrDefaultIntList() {
        return getOptIntList().orElse((List<Integer>) getDefault());
    }

    default List<Long> getOrDefaultLongList() {
        return getOptLongList().orElse((List<Long>) getDefault());
    }

    default List<Float> getOrDefaultFloatList() {
        return getOptFloatList().orElse((List<Float>) getDefault());
    }

    default List<Double> getOrDefaultDoubleList() {
        return getOptDoubleList().orElse((List<Double>) getDefault());
    }

    default List<String> getOrDefaultStringList() {
        return getOptStringList().orElse((List<String>) getDefault());
    }

}
