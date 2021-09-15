package regression.core.parsing;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lt.lb.commons.parsing.numbers.FastParse;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class FastParseTest {

    public static <E, T> void assertAll(List<E> list, List<T> result, Function<E, T> function) {
        for (int i = 0; i < list.size(); i++) {
            T apply = function.apply(list.get(i));
            Assertions.assertThat(result.get(i)).isEqualTo(apply);
        }
    }

    public List<String> getStrInt() {
        return Arrays.asList("1", "2", "3", "-1", "-2", "-3", "1111111", "null", "NaN", "-+2", "  2");
    }

    public List<Integer> getInts() {
        return Arrays.asList(1, 2, 3, -1, -2, -3, 1111111, null, null, null, null);
    }

    public List<Long> getLongs() {
        return getInts().stream()
                .map(m -> m == null ? null : m.longValue())
                .collect(Collectors.toList());
    }

    public List<Float> getFloats() {
        return getInts().stream()
                .map(m -> m == null ? null : m.floatValue())
                .collect(Collectors.toList());
    }

    public List<Double> getDoubles() {
        return getInts().stream()
                .map(m -> m == null ? null : m.doubleValue())
                .collect(Collectors.toList());
    }

    @Test
    public void testParse() {
        assertAll(getStrInt(), getInts(), FastParse::parseInt);
        assertAll(getStrInt(), getLongs(), FastParse::parseLong);
        assertAll(getStrInt(), getFloats(), FastParse::parseFloat);
        assertAll(getStrInt(), getDoubles(), FastParse::parseDouble);
    }

}
