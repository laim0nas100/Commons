/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.datafill.NumberFill;
import lt.lb.commons.func.StreamMapper.StreamDecorator;
import lt.lb.commons.func.StreamMappers;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.misc.rng.RandomDistribution;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class FTest {

    public void toFind(int toFind, Integer[] array) {
        List<Integer> list = Arrays.asList(array);
        Stream<Integer> stream = list.stream();

        Iter<Integer> f = (i, value) -> value == toFind;
        Optional<Integer> find1 = F.find(array, f).map(m -> m.g1);
        Optional<Integer> find2 = F.find(list, f).map(m -> m.g1);
        Optional<Integer> find3 = F.find(stream, f).map(m -> m.g1);

        Optional<Integer> find4 = F.findBackwards(array, f).map(m -> m.g1);
        Optional<Integer> find5 = F.findBackwards(list, f).map(m -> m.g1);

        int indexOf = list.indexOf(toFind);
        assertThat(Optional.of(indexOf))
                .isEqualTo(find1)
                .isEqualTo(find2)
                .isEqualTo(find3)
                .isEqualTo(find4)
                .isEqualTo(find5);
    }

    public void toFindNoBackwards(int toFind, Integer[] array) {
        List<Integer> list = Arrays.asList(array);
        Stream<Integer> stream = list.stream();

        int randomPick = toFind;

        IntegerValue count1 = new IntegerValue(0);
        IntegerValue count2 = new IntegerValue(0);
        IntegerValue count3 = new IntegerValue(0);

        Optional<Integer> find1 = F.find(array, (i, a) -> {
            count1.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.g1);
        Optional<Integer> find2 = F.find(list, (i, a) -> {
            count2.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.g1);
        Optional<Integer> find3 = F.find(stream, (i, a) -> {
            count3.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.g1);

        IntegerValue count4 = new IntegerValue(0);
        IntegerValue count5 = new IntegerValue(0);
        Optional<Integer> find4 = F.findBackwards(array, (i, a) -> {
            count4.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.g1);
        Optional<Integer> find5 = F.findBackwards(list, (i, a) -> {
            count5.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.g1);

        assertThat(Optional.of(list.indexOf(randomPick)))
                .isEqualTo(find1)
                .isEqualTo(find2)
                .isEqualTo(find3);

        assertThat(Optional.of(list.lastIndexOf(randomPick)))
                .isEqualTo(find4)
                .isEqualTo(find5);

        assertThat(count1)
                .isEqualTo(count2)
                .isEqualTo(count3);

        assertThat(count4).isEqualTo(count5);

    }

    @Test
    public void findTest() {

        Integer[] array = ArrayOp.asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        for (Integer toFind : array) {
            toFind(toFind, array);
        }

    }

    @Test
    public void findTestRng() {
        RandomDistribution rng = RandomDistribution.uniform(new Random());

        Integer[] array = NumberFill.fillArray(1337, rng.getIntegerSupplier(1000), Integer.class);

        for (Integer toFind : array) {
            toFindNoBackwards(toFind, array);
        }
        Integer[] distinctArray = StreamDecorator.of(Integer.class)
                .apply(StreamMappers.distinct(Equator.primitiveEquator()))
                .toArray(s -> new Integer[s])
                .startingWith(array);
        for (Integer toFind : distinctArray) {
            toFindNoBackwards(toFind, distinctArray);
            toFind(toFind, distinctArray);
        }
    }

    public void testFilter(Predicate<Integer> filter, Predicate<Integer> filter2) {
        RandomDistribution rng = RandomDistribution.uniform(new Random());

        Integer[] array = NumberFill.fillArray(133, rng.getIntegerSupplier(10), Integer.class);
        for (int i = 0; i < 5; i++) {
            Integer nextInt = rng.nextInt(array.length);
            array[nextInt] = null;
        }

        List<Integer> list = Arrays.asList(array);

        for (int i = 0; i < 10; i++) {
            List<Integer> dis1 = list.stream().parallel().sorted(ExtComparator.ofComparable(true)).filter(filter).collect(Collectors.toList());
            List<Integer> dis2 = list.stream().parallel().sorted(ExtComparator.ofComparable(true)).filter(filter2).collect(Collectors.toList());
            assertThat(dis1)
                    .isEqualTo(dis2);
        }
    }

}
