/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.containers.IntegerValue;
import lt.lb.commons.datafill.NumberFill;
import lt.lb.commons.misc.rng.RandomDistribution;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Laimonas Beniušis
 */
public class FTest {

    @Test
    public void findTest() {

        Integer[] array = ArrayOp.asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> list = Arrays.asList(array);
        Stream<Integer> stream = list.stream();

        Optional<Integer> find1 = F.find(array, (i, a) -> a == 8).map(m -> m.g1);
        Optional<Integer> find2 = F.find(list, (i, a) -> a == 8).map(m -> m.g1);
        Optional<Integer> find3 = F.find(stream, (i, a) -> a == 8).map(m -> m.g1);

        Optional<Integer> find4 = F.findBackwards(array, (i, a) -> a == 8).map(m -> m.g1);
        Optional<Integer> find5 = F.findBackwards(list, (i, a) -> a == 8).map(m -> m.g1);

        assertThat(find1)
                .isEqualTo(find2)
                .isEqualTo(find3)
                .isEqualTo(find4)
                .isEqualTo(find5);
    }

    @Test
    public void findTestRng() {
        Random ran = new Random();

        RandomDistribution rng = RandomDistribution.uniform(ran);

        Integer[] array = NumberFill.fillArray(1333, rng.getIntegerSupplier(10000), Integer.class);

        List<Integer> list = Arrays.asList(array);
        Stream<Integer> stream = list.stream();

        int randomPick = rng.pickRandom(list);

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

        assertThat(find1)
                .isEqualTo(find2)
                .isEqualTo(find3);
        
        assertThat(find4)
                .isEqualTo(find5);

        assertThat(count1)
                .isEqualTo(count2)
                .isEqualTo(count3);

        assertThat(count4).isEqualTo(count5);

        assertThat(count1).isNotEqualTo(count4);
    }

}
