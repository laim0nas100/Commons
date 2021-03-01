/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core.iteration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.interfaces.CloneSupport;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.general.IterationAbstract;
import lt.lb.commons.iteration.general.IterationIterable;
import lt.lb.commons.iteration.general.IterationIterableUnchecked;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.general.cons.unchecked.IterIterableBiConsUnchecked;
import lt.lb.commons.iteration.general.impl.SimpleIterationIterable;
import lt.lb.commons.iteration.general.impl.unchecked.SimpleIterationIterableUnchecked;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Lemmin
 */
public class IterationTest {

    public static class ExtIter<T> implements IterIterableBiConsUnchecked<T>, CloneSupport<ExtIter<T>> {

        public ExtIter(T valueToFind) {
            this.valueToFind = valueToFind;
        }
        public final T valueToFind;
        public int timesVisited;

        public boolean found;

        public List<IterIterableResult<T>> list = new ArrayList<>();

        public ExtIter<T> addExpected(int index, T val) {
            this.list.add(new IterIterableResult<>(index, val));
            timesVisited++;
            return this;
        }

        @Override
        public String toString() {
            return "ExtIter{" + "valueToFind=" + valueToFind + ", timesVisited=" + timesVisited + ", found=" + found + ", list=" + list + '}';
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + Objects.hashCode(this.valueToFind);
            hash = 41 * hash + this.timesVisited;
            hash = 41 * hash + (this.found ? 1 : 0);
            hash = 41 * hash + Objects.hashCode(this.list);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ExtIter<?> other = (ExtIter<?>) obj;
            if (this.timesVisited != other.timesVisited) {
                return false;
            }
            if (this.found != other.found) {
                return false;
            }
            if (!Objects.equals(this.valueToFind, other.valueToFind)) {
                return false;
            }
            if (!Objects.equals(this.list, other.list)) {
                return false;
            }
            return true;
        }

        @Override
        public ExtIter<T> clone() {
            ExtIter<T> copy = new ExtIter<>(this.valueToFind);
            copy.found = this.found;
            copy.list = new ArrayList<>(this.list);
            copy.timesVisited = this.timesVisited;
            return copy;
        }

        @Override
        public boolean uncheckedVisit(Integer index, T value) throws Throwable {
            addExpected(index, value);
            found = Objects.equals(value, valueToFind);
            return found;
        }

    }

    public static ExtIter<Integer> find(Integer val) {
        return new ExtIter<>(val);
    }

    public <T> void testAll(T[] array, ExtIter<T> expected, Supplier<ExtIter<T>> supplier,
            IterationIterable iteration, IterationIterableUnchecked iterationUnchecked) {
        List<T> list = Arrays.asList(array);

        if (iteration != null) {
            ExtIter<T> find1 = supplier.get();
            iteration.find(array, find1);
            assertThat(find1).isEqualTo(expected);

            ExtIter<T> find2 = supplier.get();
            iteration.find(list, find2);
            assertThat(find2).isEqualTo(expected);

            ExtIter<T> find3 = supplier.get();
            iteration.find(list.iterator(), find3);
            assertThat(find3).isEqualTo(expected); // with plus

            ExtIter<T> find4 = supplier.get();
            iteration.find(list.stream(), find4);
            assertThat(find4).isEqualTo(expected);

            ExtIter<T> find5 = supplier.get();
            iteration.find(new ArrayDeque<>(list), find5);
            assertThat(find5).isEqualTo(expected);
        }

        if (iterationUnchecked != null) {
            ExtIter<T> find1 = supplier.get();
            iterationUnchecked.find(array, find1);
            assertThat(find1).isEqualTo(expected);

            ExtIter<T> find2 = supplier.get();
            iterationUnchecked.find(list, find2);
            assertThat(find2).isEqualTo(expected);

            ExtIter<T> find3 = supplier.get();
            iterationUnchecked.find(list.iterator(), find3);
            assertThat(find3).isEqualTo(expected);

            ExtIter<T> find4 = supplier.get();
            iterationUnchecked.find(list.stream(), find4);
            assertThat(find4).isEqualTo(expected);

            ExtIter<T> find5 = supplier.get();
            iterationUnchecked.find(new ArrayDeque<>(list), find5);
            assertThat(find5).isEqualTo(expected);
        }

    }

