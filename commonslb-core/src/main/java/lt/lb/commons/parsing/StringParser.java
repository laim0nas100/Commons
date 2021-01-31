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
public interface StringParser<T> extends Function<T, SafeOpt<String>> {

    public static <V> StringParser<V> of(Function<V, String> func) {
        Objects.requireNonNull(func);
        return val -> SafeOpt.ofNullable(val).map(func);
    }

    public static <V> StringParser<V> of(UnsafeFunction<V, String> func) {
        return of((Function<V, String>) func);
    }

    @Override
    default SafeOpt<String> apply(T p) {
        return parseOptString(p);
    }

    SafeOpt<String> parseOptString(T p);

    default String parseString(T p) {
        return parseOptString(p).get();
    }

    default <O> SafeOpt<O> parseOptAny(T p, Function<String, O> func) {
        return parseOptString(p).map(func);
    }

    default <O> SafeOpt<O> parseOptAny(T p, UnsafeFunction<String, O> func) {
        return parseOptAny(p, (Function<String, O>) func);
    }

    default <O> O parseAny(T p, Function<String, O> func) {
        return parseOptAny(p, func).get();
    }

    default <O> O parseAny(T p, UnsafeFunction<String, O> func) {
        return parseAny(p, (Function<String, O>) func);
    }

    default boolean parseBool(T p) {
        return parseOptBool(p).get();
    }

    default SafeOpt<Boolean> parseOptBool(T p) {
        return parseOptAny(p, Boolean::parseBoolean);
    }

    default int parseInt(T p) {
        return parseOptInt(p).get();
    }

    default SafeOpt<Integer> parseOptInt(T p) {
        return parseOptAny(p, Integer::parseInt);
    }

    default long parseLong(T p) {
        return parseOptLong(p).get();
    }

    default SafeOpt<Long> parseOptLong(T p) {
        return parseOptAny(p, Long::parseLong);
    }

    default float parseFloat(T p) {
        return parseOptFloat(p).get();
    }

    default SafeOpt<Float> parseOptFloat(T p) {
        return parseOptAny(p, Float::parseFloat);
    }

    default double parseDouble(T p) {
        return parseOptDouble(p).get();
    }

    default SafeOpt<Double> parseOptDouble(T p) {
        return parseOptAny(p, Double::parseDouble);
    }

    default String[] parseArray(String string) {
        string = StringOp.remove(string, "[");
        string = StringOp.remove(string, "]");
        return StringOp.split(string, ", ");
    }

    default <O> SafeOpt<List<O>> parseOptAnyList(T p, Function<String, O> func) {
        return parseOptString(p)
                .map(s -> parseArray(s))
                .map(Stream::of)
                .map(stream -> stream.map(func))
                .map(stream -> stream.collect(Collectors.toList()));
    }

    default <O> SafeOpt<List<O>> parseOptAnyList(T p, UnsafeFunction<String, O> func) {
        return parseOptAnyList(p, (Function<String, O>) func);
    }

    default <O> List<O> parseAnyList(T p, Function<String, O> func) {
        return parseOptAnyList(p, func).get();
    }

    default <O> List<O> parseAnyList(T p, UnsafeFunction<String, O> func) {
        return parseOptAnyList(p, func).get();
    }

    default List<Boolean> parseBoolList(T p) {
        return parseOptBoolList(p).get();
    }

    default SafeOpt<List<Boolean>> parseOptBoolList(T p) {
        return parseOptAnyList(p, Boolean::parseBoolean);
    }

    default List<Integer> parseIntList(T p) {
        return parseOptIntList(p).get();
    }

    default SafeOpt<List<Integer>> parseOptIntList(T p) {
        return parseOptAnyList(p, Integer::parseInt);
    }

    default List<Long> parseLongList(T p) {
        return parseOptLongList(p).get();
    }

    default SafeOpt<List<Long>> parseOptLongList(T p) {
        return parseOptAnyList(p, Long::parseLong);
    }

    default List<Float> parseFloatList(T p) {
        return parseOptFloatList(p).get();
    }

    default SafeOpt<List<Float>> parseOptFloatList(T p) {
        return parseOptAnyList(p, Float::parseFloat);
    }

    default List<Double> parseDoubleList(T p) {
        return parseOptDoubleList(p).get();
    }

    default SafeOpt<List<Double>> parseOptDoubleList(T p) {
        return parseOptAnyList(p, Double::parseDouble);
    }

    default List<String> parseStringList(T p) {
        return parseOptStringList(p).get();
    }

    default SafeOpt<List<String>> parseOptStringList(T p) {
        return parseOptAnyList(p, s -> s);
    }
}
