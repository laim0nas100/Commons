package regression.core;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lt.lb.commons.Equator;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.commons.iteration.streams.SimpleStream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class SimpleStreamTest {

    @Test
    public void test1() {

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        List<Double> sortedList = MakeStream.from(list)
                .map(m -> m * 10)
                .filter(m -> m > 22)
                .map(String::valueOf)
                .filter(m -> m.contains("0"))
                .flatMap(m -> Stream.of(Integer.parseInt(m), Integer.parseInt(m) - 5))
                .map(m -> (double) m)
                .filter(m -> m <= 45.1)
                .sorted().toList();

        assertThat(sortedList).isEqualTo(Arrays.asList(25.0, 30.0, 35.0, 40.0, 45.0));

    }

    @Test
    public void test2() {

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        List<Double> sortedList = MakeStream.from(list)
                .map(m -> m * 10)
                .map(m -> m)
                .distinct(Equator.valueHashEquator(a -> a % 5)) // dsitinct by equality modulo 5
                .append(7)
                .map(String::valueOf)
                .flatMap(m -> Stream.of(Integer.parseInt(m), Integer.parseInt(m) - 5))
                .map(m -> (double) m)
                .sorted().toList();

        assertThat(sortedList).isEqualTo(Arrays.asList(2.0, 5.0, 7.0, 10.0));

    }

    @Test
    public void test3() {
        List<Double> sortedList = new SimpleStream<Integer>(Stream.empty())
                .append(1, 2, 3, 4, 5)
                .map(m -> m * 10)
                .distinct(Equator.valueHashEquator(a -> a % 5)) // dsitinct by equality modulo 5
                .append(7)
                .map(String::valueOf)
                .flatMap(m -> Stream.of(Integer.parseInt(m), Integer.parseInt(m) - 5))
                .map(m -> (double) m)
                .sorted().toList();

        assertThat(sortedList).isEqualTo(Arrays.asList(2.0, 5.0, 7.0, 10.0));
    }

    private static class TestClass4 {

        public int i;

        public TestClass4(int num) {
            i = num;
        }
    }

    @Test
    public void test4() {

        Set<TestClass4> set = MakeStream.fromValues(1, 2, 3, 4, 5).map(m -> m * 2).map(TestClass4::new).toSet();

        List<Integer> toList = MakeStream.fromValues(1, 2, 3, 4, 5).map(TestClass4::new).notIn(set, Equator.valueHashEquator(a -> a.i)).map(m -> m.i).toList();

        assertThat(toList).isEqualTo(Arrays.asList(1, 3, 5));
    }
}
