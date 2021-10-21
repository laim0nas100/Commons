package regression.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.DLog;
import lt.lb.commons.Equator;
import lt.lb.commons.containers.collections.CollectionOp;
import lt.lb.commons.containers.tuples.Pair;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.datafill.NumberFill;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.streams.StreamMapper.StreamDecorator;
import lt.lb.commons.iteration.streams.StreamMapperEnder;
import lt.lb.commons.iteration.streams.StreamMappers;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.readablecompare.Compare;
import lt.lb.readablecompare.CompareNull;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.util.Lists;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class CollectionOpTest {

    public void toFind(int toFind, Integer[] array) {
        List<Integer> list = Arrays.asList(array);
        Stream<Integer> stream = list.stream();

        IterIterableBiCons<Integer> f = (i, value) -> value == toFind;
        Optional<Integer> find1 = For.elements().find(array, f).map(m -> m.index);
        Optional<Integer> find2 = For.elements().find(list, f).map(m -> m.index);
        Optional<Integer> find3 = For.elements().find(stream, f).map(m -> m.index);

        Optional<Integer> find4 = For.elements().findBackwards(array, f).map(m -> m.index);
        Optional<Integer> find5 = For.elements().findBackwards(list, f).map(m -> m.index);

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

        Optional<Integer> find1 = For.elements().find(array, (i, a) -> {
            count1.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.index);
        Optional<Integer> find2 = For.elements().find(list, (i, a) -> {
            count2.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.index);
        Optional<Integer> find3 = For.elements().find(stream, (i, a) -> {
            count3.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.index);

        IntegerValue count4 = new IntegerValue(0);
        IntegerValue count5 = new IntegerValue(0);
        Optional<Integer> find4 = For.elements().findBackwards(array, (i, a) -> {
            count4.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.index);
        Optional<Integer> find5 = For.elements().findBackwards(list, (i, a) -> {
            count5.incrementAndGet();
            return a == randomPick;
        }).map(m -> m.index);

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
                .apply(StreamMappers.distinct(Equator.simpleEquator()))
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

        DLog.println("Disjunction");
        DLog.printLines(CollectionOp.disjointPairs(list1, list2, Equator.simpleHashEquator()));

        DLog.println("Intersection");
        DLog.printLines(CollectionOp.intersectionPairs(list1, list2, Equator.simpleHashEquator()));

        DLog.println("Disjunction");
        DLog.printLines(CollectionOp.disjointPairs(list1, list2, mod3));

        DLog.println("Intersection");
        ArrayList<Pair<Integer>> intersectionPairs = CollectionOp.intersectionPairs(list1, list2, mod3);
        DLog.printLines(intersectionPairs);

        DLog.close();
    }

    @Test
    public void disjuctionTest() {
        List<Integer> list1 = Lists.newArrayList(1, 3, 5, 7, 8, 9);
        List<Integer> list2 = Lists.newArrayList(1, 2, 5, 6, 9, 7, 8, 10);

        Equator<Integer> mod3 = (a, b) -> a % 3 == b % 3;

        StreamMapperEnder<Pair<Integer>, Integer, List<Integer>> left = new StreamDecorator<Pair<Integer>>().filter(m -> m.g1 != null).map(m -> m.g1).collectToList();
        StreamMapperEnder<Pair<Integer>, Integer, List<Integer>> right = new StreamDecorator<Pair<Integer>>().filter(m -> m.g2 != null).map(m -> m.g2).collectToList();

        ArrayList<Pair<Integer>> disjointPairs1 = CollectionOp.disjointPairs(list1, list2, Equator.simpleHashEquator());

        assertThat(Lists.newArrayList(3)).containsOnlyElementsOf(left.startingWithOpt(disjointPairs1));
        assertThat(Lists.newArrayList(2, 6, 10)).containsOnlyElementsOf(right.startingWithOpt(disjointPairs1));

        ArrayList<Pair<Integer>> intersectionPairs1 = CollectionOp.intersectionPairs(list1, list2, Equator.simpleHashEquator());

        assertThat(Lists.newArrayList(1, 5, 7, 8, 9)).containsExactlyElementsOf(left.startingWithOpt(intersectionPairs1));
        assertThat(Lists.newArrayList(1, 5, 9, 7, 8)).containsExactlyElementsOf(right.startingWithOpt(intersectionPairs1));

        ArrayList<Pair<Integer>> disjointPairs2 = CollectionOp.disjointPairs(list1, list2, mod3);

        assertThat(disjointPairs2).isEmpty();

        ArrayList<Pair<Integer>> intersectionPairs2 = CollectionOp.intersectionPairs(list1, list2, mod3);

        assertThat(Lists.newArrayList(1, 3, 5)).containsExactlyElementsOf(left.startingWithOpt(intersectionPairs2));
        assertThat(Lists.newArrayList(1, 2, 6)).containsExactlyElementsOf(right.startingWithOpt(intersectionPairs2));

    }

    @Test
    public void testCompareNull() {
        RandomDistribution rng = RandomDistribution.uniform(new Random());

        Integer[] array = NumberFill.fillArray(rng.nextInt(100, 200), rng.getIntegerSupplier(20), Integer.class);
        int nulls = rng.nextInt(5, 10);
        for (int i = 0; i < nulls; i++) {

            Integer nextInt = rng.nextInt(array.length);
            while (array[nextInt] == null) {
                nextInt = rng.nextInt(array.length);
            }
            array[nextInt] = null;
        }

        List<Integer> list = Arrays.asList(array);
        List<Integer> dis1 = list.stream().parallel().sorted(Compare.of(CompareNull.NULL_LOWER)).collect(Collectors.toList());
        List<Integer> dis2 = list.stream().parallel().sorted(Compare.of(CompareNull.NULL_HIGHER)).collect(Collectors.toList());

        List<Integer> fil1 = new ArrayList<>();
        List<Integer> fil2 = new ArrayList<>();
        For.elements().startingFrom(0).endingBefore(nulls).iterate(dis1, (i, item) -> {
            fil1.add(item);
        });
        For.elements().startingFrom(dis2.size() - nulls).iterate(dis2, (i, item) -> {
            fil2.add(item);
        });

        assertThat(fil1).isEqualTo(fil2);

        For.elements().first(nulls).iterate(dis1, (i, item) -> {
            fil1.add(item);
        });
        For.elements().last(nulls).iterate(dis2, (i, item) -> {
            fil2.add(item);
        });
        assertThat(fil1).isEqualTo(fil2);
    }

}
