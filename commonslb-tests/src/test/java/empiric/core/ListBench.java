package empiric.core;

import java.lang.reflect.InvocationTargetException;
import lt.lb.commons.containers.collections.PagedList;
import lt.lb.commons.containers.collections.PagedHashList;
import lt.lb.commons.containers.collections.PrefillArrayMapList;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.benchmarking.BenchmarkResult;
import org.junit.Ignore;
import org.junit.Test;
import org.magicwerk.brownies.collections.BigList;
import lt.lb.uncheckedutils.func.UncheckedRunnable;
import lt.lb.uncheckedutils.Checked;
import org.magicwerk.brownies.collections.GapList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author laim0nas100
 */
public class ListBench {

    static {
        DLog.main().async = true;
    }

    private void dPrintList(PagedHashList l) {
        DLog.print(l.toString());
        DLog.printLines(l.getMappings());
    }
//    @Ignore

//    @Test
    public void testPrefillHash() {
        PrefillArrayMapList<Long> map = new PrefillArrayMapList<Long>();
        for (int i = 0; i < 10; i++) {
            map.put(i, 10L - i);
        }
        map.remove(5);
        map.remove(9);
        DLog.printLines(map.entrySet());
        DLog.print(map);
    }

    @Ignore
//    @Test
    public void test() throws InterruptedException, TimeoutException {
        PagedHashList<Long> list = new PagedHashList<>();
        for (int i = 0; i < 10; i++) {
//            dPrintList(list);
            list.add((long) i * 2);

        }

        for (int i = 0; i < 10; i++) {
            list.set(i, i * -1L);
        }
//        DLog.printLines(list.getMappings());
//        DLog.print(list.remove(9)+" ");
//        dPrintList(list);
//

        for (int i = 0; i < 14; i++) {
            list.add(0, 55L);
            dPrintList(list);
            list.add(0, 66L);
            dPrintList(list);
            list.add(0, 77L);
            dPrintList(list);
            list.add(0, 88L);
            dPrintList(list);
            list.add(4, 99L);
            dPrintList(list);
            list.remove(4);
            dPrintList(list);
            list.add(7, 100L);
            dPrintList(list);

        }

//dPrintList(list);
//        Random r = new Random(3);
//        while(!list.isEmpty()){
//            int toRemove =  r.nextInt(list.size());
//            DLog.print("remove:"+toRemove,list.remove(toRemove)+" ");
//        dPrintList(list);
//        }
//        int size = 50000;
//        int iterations = 500000;
//        DLog.print(executeBench(80, "ArrayList read", makeBenchRead(makeList(this.getBank(size, size), new ArrayList<>()), new Random(10), iterations)));
//        DLog.print(executeBench(80, "BigList read", makeBenchRead(makeList(this.getBank(size, size), new BigList<>()), new Random(10), iterations)));
//        DLog.print(executeBench(80, "ArrayList read", makeBenchRead(makeList(this.getBank(size, size), new ArrayList<>()), new Random(10), iterations)));
//        DLog.print(executeBench(80, "PagedHashList read", makeBenchRead(makeList(this.getBank(size, size), new PagedHashList<>()), new Random(10), iterations)));
//        ListIterator<Long> listIterator = list.listIterator();
//        while (listIterator.hasNext()) {
//            DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
//        }
//        listIterator.remove();
//        DLog.print(list.toString());
//        DLog.println();
//        while (listIterator.hasPrevious()) {
//            DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
//        }
//        DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex());
//        listIterator.add(13L);
//        DLog.print(list.toString());
//        DLog.println();
//        for (int i = 0; i < 10; i++) {
//            DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
//            DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
//        }
//        DLog.println();
//        DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
////        listIterator.next();
//        listIterator.add(20L);
//        DLog.print(list.toString());
//        list.remove(5);
//        DLog.print(list.toString());
//        list.add(7, 25L);
//
//        DLog.print(list.toString());
//        //TODO
//        list.add(3, 35L);
//
//        DLog.print(list.toString());
        DLog.await(1, TimeUnit.MINUTES);

    }

    public List<Long> getBank(int size, int seed) {
        ArrayList<Long> bank = new ArrayList<>(size);
        Random r = new Random(seed);
        r.longs(size).forEach(a -> {
            bank.add(a);
        });
        return bank;
    }

