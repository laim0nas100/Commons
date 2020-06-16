package regression.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import lt.lb.commons.F;
import lt.lb.commons.containers.CastIndexedList;
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
        Integer bound = 5;
        F.unsafeRun(() -> {
            ArrayList<IterProvider<Integer>> list = new ArrayList<>();
            for (int i = 0; i < bound; i++) {
                list.add(upTo(rng.nextInt(10)));
            }

            Iterator<CastIndexedList<Integer>> iterator = NestingIteration.iterator(list, false);
            F.iterate(iterator, (i, c) -> {
                assertCast(c);
            });

        });

        F.unsafeRun(() -> {
            ArrayList<ReadOnlyIterator<Integer>> list = new ArrayList<>();
           
            for (int i = 0; i < bound; i++) {
                list.add(upToIter(rng.nextInt(10)));
            }
            Iterator<CastIndexedList<Integer>> iterator = NestingIteration.lazyInitIterator(list, false);
            F.iterate(iterator, (i, c) -> {
                assertCast(c);
            });
        });
    }

    public void assertCast(CastIndexedList<Integer> c) {
        assertThat(c.asList()).isEqualTo(c.asIndexList());
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
