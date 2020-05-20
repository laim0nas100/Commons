package regression.core;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.func.StreamMapper;
import lt.lb.commons.func.StreamMapperEnder;
import lt.lb.commons.func.StreamMappers;
import static lt.lb.commons.func.StreamMappers.concat;
import static lt.lb.commons.func.StreamMappers.distinct;
import lt.lb.commons.interfaces.Equator;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class StreamMapperTest {

    @Test
    public void test1() {

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        StreamMapper<Integer, String> filter = StreamMapper.of(Integer.class)
                .map(m -> m * 10)
                .filter(m -> m > 22)
                .map(m -> "" + m)
                .filter(m -> m.contains("0"));
        StreamMapper<String, Double> sorted = StreamMapper.of(String.class)
                .flatMap(m -> Stream.of(Integer.parseInt(m), Integer.parseInt(m) - 5))
                .map(m -> (double) m)
                .filter(m -> m <= 45.1)
                .sorted();

        StreamMapperEnder<Integer, Double, List<Double>> collect = filter.thenCombine(sorted).collect(Collectors.toList());

        assertThat(collect.startingWithOpt(list)).isEqualTo(Arrays.asList(25.0, 30.0, 35.0, 40.0, 45.0));

    }

    @Test
    public void test2() {

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        StreamMapper<Integer, String> filter = StreamMapper.of(Integer.class)
                .map(m -> m * 10)
                .map(m -> m)
                .apply(distinct((a, b) -> a % 5 == b % 5)) // dsitinct by equality modulo 5
                .apply(concat(7))
                .map(m -> "" + m);
        StreamMapper<String, Double> sorted = StreamMapper.of(String.class)
                .flatMap(m -> Stream.of(Integer.parseInt(m), Integer.parseInt(m) - 5))
                .map(m -> (double) m)
                .sorted();

        Stream<Integer> stream = list.stream();
        StreamMapper<Integer, Double> combine = filter.thenCombine(sorted);

        Stream<Double> decorate = combine.decorate(stream);

        assertThat(decorate.collect(Collectors.toList())).isEqualTo(Arrays.asList(2.0, 5.0, 7.0, 10.0));

    }

    @Test
    public void test3() {
        StreamMapper<Integer, String> filter = StreamMapper.of(Integer.class)
                .apply(concat(1, 2, 3, 4, 5))
                .map(m -> m * 10)
                .apply(distinct((a, b) -> a % 5 == b % 5)) // dsitinct by equality modulo 5
                .apply(concat(7))
                .map(m -> "" + m);
        StreamMapper<String, Double> sorted = StreamMapper.of(String.class)
                .flatMap(m -> Stream.of(Integer.parseInt(m), Integer.parseInt(m) - 5))
                .map(m -> (double) m)
                .sorted();

        StreamMapper<Integer, Double> combine = filter.thenCombine(sorted);

        List l = null;
        List<Double> startingWith = combine.collect(Collectors.toList()).startingWithOpt(l);

        assertThat(startingWith).isEqualTo(Arrays.asList(2.0, 5.0, 7.0, 10.0));
    }

    private static class TestClass4 {

        public int i;

        public TestClass4(int num) {
            i = num;
        }
    }

    @Test
    public void test4() {
        StreamMapper<Integer, TestClass4> map1 = StreamMapper.of(Integer.class)
                .apply(concat(1, 2, 3, 4, 5))
                .map(TestClass4::new);

        Set<TestClass4> startingWith = StreamMapper.of(Integer.class)
                .apply(concat(1, 2, 3, 4, 5))
                .map(m -> m * 2)
                .map(TestClass4::new).collectToSet().startingWith();

        List<Integer> left = map1.apply(StreamMappers.filterNotIn(startingWith, Equator.valueHashEquator(a -> a.i)))
                .map(m -> m.i).collectToList().startingWith();

        assertThat(left).isEqualTo(Arrays.asList(1, 3, 5));
    }
}
