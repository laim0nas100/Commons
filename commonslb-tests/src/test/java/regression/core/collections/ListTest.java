/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.collections.ListDeque;
import lt.lb.commons.containers.collections.PagedHashList;
import lt.lb.commons.containers.collections.PagedList;
import lt.lb.commons.containers.collections.PrefillArrayList;
import lt.lb.commons.misc.rng.RandomDistribution;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class ListTest {

    public static final long LONG_MOD = 10L;

    public static void assertEquals(int i, List l1, List testing, Object o1, Object o2) {
        if (!Objects.equals(o1, o2)) {
            LineStringBuilder sb = new LineStringBuilder();

            sb.append("Not equal at index ").append(i).append(" ").append(o1).append(" ")
                    .append(o2).appendLine().append(testing.getClass());
            String toString = sb.appendLine().appendAsLines(l1, testing).toString();
            throw new IllegalStateException(toString);
        }
    }

    public static void listEquals(List l1, List testing) {
        if (l1.size() != testing.size()) {
            throw new IllegalStateException("Size is not equal:" + l1.size() + " " + testing.size() + " " + testing);
        }
        int size = l1.size();
        for (int i = 0; i < size; i++) {
            Object get = l1.get(i);
            Object get1 = testing.get(i);
            assertEquals(i, l1, testing, get, get1);
        }
        ListIterator li1 = l1.listIterator();
        ListIterator li2 = testing.listIterator();
        int i = 0;
        while (li1.hasNext() || li2.hasNext()) {

            Object next = li1.next();
            Object next1 = li2.next();
            assertEquals(i++, l1, testing, next, next1);
        }

        ListIterator li1b = l1.listIterator(size);
        ListIterator li2b = testing.listIterator(size);
        int i2 = 0;
        while (li1b.hasPrevious() || li2b.hasPrevious()) {
            Object previous = li1b.previous();
            Object previous1 = li2b.previous();
            assertEquals(i2++, l1, testing, previous, previous1);
        }
    }

    static interface ListOp {

        public void d(List list, int rngSeed, int size);
        public static ListOp add = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                long l = r.nextLong() % LONG_MOD;
                list.add(l);
            }
        };

        public static ListOp randomAdd = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                int l = r.nextInt(list.size());
                long item = r.nextLong() % LONG_MOD;
                list.add(l, item);
            }
        };

        public static ListOp remove = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                list.remove(0);
            }
        };

        public static ListOp randomRemove = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                int l = r.nextInt(list.size());
                list.remove(l);
            }
        };

        public static ListOp addAll = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                int l = r.nextInt(list.size());
                ArrayList<Long> bulkAdd = new ArrayList<>();
                for (int j = 0; j < l; j++) {
                    i++;
                    long item = r.nextLong() % LONG_MOD;
                    bulkAdd.add(item);
                }
                list.addAll(bulkAdd);
            }
        };
        public static ListOp randomAddAll = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                int l = r.nextInt(size);
                ArrayList<Long> bulkAdd = new ArrayList<>();
                for (int j = 0; j < l && i < size; j++) {
                    i++;
                    long item = r.nextLong() % LONG_MOD;
                    bulkAdd.add(item);
                }
                int addIndex = r.nextInt(list.size());
                list.addAll(addIndex, bulkAdd);
            }
        };

        public static ListOp sort = (List list, int rngSeed, int size) -> {
            Collections.sort(list);
        };

        public static ListOp shuffle = (List list, int rngSeed, int size) -> {
            Collections.shuffle(list, new Random(rngSeed));
        };
    }

    public void listBehaviourTest(List<Long> toTest, List<Long> safeList, int rndSeed, int size) {
        toTest.clear();
        safeList.clear();
        ListOp.add.d(safeList, rndSeed, size);
        ListOp.add.d(toTest, rndSeed, size);

        listEquals(safeList, toTest);

        ListOp.remove.d(safeList, rndSeed, size);
        ListOp.remove.d(toTest, rndSeed, size);

        listEquals(safeList, toTest);

        ListOp.add.d(safeList, rndSeed, size);
        ListOp.add.d(toTest, rndSeed, size);
        listEquals(safeList, toTest);
        ListOp.randomRemove.d(safeList, rndSeed, size / 2);
        ListOp.randomRemove.d(toTest, rndSeed, size / 2);

        listEquals(safeList, toTest);
        ListOp.addAll.d(safeList, rndSeed, size);
        ListOp.addAll.d(toTest, rndSeed, size);
        listEquals(safeList, toTest);

        safeList.clear();
        toTest.clear();
        toTest.add(0L);
        safeList.add(0L);

        ListOp.randomAddAll.d(safeList, rndSeed, size);
        ListOp.randomAddAll.d(toTest, rndSeed, size);
        listEquals(safeList, toTest);

        ListOp.sort.d(safeList, rndSeed, size);
        ListOp.sort.d(toTest, rndSeed, size);
        listEquals(safeList, toTest);

        ListOp.shuffle.d(safeList, rndSeed, size);
        ListOp.shuffle.d(toTest, rndSeed, size);
        listEquals(safeList, toTest);

        safeList.clear();
        toTest.clear();
        toTest.add(0L);
        safeList.add(0L);
        ListOp.randomAdd.d(safeList, rndSeed, size);
        ListOp.randomAdd.d(toTest, rndSeed, size);
        listEquals(safeList, toTest);

    }

    @Test
    public void test() {
        List<Long> safe = new ArrayList<>();

        List<Long>[] toTest = ArrayOp.asArray(
                ListDeque.toList(new ArrayDeque<>()),
                new ArrayList<>(),
                new LinkedList<>(),
                new PagedHashList<>(),
                new PagedList<>()
        );

        for (int i = 0; i < 5; i++) {
            RandomDistribution rng = RandomDistribution.uniform(new Random(Java.getCurrentTimeMillis()+i));
            Integer seed = rng.nextInt();
            Integer size = rng.nextInt(2000, 4000);
            for (List<Long> list : toTest) {
                listBehaviourTest(list, safe, seed, size);
            }
        }

    }

    public static void main(String[] ars) {
        List<Long> safe = new ArrayList<>();
        List<Long> paged = new PagedList<>();
        for (int i = 11; i < 15; i++) {
            safe.clear();
            paged.clear();
            safe.addAll(Arrays.asList(0L, 0L, 0L));
            paged.addAll(Arrays.asList(0L, 0L, 0L));
            System.out.println("------");
            System.out.println(safe);
            System.out.println(paged);
            ListOp.randomAdd.d(safe, i, 5);
            ListOp.randomAdd.d(paged, i, 5);
            System.out.println("AFTER");
            System.out.println(safe);
            System.out.println(paged);
            listEquals(safe, paged);
        }

    }
}
