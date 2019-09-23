/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.func.StreamMapper;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class StreamMapperTest {

    @Test
    public void testOK() {

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

        Stream<Integer> stream = list.stream();

        StreamMapper<Integer, Double> combine = filter.thenApply(sorted);

        Stream<Double> decorate = combine.decorate(stream);

        assertThat(decorate.collect(Collectors.toList())).isEqualTo(Arrays.asList(25.0, 30.0, 35.0, 40.0, 45.0));

    }
}