    public <T> Supplier<List<T>> makeList(List<T> bank, List<T> newList) {

        return () -> {
            newList.addAll(bank);
            return newList;
        };
    }
    Benchmark b = new Benchmark();

    public void benchBatch(int size, int iterations, int seed, Class<? extends List>... lists) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

        List<Long> bank = this.getBank(size, 1337);
        DLog.print("Size:", size);
        int runCount = 10;

        for (Class<? extends List> list : lists) {
            List newInstance = list.getDeclaredConstructor().newInstance();
            DLog.print(b.executeBench(runCount, list.getSimpleName() + " write", makeBenchWrite(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.getDeclaredConstructor().newInstance();
            DLog.print(b.executeBench(runCount, list.getSimpleName() + " read", makeBenchRead(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.getDeclaredConstructor().newInstance();
            DLog.print(b.executeBench(runCount, list.getSimpleName() + " read>>write", makeBenchReadWrite(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.getDeclaredConstructor().newInstance();
            DLog.print(b.executeBench(runCount, list.getSimpleName() + " read<<write", makeBenchWriteRead(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.getDeclaredConstructor().newInstance();
            DLog.print(b.executeBench(runCount, list.getSimpleName() + " random r w", makeBenchRandomWriteRead(makeList(bank, newInstance), new Random(seed), iterations)));
        }

//        DLog.print(executeBench(10, "ArrayList write", makeBenchWrite(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "PagedList write", makeBenchWrite(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "GapList write", makeBenchWrite(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "BigList write", makeBenchWrite(makeList(bank, new BigList<>()), new Random(seed), iterations)));
////        DLog.print();
//        DLog.print(executeBench(10, "ArrayList read", makeBenchRead(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "PagedList read", makeBenchRead(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "GapList read", makeBenchRead(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "BigList read", makeBenchRead(makeList(bank, new BigList<>()), new Random(seed), iterations)));
////        DLog.print();
//        DLog.print(executeBench(10, "ArrayList readWrite", makeBenchReadWrite(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "PagedList readWrite", makeBenchReadWrite(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "GapList readWrite", makeBenchReadWrite(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "BigList readWrite", makeBenchReadWrite(makeList(bank, new BigList<>()), new Random(seed), iterations)));
////        DLog.print();
//        DLog.print(executeBench(10, "ArrayList writeRead", makeBenchWriteRead(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "PagedList writeRead", makeBenchWriteRead(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "GapList writeRead", makeBenchWriteRead(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "BigList writeRead", makeBenchWriteRead(makeList(bank, new BigList<>()), new Random(seed), iterations)));
////        DLog.print();
//        DLog.print(executeBench(10, "ArrayList randomWriteRead", makeBenchRandomWriteRead(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "PagedList randomWriteRead", makeBenchRandomWriteRead(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "GapList randomWriteRead", makeBenchRandomWriteRead(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        DLog.print(executeBench(10, "BigList randomWriteRead", makeBenchRandomWriteRead(makeList(bank, new BigList<>()), new Random(seed), iterations)));
//        DLog.print();
    }

    public static void main(String[] a) throws Exception {
        DLog.main().async = true;
        DLog.main().keepBufferForFile = false;
//        DLog.disable = true;
        new ListBench().listBench();
//        PagedHashList list = new PagedHashList<>();
//        new ListBench().listBehaviourTest(new BigList<>(), new PagedHashList<>(), 1373, 100000);

//        DLog.printLines(list.getMappings());
//        DLog.print(list.getPageCount(), list.getPageSize(), list.getAveragePageSize(), list.size());
    }

    @Ignore
//    @Test
    public void listBench() throws Exception {
        DLog.print("List benchmark");
        int size = 30000000;
        int iterations = 50;
        int seed = 11;
        Class<List>[] lists = new Class[]{PagedList.class,PagedHashList.class,BigList.class};
//        Class<List>[] lists = new Class[]{PagedHashList.class, BigList.class};
//        Class<List>[] lists = new Class[]{BigList.class};
        DLog.print("Waiting for input");
        DLog.print("Start " + System.in.read());
//        benchBatch(size / 10, iterations, seed, lists);
        benchBatch(size, iterations, seed, lists);
//        benchBatch(size * 10, iterations, seed, lists);
//        benchBatch(size * 100, iterations, seed, lists);
//        benchBatch(size * 1000, iterations, seed, lists);
//        benchBatch(size*5000, iterations, seed, lists);
//        benchBatch(size*30000, iterations, seed, lists);

//        int mult = 1;
//        List<Long> bank = this.getBank(size * mult, seed);
////        DLog.print(executeBench(100, "ArrayList read", makeBenchRead(makeList(this.getBank(size * mult, seed), new ArrayList<>()), new Random(seed), iterations)));
//        DLog.print(b.executeBench(150, "BigList read", makeBenchRead(makeList(bank, new BigList<>(1000)), new Random(seed), iterations)));
//        DLog.print(b.executeBench(150, "PagedHashedList read", makeBenchRead(makeList(bank, new PagedHashList<>()), new Random(seed), iterations)));
//        DLog.print(b.executeBench(150, "ArrayList read", makeBenchRead(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
    }

    @Ignore
//    @Test
    public void pagedListTest() {
        PagedList<Long> l = new PagedList<>();
        int size = 100000;
        int seed = 10;
        int iterations = 1000;
        List<Long> bank = this.getBank(size, 1337);
        DLog.print(b.executeBench(10, "PagedList Write", makeBenchWrite(makeList(bank, l), new Random(seed), iterations)));
        DLog.print("PageCount:" + l.getPageCount(), "PageSize:" + l.getPageSize());
        DLog.print(l.getPageRepresentation());

    }

    public <T> UncheckedRunnable makeBenchWrite(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        UncheckedRunnable run = () -> {

            Object ob = new Object();
            int bound = list.size();
            for (int i = 0; i < iterations; i++) {
                list.add(rnd.nextInt(list.size()), ob);
            }
            for (int i = 0; i < iterations; i++) {
                list.remove(rnd.nextInt(list.size()));
            }
        };
        return run;
    }
    Object ob = true;

    public void blackHole(Object o) {
        if (o == ob) {
            throw new IllegalStateException();
        }
    }

    public <T> UncheckedRunnable makeBenchRead(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        UncheckedRunnable run = () -> {
            int bound = list.size();
            for (int i = 0; i < iterations; i++) {
                Object get = list.get(rnd.nextInt(list.size()));
                blackHole(get);
            }
        };
        return run;
    }

    public <T> UncheckedRunnable makeBenchReadWrite(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        UncheckedRunnable run = () -> {
            Object ob = new Object();
            int bound = list.size();
            for (int i = 0; i < iterations; i++) {
                list.add(rnd.nextInt(list.size()), ob);
                for (int j = 0; j < iterations; j++) {
                    Object get = list.get(rnd.nextInt(list.size()));
                }
                list.remove(rnd.nextInt(list.size()));
            }
        };
        return run;
    }

    public <T> UncheckedRunnable makeBenchWriteRead(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        UncheckedRunnable run = () -> {
            Object ob = new Object();
            int bound = list.size();
            for (int i = 0; i < iterations; i++) {
                Object get = list.get(rnd.nextInt(bound));
                for (int j = 0; j < 100; j++) {
                    list.add(rnd.nextInt(list.size()), ob);

                }
                for (int j = 0; j < 100; j++) {
                    list.remove(rnd.nextInt(list.size()));

                }

            }
        };
        return run;
    }

    public <T> UncheckedRunnable makeBenchRandomWriteRead(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        UncheckedRunnable run = () -> {
            Object ob = new Object();
            int bound = list.size();
            for (int i = 0; i < iterations; i++) {
                Object get = list.get(rnd.nextInt(list.size()));
                if (rnd.nextBoolean()) {
                    list.add(rnd.nextInt(list.size()), ob);
                } else {
                    list.remove(rnd.nextInt(list.size()));
                }
            }
        };
        return run;
    }

    public void listEquals(List l1, List l2) {
        if (l1.size() != l2.size()) {
            throw new IllegalStateException("Size is not equal:" + l1.size() + " " + l2.size());
        }
        int size = l1.size();
        for (int i = 0; i < size; i++) {
            if (!Objects.equals(l1.get(i), l2.get(i))) {
                throw new IllegalStateException("Not equal at index " + i + " list1:" + l1.get(i) + " list2:" + l2.get(i));
            }
        }
        Checked.uncheckedRun(() -> {
            ListIterator listIterator = l1.listIterator();
            ListIterator listIterator1 = l2.listIterator();
            int i = 0;
            while (listIterator.hasNext() || listIterator1.hasNext()) {
                Object next = listIterator.next();
                Object next1 = listIterator1.next();
                if (!Objects.equals(next, next1)) {
                    throw new IllegalStateException("Not equal at index " + i + " list1:" + next + " list2:" + next1);
                }
            }
        });

        Checked.uncheckedRun(() -> {
            ListIterator listIterator = l1.listIterator(size);
            ListIterator listIterator1 = l2.listIterator(size);
            int i = 0;
            while (listIterator.hasPrevious()|| listIterator1.hasPrevious()) {
                Object next = listIterator.previous();
                Object next1 = listIterator1.previous();
                if (!Objects.equals(next, next1)) {
                    throw new IllegalStateException("Not equal at index " + i + " list1:" + next + " list2:" + next1);
                }
            }
        });
    }

    static interface ListOp {

        public void d(List list, int rngSeed, int size);
        public static ListOp add = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                long l = r.nextLong() % size;
                list.add(l);
            }
        };

        public static ListOp randomAdd = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                int l = r.nextInt(list.size());
                list.add(l, r.nextLong() % size);
            }
        };

        public static ListOp remove = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                list.remove(r.nextInt(list.size()));
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
                    bulkAdd.add(r.nextLong() % size);
                }
                list.addAll(bulkAdd);
            }
        };
        public static ListOp randomAddAll = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                int l = r.nextInt(size);
                ArrayList<Long> bulkAdd = new ArrayList<>();
                for (int j = 0; j < l; j++) {
                    i++;
                    bulkAdd.add(r.nextLong() % size);
                }
                list.addAll(r.nextInt(list.size()), bulkAdd);
            }
        };
    }

    public void listBehaviourTest(List<Long> toTest, List<Long> safeList, int rndSeed, int size) {
        toTest.clear();
        safeList.clear();
//        DLog.disable = true;
        ListOp.add.d(safeList, rndSeed, size);
        ListOp.add.d(toTest, rndSeed, size);

        this.listEquals(safeList, toTest);
//        DLog.print("List is valid after add");

        ListOp.remove.d(safeList, rndSeed, size);
        ListOp.remove.d(toTest, rndSeed, size);

        this.listEquals(safeList, toTest);
//        DLog.print("List is valid after remove");

        ListOp.add.d(safeList, rndSeed, size);
        ListOp.add.d(toTest, rndSeed, size);
        this.listEquals(safeList, toTest);
        ListOp.randomRemove.d(safeList, rndSeed, size / 2);
        ListOp.randomRemove.d(toTest, rndSeed, size / 2);

        this.listEquals(safeList, toTest);
//        DLog.print("List is valid after random remove");
        ListOp.addAll.d(safeList, rndSeed, size);
        ListOp.addAll.d(toTest, rndSeed, size);
        this.listEquals(safeList, toTest);

//        DLog.print("List is valid after add all");
        safeList.clear();
        toTest.clear();
        toTest.add(0L);
        safeList.add(0L);
        DLog.main().disable = false;

        ListOp.randomAddAll.d(safeList, rndSeed, size);
        ListOp.randomAddAll.d(toTest, rndSeed, size);
        this.listEquals(safeList, toTest);
//        DLog.print("List is valid after random add all");

        ListOp.randomAdd.d(safeList, rndSeed, size);
        ListOp.randomAdd.d(toTest, rndSeed, size);
        this.listEquals(safeList, toTest);

//        DLog.print("List is valid after random add");
//
//        DLog.print("Validation test passed");

        ListOp.randomRemove.d(safeList, rndSeed, size);
        ListOp.randomRemove.d(toTest, rndSeed, size);
//
        this.listEquals(safeList, toTest);
//        DLog.print("List is valid after random add");
    }
}
