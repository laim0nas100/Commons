
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lt.lb.commons.Containers.PagedList;
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

    public void test() throws InterruptedException {
        PagedList<Long> list = new PagedList<>();
        for (int i = 0; i < 10; i++) {
            list.add((long) i * 2);
        }
        Log.print(list.toString());

        ListIterator<Long> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
        }
        listIterator.remove();
        Log.print(list.toString());
        Log.println();
        while (listIterator.hasPrevious()) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex());
        listIterator.add(13L);
        Log.print(list.toString());
        Log.println();
        for (int i = 0; i < 10; i++) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        Log.println();
        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
//        listIterator.next();
        listIterator.add(20L);
        Log.print(list.toString());
        list.remove(5);
        Log.print(list.toString());
        list.add(7, 25L);

        Log.print(list.toString());
        //TODO
        list.add(3, 35L);

        Log.print(list.toString());
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
            for (T t : bank) {
                newList.add(t);
            }
            return newList;
        };
    }

    public void benchBatch(int size, int iterations, int seed, Class<? extends List>... lists) throws InstantiationException, IllegalAccessException {

        List<Long> bank = this.getBank(size, 1337);
        Log.print("Size:", size);
        int runCount = 10;

        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName()+" write", makeBenchWrite(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName()+" read", makeBenchRead(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName()+" read>>write", makeBenchReadWrite(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName()+" read<<write", makeBenchWriteRead(makeList(bank, newInstance), new Random(seed), iterations)));
        }
        for (Class<? extends List> list : lists) {
            List newInstance = list.newInstance();
            Log.print(executeBench(runCount, list.getSimpleName()+" random read write", makeBenchRandomWriteRead(makeList(bank, newInstance), new Random(seed), iterations)));
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

    public static void main(String [] a) throws Exception{
        new ListBench().listBench();
    }
    
    @Test
    public void listBench() throws Exception{
        Log.print("List benchmark");
        int size = 1000;
        int iterations = 200;
        int seed = 10;
//        Class<List>[] lists = new Class[]{ArrayList.class,GapList.class,PagedList.class,BigList.class};
        Class<List>[] lists = new Class[]{PagedList.class,BigList.class};
//        Class<List>[] lists = new Class[]{BigList.class};
        Log.print("Waiting for input");
        Log.print("Start "+System.in.read());
//        benchBatch(size / 10, iterations, seed,lists);
//        benchBatch(size, iterations, seed,lists);
//        benchBatch(size * 10, iterations, seed,lists);
//        benchBatch(size * 100, iterations, seed,lists);
        benchBatch(size * 10000, iterations, seed,lists);
//        benchBatch(size*5000, iterations, seed, lists);
//        benchBatch(size*30000, iterations, seed, lists);

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
}
