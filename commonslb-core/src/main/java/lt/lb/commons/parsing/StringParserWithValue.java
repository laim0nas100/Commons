package lt.lb.commons.parsing;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedFunction;

/**
 *
 * Generalized way to parse common values from string that already has a
 * supplied value.
 *
 * @author laim0nas100
 */
public interface StringParserWithValue<T> extends StringParser<T>, Supplier<T> {

    default SafeOpt<T> getOpt() {
        return SafeOpt.ofGet(this);
    }

    default SafeOpt<String> getOptString() {
        return getOpt().flatMap(this::parseOptString);
    }

    default String getString() {
        return getOpt().flatMap(this::parseOptString).get();
    }

    default <O> SafeOpt<O> getOptAny(Function<String, O> func) {
        return getOpt().flatMap(o -> parseOptAny(o, func));
    }

    default <O> SafeOpt<O> getOptAny(UncheckedFunction<String, O> func) {
        return getOpt().flatMap(o -> parseOptAny(o, func));
    }

    default <O> O getAny(Function<String, O> func) {
        return getOpt().flatMap(o -> parseOptAny(o, func)).get();
    }

    default <O> O getAny(UncheckedFunction<String, O> func) {
        return getOpt().flatMap(o -> parseOptAny(o, func)).get();
    }

    default boolean getBool() {
        return getOpt().flatMap(this::parseOptBool).get();
    }

    default SafeOpt<Boolean> getOptBool() {
        return getOpt().flatMap(this::parseOptBool);
    }

    default int getInt() {
        return getOpt().flatMap(this::parseOptInt).get();
    }

    default SafeOpt<Integer> getOptInt() {
        return getOpt().flatMap(this::parseOptInt);
    }

    default long getLong() {
        return getOpt().flatMap(this::parseOptLong).get();
    }

    default SafeOpt<Long> getOptLong() {
        return getOpt().flatMap(this::parseOptLong);
    }

    default float getFloat() {
        return getOpt().flatMap(this::parseOptFloat).get();
    }

    default SafeOpt<Float> getOptFloat() {
        return getOpt().flatMap(this::parseOptFloat);
    }

    default double getDouble() {
        return getOpt().flatMap(this::parseOptDouble).get();
    }

    default SafeOpt<Double> getOptDouble() {
        return getOpt().flatMap(this::parseOptDouble);
    }

    default <O> SafeOpt<List<O>> getOptAnyList(Function<String, O> func) {
        return getOpt().flatMap(ob -> parseOptAnyList(ob, func));
    }

    default <O> SafeOpt<List<O>> getOptAnyList(UncheckedFunction<String, O> func) {
        return getOpt().flatMap(ob -> parseOptAnyList(ob, func));
    }

    default <O> List<O> getAnyList(Function<String, O> func) {
        return getOpt().flatMap(ob -> parseOptAnyList(ob, func)).get();
    }

    default <O> List<O> getAnyList(UncheckedFunction<String, O> func) {
        return getOpt().flatMap(ob -> parseOptAnyList(ob, func)).get();
    }

    default List<Boolean> getBoolList() {
        return getOpt().flatMap(this::parseOptBoolList).get();
    }

    default SafeOpt<List<Boolean>> getOptBoolList() {
        return getOpt().flatMap(this::parseOptBoolList);
    }

    default List<Integer> getIntList() {
        return getOpt().flatMap(this::parseOptIntList).get();
    }

    default SafeOpt<List<Integer>> getOptIntList() {
        return getOpt().flatMap(this::parseOptIntList);
    }

    default List<Long> getLongList() {
        return getOpt().flatMap(this::parseOptLongList).get();
    }

    default SafeOpt<List<Long>> getOptLongList() {
        return getOpt().flatMap(this::parseOptLongList);
    }

    default List<Float> getFloatList() {
        return getOpt().flatMap(this::parseOptFloatList).get();
    }

    default SafeOpt<List<Float>> getOptFloatList() {
        return getOpt().flatMap(this::parseOptFloatList);
    }

    default List<Double> getDoubleList() {
        return getOpt().flatMap(this::parseOptDoubleList).get();
    }

    default SafeOpt<List<Double>> getOptDoubleList() {
        return getOpt().flatMap(this::parseOptDoubleList);
    }

    default List<String> getStringList() {
        return getOpt().flatMap(this::parseOptStringList).get();
    }

    default SafeOpt<List<String>> getOptStringList() {
        return getOpt().flatMap(this::parseOptStringList);
    }
}