    public <T> void testAllReverse(T[] array, ExtIter<T> expected, Supplier<ExtIter<T>> supplier,
            IterationIterable iteration, IterationIterableUnchecked iterationUnchecked) {
        List<T> list = Arrays.asList(array);

        if (iteration != null) {
            ExtIter<T> find1 = supplier.get();
            iteration.findBackwards(array, find1);
            assertThat(find1).isEqualTo(expected);

            ExtIter<T> find2 = supplier.get();
            iteration.findBackwards(list, find2);
            assertThat(find2).isEqualTo(expected);

            ExtIter<T> find5 = supplier.get();
            iteration.findBackwards(new ArrayDeque<>(list), find5);
            assertThat(find5).isEqualTo(expected);
        }

        if (iterationUnchecked != null) {
            ExtIter<T> find1 = supplier.get();
            iterationUnchecked.findBackwards(array, find1);
            assertThat(find1).isEqualTo(expected);

            ExtIter<T> find2 = supplier.get();
            iterationUnchecked.findBackwards(list, find2);
            assertThat(find2).isEqualTo(expected);

            ExtIter<T> find5 = supplier.get();
            iterationUnchecked.findBackwards(new ArrayDeque<>(list), find5);
            assertThat(find5).isEqualTo(expected);
        }

    }

    @Test
    public void newForTest() {
        Integer[] array = ArrayOp.asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        SimpleIterationIterable from_2_8 = For.elements().startingFrom(2).endingBefore(8);
        SimpleIterationIterableUnchecked from_2_8_unchecked = For.elementsUnchecked().startingFrom(2).endingBefore(8);

        ExtIter<Integer> expectedFrom_2_8 = find(6);
        expectedFrom_2_8.found = true;
        expectedFrom_2_8
                .addExpected(2, 3)
                .addExpected(3, 4)
                .addExpected(4, 5)
                .addExpected(5, 6);

        testAll(array, expectedFrom_2_8, () -> find(6), from_2_8, from_2_8_unchecked);

        SimpleIterationIterable from_1_7_first_4 = For.elements().startingFrom(1).endingBefore(7).first(4);
        SimpleIterationIterableUnchecked from_1_7_first_4_unchecked = For.elementsUnchecked().startingFrom(1).endingBefore(7).first(4);

        ExtIter<Integer> expectedFrom_1_7_first_4 = find(6);
        expectedFrom_1_7_first_4.found = false;
        expectedFrom_1_7_first_4
                .addExpected(1, 2)
                .addExpected(2, 3)
                .addExpected(3, 4)
                .addExpected(4, 5);

        testAll(array, expectedFrom_1_7_first_4, () -> find(6), from_1_7_first_4, from_1_7_first_4_unchecked);

        SimpleIterationIterable from_1_7_last_4 = For.elements().startingFrom(1).endingBefore(7).last(4);
        SimpleIterationIterableUnchecked from_1_7_last_4_unchecked = For.elementsUnchecked().startingFrom(1).endingBefore(7).last(4);

        ExtIter<Integer> expectedFrom_1_7_last_4 = find(6);
        expectedFrom_1_7_last_4.found = true;
        //4, 5, 6, 7
        expectedFrom_1_7_last_4
                .addExpected(3, 4)
                .addExpected(4, 5)
                .addExpected(5, 6);

        testAll(array, expectedFrom_1_7_last_4, () -> find(6), from_1_7_last_4, from_1_7_last_4_unchecked);

        SimpleIterationIterable from_1_7_last_4_reverse = For.elements().startingFrom(1).endingBefore(7).last(4);
        SimpleIterationIterableUnchecked from_1_7_last_4_reverse_uncheked = For.elementsUnchecked().startingFrom(1).endingBefore(7).last(4);

        ExtIter<Integer> expectedFrom_1_7_last_4_reverse = find(6);
        expectedFrom_1_7_last_4_reverse.found = true;
        //4, 5, 6, 7
        expectedFrom_1_7_last_4_reverse
                .addExpected(6, 7)
                .addExpected(5, 6);

        testAllReverse(array, expectedFrom_1_7_last_4_reverse, () -> find(6), from_1_7_last_4_reverse, from_1_7_last_4_reverse_uncheked);

        SafeOpt<Void> caughtException = For.elementsUnchecked().iterate(Arrays.asList(1, 2, 3, 4, 5, 6, 7), (i, item) -> {
            if (i == 3) {
                throw new Error("Fail on index 3");
            }
        });

        assertThat(caughtException.getError().get()).isInstanceOf(Error.class);

        Assertions.assertThatExceptionOfType(Error.class).isThrownBy(() -> {
            SafeOpt<Void> irrelevant = For.elements().iterate(Arrays.asList(1, 2, 3, 4, 5, 6, 7), (i, item) -> {
                if (i == 3) {
                    throw new Error("Fail on index 3");
                }
            });
        });

    }
}
