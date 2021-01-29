package lt.lb.commons.parsing;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.func.unchecked.UnsafeFunction;

/**
 *
 * Generalized way to parse common values from string.
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface StringParser<T> {

    public static <V> StringParser<V> of(Function<V, String> func) {
        Objects.requireNonNull(func);
        return val -> SafeOpt.ofNullable(val).map(func);
    }

    public static <V> StringParser<V> of(UnsafeFunction<V, String> func) {
        return of((Function<V, String>) func);
    }

    SafeOpt<String> getStringOpt(T p);

    default String getString(T p) {
        return getStringOpt(p).get();
    }

    default <O> SafeOpt<O> getOptAny(T p, Function<String, O> func) {
        return getStringOpt(p).map(func);
    }

    default <O> SafeOpt<O> getOptAny(T p, UnsafeFunction<String, O> func) {
        return getOptAny(p, (Function<String, O>) func);
    }

    default <O> O getAny(T p, Function<String, O> func) {
        return getOptAny(p, func).get();
    }

    default <O> O getAny(T p, UnsafeFunction<String, O> func) {
        return getAny(p, (Function<String, O>) func);
    }

    default boolean getBool(T p) {
        return getBoolOpt(p).get();
    }

    default SafeOpt<Boolean> getBoolOpt(T p) {
        return getOptAny(p, Boolean::parseBoolean);
    }

    default int getInt(T p) {
        return getOptInt(p).get();
    }

    default SafeOpt<Integer> getOptInt(T p) {
        return getOptAny(p, Integer::parseInt);
    }

    default long getLong(T p) {
        return getOptLong(p).get();
    }

    default SafeOpt<Long> getOptLong(T p) {
        return getOptAny(p, Long::parseLong);
    }

    default float getFloat(T p) {
        return getOptFloat(p).get();
    }

    default SafeOpt<Float> getOptFloat(T p) {
        return getOptAny(p, Float::parseFloat);
    }

    default double getDouble(T p) {
        return getOptDouble(p).get();
    }

    default SafeOpt<Double> getOptDouble(T p) {
        return getOptAny(p, Double::parseDouble);
    }

    default String[] parseArray(String string) {
        string = StringOp.remove(string, "[");
        string = StringOp.remove(string, "]");
        return StringOp.split(string, ", ");
    }

    default <O> SafeOpt<List<O>> getOptAnyList(T p, Function<String, O> func) {
        return getStringOpt(p)
                .map(s -> parseArray(s))
                .map(Stream::of)
                .map(stream -> stream.map(func))
                .map(stream -> stream.collect(Collectors.toList()));
    }

    default <O> SafeOpt<List<O>> getOptAnyList(T p, UnsafeFunction<String, O> func) {
        return getOptAnyList(p, (Function<String, O>) func);
    }

    default <O> List<O> getAnyList(T p, Function<String, O> func) {
        return getOptAnyList(p, func).get();
    }

    default List<Boolean> getBoolList(T p) {
        return getOptBoolList(p).get();
    }

    default SafeOpt<List<Boolean>> getOptBoolList(T p) {
        return getOptAnyList(p, Boolean::parseBoolean);
    }

    default List<Integer> getIntList(T p) {
        return getOptIntList(p).get();
    }

    default SafeOpt<List<Integer>> getOptIntList(T p) {
        return getOptAnyList(p, Integer::parseInt);
    }

    default List<Long> getLongList(T p) {
        return getOptLongList(p).get();
    }

    default SafeOpt<List<Long>> getOptLongList(T p) {
        return getOptAnyList(p, Long::parseLong);
    }

    default List<Float> getFloatList(T p) {
        return getOptFloatList(p).get();
    }

    default SafeOpt<List<Float>> getOptFloatList(T p) {
        return getOptAnyList(p, Float::parseFloat);
    }

    default List<Double> getDoubleList(T p) {
        return getOptDoubleList(p).get();
    }

    default SafeOpt<List<Double>> getOptDoubleList(T p) {
        return getOptAnyList(p, Double::parseDouble);
    }

    default List<String> getStringList(T p) {
        return getOptStringList(p).get();
    }

    default SafeOpt<List<String>> getOptStringList(T p) {
        return getOptAnyList(p, s -> s);
    }
}
