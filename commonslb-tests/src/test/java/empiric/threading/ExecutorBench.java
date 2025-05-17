package empiric.threading;

import lt.lb.commons.threads.executors.BurstExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.misc.numbers.Atomic;
import lt.lb.commons.threads.executors.FastExecutor;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.executors.InPlaceExecutor;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class ExecutorBench {

    public static void main(String... args) throws Exception {

//        submitEmptyTasks(new FastExecutor(16), 500000, true);
        bench();
//    DLog.await(1, TimeUnit.MINUTES);
    }

    public static void bench() throws Exception {
        Benchmark bench = new Benchmark();
        bench.threads = 0;
        bench.warmupTimes = 5;

        int times = 50_000;
        int t = 8;
        int b = 20;

        bench.executeBench(b, "IN_PLACE", () -> {
            submitEmptyTasks(new InPlaceExecutor(), times);
        }).print(System.out::println);
//        bench.executeBench(b, "Checked", () -> {
//            submitEmptyTasks(Checked.createDefaultExecutorService(), times);
//        }).print(System.out::println);
        bench.executeBench(b, "Fast rework single", () -> {
            submitEmptyTasks(new FastExecutor(1), times);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework", () -> {
            submitEmptyTasks(new FastExecutor(t), times);
        }).print(System.out::println);
        
        bench.executeBench(b, "FastWaiting rework", () -> {
            submitEmptyTasks(new FastWaitingExecutor(t, WaitTime.ofMicros(10)), times);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework ArraySinchronizedArena", () -> {
            submitEmptyTasks(new FastExecutor(t,0), times);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework ArrayConcurrentArena", () -> {
            submitEmptyTasks(new FastExecutor(t,1), times);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework ArrayLockedArena", () -> {
            submitEmptyTasks(new FastExecutor(t,2), times);
        }).print(System.out::println);
         bench.executeBench(b, "Fast rework SplitConcurrentArena", () -> {
            submitEmptyTasks(new FastExecutor(t,3), times);
        }).print(System.out::println);
        bench.executeBench(b, "Burst", () -> {
            submitEmptyTasks(new BurstExecutor(t), times);
        }).print(System.out::println);
        bench.executeBench(b, "Regular single", () -> {
            submitEmptyTasks(Executors.newSingleThreadExecutor(), times);
        }).print(System.out::println);
        bench.executeBench(b, "Regular", () -> {
            submitEmptyTasks(Executors.newFixedThreadPool(t), times);
        }).print(System.out::println);

    }

    public static void submitEmptyTasks(ExecutorService pool, int times) throws Exception {
        submitEmptyTasks(pool, times, false);
    }

    public static void submitEmptyTasks(ExecutorService pool, int times, boolean debug) throws Exception {
        
        AtomicInteger ran = new AtomicInteger(0);
        int repeat = 5;
        for (int r = 0; r < repeat; r++) {
            List<Callable<Integer>> calls = new ArrayList<>(times);
            for (int i = 0; i < times; i++) {
                Callable<Integer> futureTask = () -> {
                    int inc = Atomic.incrementAndGet(ran);

                    if (debug) {
//                    System.out.println(inc);
                        DLog.print(inc);
                    }
                    return inc;
                };
                calls.add(futureTask);

            }
            pool.invokeAll(calls);
            if (debug) {
                DLog.print("Submitted");
            }
        }

        int expected = repeat * times;
        if (expected != ran.get()) {
            System.out.print("Times missmatch:" + expected + " " + ran.get());
        }
        assert times == ran.get();
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
    }

}
