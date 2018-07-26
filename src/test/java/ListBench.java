
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lt.lb.commons.Containers.*;
import lt.lb.commons.Log;
import org.junit.Ignore;
import org.junit.Test;
import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.collections.GapList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Lemmin
 */
public class ListBench {

    static {
        Log.instant = true;
    }

    public static class BenchResult {

        public Long totalTime = 0L;
        public Long timesRan = 0L;
        public Double averageTime = null;
        public Long maxTime = null;
        public Long minTime = null;
        public String name = "Bench";

        @Override
        public String toString() {
            double mil = 1000000;
            return name + " Times(ms) Total(s):" + totalTime / (mil * 1000) + " Min:" + minTime / mil + " Max:" + maxTime / mil + " Avg:" + averageTime / mil;
        }
    }

    private void dPrintList(PagedHashList l) {
        Log.print(l.toString());
        Log.printLines(l.getMappings());
    }
//    @Ignore

    @Test
    public void testPrefillHash() {
        PrefillArrayMap<Long> map = new PrefillArrayMap<Long>();
        for (int i = 0; i < 10; i++) {
            map.put(i, 10L - i);
        }
        map.remove(5);
        map.remove(9);
        Log.printLines(map.entrySet());
        Log.print(map);
    }

    @Ignore
    @Test
    public void test() throws InterruptedException {
        PagedHashList<Long> list = new PagedHashList<>();
        for (int i = 0; i < 10; i++) {
//            dPrintList(list);
            list.add((long) i * 2);

        }

        for (int i = 0; i < 10; i++) {
            list.set(i, i * -1L);
        }
//        Log.printLines(list.getMappings());
//        Log.print(list.remove(9)+" ");
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
//            Log.print("remove:"+toRemove,list.remove(toRemove)+" ");
//        dPrintList(list);
//        }
//        int size = 50000;
//        int iterations = 500000;
//        Log.print(executeBench(80, "ArrayList read", makeBenchRead(makeList(this.getBank(size, size), new ArrayList<>()), new Random(10), iterations)));
//        Log.print(executeBench(80, "BigList read", makeBenchRead(makeList(this.getBank(size, size), new BigList<>()), new Random(10), iterations)));
//        Log.print(executeBench(80, "ArrayList read", makeBenchRead(makeList(this.getBank(size, size), new ArrayList<>()), new Random(10), iterations)));
//        Log.print(executeBench(80, "PagedHashList read", makeBenchRead(makeList(this.getBank(size, size), new PagedHashList<>()), new Random(10), iterations)));
//        ListIterator<Long> listIterator = list.listIterator();
//        while (listIterator.hasNext()) {
//            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
//        }
//        listIterator.remove();
//        Log.print(list.toString());
//        Log.println();
//        while (listIterator.hasPrevious()) {
//            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
//        }
//        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex());
//        listIterator.add(13L);
//        Log.print(list.toString());
//        Log.println();
//        for (int i = 0; i < 10; i++) {
//            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
//            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
//        }
//        Log.println();
//        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
////        listIterator.next();
//        listIterator.add(20L);
//        Log.print(list.toString());
//        list.remove(5);
//        Log.print(list.toString());
//        list.add(7, 25L);
//
//        Log.print(list.toString());
//        //TODO
//        list.add(3, 35L);
//
//        Log.print(list.toString());
        Log.await(1, TimeUnit.HOURS);

    }

    public List<Long> getBank(int size, int seed) {
        ArrayList<Long> bank = new ArrayList<>(size);
        Random r = new Random(seed);
        r.longs(size).forEach(a -> {
            bank.add(a);
        });
        return bank;
    }

    public BenchResult executeBench(int times, String name, Runnable run) {
        BenchResult res = new BenchResult();
        res.name = name;
        for (int i = 0; i < times; i++) {
            System.gc();
            long time = execute(run);
            res.timesRan++;
            if (res.maxTime == null) {
                res.maxTime = time;
                res.minTime = time;
            } else {
                if (res.maxTime < time) {
                    res.maxTime = time;
                }
                if (res.minTime > time) {
                    res.minTime = time;
                }
            }
            res.totalTime += time;

        }
        res.averageTime = (double) res.totalTime / res.timesRan;
        return res;

    }

    public long execute(Runnable run) {
        long time = System.nanoTime();
        run.run();
        return System.nanoTime() - time;
    }

