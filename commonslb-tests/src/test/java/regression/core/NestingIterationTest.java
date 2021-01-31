package regression.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lt.lb.commons.F;
import lt.lb.commons.containers.CastIndexedList;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.IterProvider;
import lt.lb.commons.iteration.NestingIteration;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.rng.RandomDistribution;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class NestingIterationTest {

    @Test
    public void test() {
        RandomDistribution rng = RandomDistribution.uniform(new Random());
        Integer bound = 10;
        F.uncheckedRun(() -> {
            int[] sizes = new int[bound];
            ArrayList<IterProvider<Integer>> list = new ArrayList<>();
            for (int i = 0; i < bound; i++) {
                sizes[i] = rng.nextInt(0, 7);
                list.add(upTo(sizes[i]));
            }

            Iterator<CastIndexedList<Integer>> iterator = NestingIteration.iterator(list, false);
            For.elements().iterate(iterator, (i, c) -> {
                assertCast(sizes, c);
            });

        });

        F.uncheckedRun(() -> {
            ArrayList<Iterator<Integer>> list = new ArrayList<>();
            int[] sizes = new int[bound];
            for (int i = 0; i < bound; i++) {
                sizes[i] = rng.nextInt(0, 7);
                list.add(upToIter(sizes[i]));
            }
            Iterator<CastIndexedList<Integer>> iterator = NestingIteration.lazyInitIterator(list, false);
            For.elements().iterate(iterator, (i, c) -> {
                assertCast(sizes, c);
            });
        });
    }

    public void assertCast(int[] sizes, CastIndexedList<Integer> c) {
        List<Integer> indexList = c.asIndexList();
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] == 0) {
                indexList.set(i, null);
            }
        }
        assertThat(c.asList()).isEqualTo(indexList);
    }

    public static IterProvider<Integer> upTo(int upTo) {
        return new IterProvider<Integer>() {
            @Override
            public int size() {
                return upTo;
            }

            @Override
            public Integer get(int i) {
                return i;
            }
        };
    }

    public static ReadOnlyIterator<Integer> upToIter(int upTo) {
        Iterator<Integer> iterator = new Iterator<Integer>() {
            int current = 0;

            @Override
            public boolean hasNext() {
                return current < upTo;
            }

            @Override
            public Integer next() {
                return current++;
            }
        };

        return ReadOnlyIterator.of(iterator);
    }
}
