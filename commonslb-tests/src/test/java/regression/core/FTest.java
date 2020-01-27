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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.containers.tuples.Pair;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.datafill.NumberFill;
import lt.lb.commons.func.StreamMapper;
import lt.lb.commons.func.StreamMapper.StreamDecorator;
import lt.lb.commons.func.StreamMapperEnder;
import lt.lb.commons.func.StreamMappers;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.misc.rng.RandomDistribution;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.util.Lists;
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

    public static void main(String[] args) {
        List<Integer> list1 = Lists.newArrayList(1, 3, 5, 7, 8, 9);
        List<Integer> list2 = Lists.newArrayList(1, 2, 5, 6, 9, 7, 8, 10);

        Equator<Integer> mod3 = (a, b) -> a % 3 == b % 3;

        Log.println("Disjunction");
        Log.printLines(F.disjointPairs(list1, list2, Equator.primitiveHashEquator()));

        Log.println("Intersection");
        Log.printLines(F.intersectionPairs(list1, list2, Equator.primitiveHashEquator()));

        Log.println("Disjunction");
        Log.printLines(F.disjointPairs(list1, list2, mod3));

        Log.println("Intersection");
        ArrayList<Pair<Integer>> intersectionPairs = F.intersectionPairs(list1, list2, mod3);
        Log.printLines(intersectionPairs);

        Log.close();
    }

    @Test
    public void disjuctionTest() {
        List<Integer> list1 = Lists.newArrayList(1, 3, 5, 7, 8, 9);
        List<Integer> list2 = Lists.newArrayList(1, 2, 5, 6, 9, 7, 8, 10);

        Equator<Integer> mod3 = (a, b) -> a % 3 == b % 3;

        StreamMapperEnder<Pair<Integer>, Integer, List<Integer>> left = new StreamDecorator<Pair<Integer>>().filter(m -> m.g1 != null).map(m -> m.g1).collectToList();
        StreamMapperEnder<Pair<Integer>, Integer, List<Integer>> right = new StreamDecorator<Pair<Integer>>().filter(m -> m.g2 != null).map(m -> m.g2).collectToList();

        ArrayList<Pair<Integer>> disjointPairs1 = F.disjointPairs(list1, list2, Equator.primitiveHashEquator());

        assertThat(Lists.newArrayList(3)).containsOnlyElementsOf(left.startingWithOpt(disjointPairs1));
        assertThat(Lists.newArrayList(2, 6, 10)).containsOnlyElementsOf(right.startingWithOpt(disjointPairs1));

        ArrayList<Pair<Integer>> intersectionPairs1 = F.intersectionPairs(list1, list2, Equator.primitiveHashEquator());

        assertThat(Lists.newArrayList(1, 5, 7, 8, 9)).containsExactlyElementsOf(left.startingWithOpt(intersectionPairs1));
        assertThat(Lists.newArrayList(1, 5, 9, 7, 8)).containsExactlyElementsOf(right.startingWithOpt(intersectionPairs1));

        ArrayList<Pair<Integer>> disjointPairs2 = F.disjointPairs(list1, list2, mod3);

        assertThat(disjointPairs2).isEmpty();

        ArrayList<Pair<Integer>> intersectionPairs2 = F.intersectionPairs(list1, list2, mod3);

        assertThat(Lists.newArrayList(1, 3, 5)).containsExactlyElementsOf(left.startingWithOpt(intersectionPairs2));
        assertThat(Lists.newArrayList(1, 2, 6)).containsExactlyElementsOf(right.startingWithOpt(intersectionPairs2));

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