    public <T> Supplier<List<T>> makeList(List<T> bank, List<T> newList) {

        return () -> {
            newList.addAll(bank);
            return newList;
        };
    }

    public void benchBatch(int size, int iterations, int seed, Class<? extends List>... lists) throws InstantiationException, IllegalAccessException {

        List<Long> bank = this.getBank(size, 1337);
        Log.print("Size:", size);
        int runCount = 10;

        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName() + " write", makeBenchWrite(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName() + " read", makeBenchRead(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName() + " read>>write", makeBenchReadWrite(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName() + " read<<write", makeBenchWriteRead(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName() + " random read write", makeBenchRandomWriteRead(makeList(bank, newInstance), new Random(seed), iterations)));
        }

//        Log.print(executeBench(10, "ArrayList write", makeBenchWrite(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "PagedList write", makeBenchWrite(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "GapList write", makeBenchWrite(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "BigList write", makeBenchWrite(makeList(bank, new BigList<>()), new Random(seed), iterations)));
////        Log.print();
//        Log.print(executeBench(10, "ArrayList read", makeBenchRead(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "PagedList read", makeBenchRead(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "GapList read", makeBenchRead(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "BigList read", makeBenchRead(makeList(bank, new BigList<>()), new Random(seed), iterations)));
////        Log.print();
//        Log.print(executeBench(10, "ArrayList readWrite", makeBenchReadWrite(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "PagedList readWrite", makeBenchReadWrite(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "GapList readWrite", makeBenchReadWrite(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "BigList readWrite", makeBenchReadWrite(makeList(bank, new BigList<>()), new Random(seed), iterations)));
////        Log.print();
//        Log.print(executeBench(10, "ArrayList writeRead", makeBenchWriteRead(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "PagedList writeRead", makeBenchWriteRead(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "GapList writeRead", makeBenchWriteRead(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "BigList writeRead", makeBenchWriteRead(makeList(bank, new BigList<>()), new Random(seed), iterations)));
////        Log.print();
//        Log.print(executeBench(10, "ArrayList randomWriteRead", makeBenchRandomWriteRead(makeList(bank, new ArrayList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "PagedList randomWriteRead", makeBenchRandomWriteRead(makeList(bank, new PagedList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "GapList randomWriteRead", makeBenchRandomWriteRead(makeList(bank, new GapList<>()), new Random(seed), iterations)));
//        Log.print(executeBench(10, "BigList randomWriteRead", makeBenchRandomWriteRead(makeList(bank, new BigList<>()), new Random(seed), iterations)));
//        Log.print();
    }

    public static void main(String[] a) throws Exception {
        Log.instant = true;
        Log.keepBuffer = false;
//        Log.disable = true;
        new ListBench().listBench();
//        PagedHashList list = new PagedHashList<>();
//        new ListBench().listBehaviourTest(list, new ArrayList<>(), 1373, 800);
//
//        Log.printLines(list.getMappings());
//        Log.print(list.getPageCount(), list.getPageSize(), list.getAveragePageSize());
    }

    @Ignore
    @Test
    public void listBench() throws Exception {
        Log.print("List benchmark");
        int size = 8000;
        int iterations = 10000;
        int seed = 10;
//        Class<List>[] lists = new Class[]{ArrayList.class,GapList.class,PagedList.class,BigList.class};
        Class<List>[] lists = new Class[]{PagedHashList.class, BigList.class};
//        Class<List>[] lists = new Class[]{BigList.class};
        Log.print("Waiting for input");
        Log.print("Start " + System.in.read());
//        benchBatch(size / 10, iterations, seed,lists);
//        benchBatch(size, iterations, seed,lists);
//        benchBatch(size * 10, iterations, seed,lists);
//        benchBatch(size * 100, iterations, seed,lists);
//        benchBatch(size * 1000, iterations, seed, lists);
//        benchBatch(size*5000, iterations, seed, lists);
//        benchBatch(size*30000, iterations, seed, lists);

    
int mult = 1000;
        Log.print(executeBench(100, "ArrayList read", makeBenchRead(makeList(this.getBank(size*mult, seed), new ArrayList<>()), new Random(seed), iterations)));
        Log.print(executeBench(100, "BigList read", makeBenchRead(makeList(this.getBank(size*mult, seed), new BigList<>(100)), new Random(seed), iterations)));
        Log.print(executeBench(100, "PagedHashedList read", makeBenchRead(makeList(this.getBank(size*mult, seed), new PagedHashList<>()), new Random(seed), iterations)));

    }

    @Ignore
    @Test
    public void pagedListTest() {
        PagedList<Long> l = new PagedList<>();
        int size = 100000;
        int seed = 10;
        int iterations = 1000;
        List<Long> bank = this.getBank(size, 1337);
        Log.print(executeBench(10, "PagedList Write", makeBenchWrite(makeList(bank, l), new Random(seed), iterations)));
        Log.print("PageCount:" + l.getPageCount(), "PageSize:" + l.getPageSize());
        Log.print(l.getPageRepresentation());

    }

    public <T> Runnable makeBenchWrite(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        Runnable run = () -> {

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

    public <T> Runnable makeBenchRead(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        Runnable run = () -> {
            Object ob = new Object();
            int bound = list.size();
            for (int i = 0; i < iterations; i++) {
                Object get = list.get(rnd.nextInt(list.size()));
            }
        };
        return run;
    }

    public <T> Runnable makeBenchReadWrite(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        Runnable run = () -> {
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

    public <T> Runnable makeBenchWriteRead(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        Runnable run = () -> {
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

    public <T> Runnable makeBenchRandomWriteRead(Supplier<List<T>> sup, Random rnd, int iterations) {
        List list = sup.get();
        Runnable run = () -> {
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
            throw new IllegalStateException("Size is not equal");
        }
        int size = l1.size();
        for (int i = 0; i < size; i++) {
            if (!Objects.equals(l1.get(i), l2.get(i))) {
                throw new IllegalStateException("Not equal at index " + i + " list1:" + l1.get(i) + " list2:" + l2.get(i));
            }
        }
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
                for(int j = 0; j < l ; j++){
                    i++;
                    bulkAdd.add(r.nextLong()% size);
                }
                list.addAll(bulkAdd);
            }
        };
        public static ListOp randomAddAll = (List list, int rngSeed, int size) -> {
            Random r = new Random(rngSeed);
            for (int i = 0; i < size; i++) {
                int l = r.nextInt(size);
                ArrayList<Long> bulkAdd = new ArrayList<>();
                for(int j = 0; j < l ; j++){
                    i++;
                    bulkAdd.add(r.nextLong()% size);
                }
                list.addAll(r.nextInt(list.size()),bulkAdd);
            }
        };
    }

    public void listBehaviourTest(List<Long> toTest, List<Long> safeList, int rndSeed, int size) {
        toTest.clear();
        safeList.clear();
//        Log.disable = true;
        ListOp.add.d(safeList, rndSeed, size);
        ListOp.add.d(toTest, rndSeed, size);

        this.listEquals(safeList, toTest);
        Log.print("List is valid after add");

        ListOp.remove.d(safeList, rndSeed, size);
        ListOp.remove.d(toTest, rndSeed, size);

        this.listEquals(safeList, toTest);
        Log.print("List is valid after remove");

        ListOp.add.d(safeList, rndSeed, size);
        ListOp.add.d(toTest, rndSeed, size);
        this.listEquals(safeList, toTest);
        ListOp.randomRemove.d(safeList, rndSeed, size / 2);
        ListOp.randomRemove.d(toTest, rndSeed, size / 2);

        this.listEquals(safeList, toTest);
        Log.print("List is valid after random remove");

        ListOp.randomAdd.d(safeList, rndSeed, size);
        ListOp.randomAdd.d(toTest, rndSeed, size);
        this.listEquals(safeList, toTest);
        
        Log.print("List is valid after random add");
        
        ListOp.addAll.d(safeList, rndSeed, size);
        ListOp.addAll.d(toTest, rndSeed, size);
        this.listEquals(safeList, toTest);
        
        Log.print("List is valid after add all");
        safeList.clear();
        toTest.clear();
        toTest.add(0L);
        safeList.add(0L);
        Log.disable = false;
        
        ListOp.randomAddAll.d(safeList, rndSeed, size);
        ListOp.randomAddAll.d(toTest, rndSeed, size);
        this.listEquals(safeList, toTest);
        
        Log.print("List is valid after random add all");

        Log.print("Validation test passed");
        

//        ListOp.randomRemove.d(safeList, rndSeed, size);
//        ListOp.randomRemove.d(toTest, rndSeed, size);
//
//        this.listEquals(safeList, toTest);
//        Log.print("List is valid after random add");
    }
}
