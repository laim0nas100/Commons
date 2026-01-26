package lt.lb.commons.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 *
 * Generalized way to parse common values from string.
 *
 * @author laim0nas100
 * @param <T>
 */
@FunctionalInterface
public interface StringParser<T> extends Function<T, SafeOpt<String>> {

    /**
     * Splits the string by occurrences of the given literal separator string.
     * <p>
     * This method behaves like {@link String#split(String)} with a literal
     * separator, but preserves trailing empty strings and does not interpret
     * the separator as a regex.
     * <p>
     * <b>Examples:</b>
     * <pre>{@code
     * split("a,b,c", ",")     → ["a", "b", "c"]
     * split("a,,b,", ",")     → ["a", "", "b", ""]
     * split("", ",")          → [""]
     * split("hello", "xyz")   → ["hello"]
     * split("a;b;c", ";")     → ["a", "b", "c"]
     * }</pre>
     *
     * @param source the input string (must not be null)
     * @param separator the literal delimiter (must not be null or empty)
     * @return array of substrings (never null, may contain empty strings)
     * @throws NullPointerException if source or separator is null
     * @throws IllegalArgumentException if separator is empty
     */
    public static ArrayList<String> split(String source, String separator) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(separator, "separator");
        if (separator.isEmpty()) {
            throw new IllegalArgumentException("separator must not be empty");
        }
        ArrayList<String> parts = new ArrayList<>();
        int index = 0;
        int len = separator.length();
        if (len == 1) {//split by char
            char sep = separator.charAt(0);
            for (;;) {
                int next = source.indexOf(sep, index);
                if (next < 0) {
                    parts.add(source.substring(index));
                    break;
                }
                parts.add(source.substring(index, next));
                index = next + 1;
            }
        } else {
            for (;;) {
                int next = source.indexOf(separator, index);
                if (next < 0) {
                    parts.add(source.substring(index));
                    break;
                } else {
                    parts.add(source.substring(index, next));
                    index = next + len;
                }
            }
        }
        return parts;

    }

    public static <V> StringParser<V> of(Function<V, String> func) {
        Objects.requireNonNull(func);
        return val -> SafeOpt.ofNullable(val).map(func);
    }

    public static <V> StringParser<V> of(UncheckedFunction<V, String> func) {
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

    default <O> SafeOpt<O> parseOptAny(T p, UncheckedFunction<String, O> func) {
        return parseOptAny(p, (Function<String, O>) func);
    }

    default <O> O parseAny(T p, Function<String, O> func) {
        return parseOptAny(p, func).get();
    }

    default <O> O parseAny(T p, UncheckedFunction<String, O> func) {
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
        string = Strings.CS.remove(string, "[");
        string = Strings.CS.remove(string, "]");
        return split(string, ", ").stream().toArray(s -> new String[s]);
    }

    default <O> SafeOpt<List<O>> parseOptAnyList(T p, Function<String, O> func) {
        return parseOptString(p)
                .map(s -> parseArray(s))
                .map(Stream::of)
                .map(stream -> stream.map(func))
                .map(stream -> stream.collect(Collectors.toList()));
    }

    default <O> SafeOpt<List<O>> parseOptAnyList(T p, UncheckedFunction<String, O> func) {
        return parseOptAnyList(p, (Function<String, O>) func);
    }

    default <O> List<O> parseAnyList(T p, Function<String, O> func) {
        return parseOptAnyList(p, func).get();
    }

    default <O> List<O> parseAnyList(T p, UncheckedFunction<String, O> func) {
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
