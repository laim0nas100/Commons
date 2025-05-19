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
import lt.lb.commons.Java;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.misc.numbers.Atomic;
import lt.lb.commons.threads.executors.FastExecutor;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.executors.InPlaceExecutor;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.Checked;

/**
 *
 * @author laim0nas100
 */
public class ExecutorBench {

    public static void main(String... args) throws Exception {

        DLog.print(Java.getJavaVersionMajor());
//        submitEmptyTasks(new FastExecutor(16), 500000, true);
        bench();
//    DLog.await(1, TimeUnit.MINUTES);
    }

    public static void bench() throws Exception {
        Benchmark bench = new Benchmark();
        bench.threads = 0;
        bench.warmupTimes = 5;
        bench.useGChint = true;

        int times = 50_000;
        int t = 6;
        int b = 25;

        bench.executeBench(b, "IN_PLACE", () -> {
            submitEmptyTasks(new InPlaceExecutor(), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Regular single", () -> {
            submitEmptyTasks(Executors.newSingleThreadExecutor(), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Regular", () -> {
            submitEmptyTasks(Executors.newFixedThreadPool(t), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Checked", () -> {
            submitEmptyTasks(Checked.createDefaultExecutorService(), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework single", () -> {
            submitEmptyTasks(new FastExecutor(1), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework single ArrayLockedArena", () -> {
            submitEmptyTasks(FastExecutor._spec(2, 1), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework ConcurrentLinkedQueue", () -> {
            submitEmptyTasks(FastExecutor._spec(t, -1), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework LinkedBlockingQueue", () -> {
            submitEmptyTasks(FastExecutor._spec(t, 0), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "FastWaiting rework", () -> {
            submitEmptyTasks(new FastWaitingExecutor(t, WaitTime.ofMicros(10)), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework ArraySinchronizedArena", () -> {
            submitEmptyTasks(FastExecutor._spec(t, 1), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework ArrayLockedArena", () -> {
            submitEmptyTasks(FastExecutor._spec(t, 2), times, false);
        }).print(System.out::println);
        bench.executeBench(b, "Burst", () -> {
            submitEmptyTasks(new BurstExecutor(t), times, true);
        }).print(System.out::println);

    }

    public static void submitEmptyTasks(ExecutorService pool, int times, boolean bunch) throws Exception {
        submitEmptyTasks(pool, times, false, bunch);
    }

    public static int fib(int a) {
        return a <= 1 ? a : fib(a - 1) + fib(a - 2);
    }

    public static void submitEmptyTasks(ExecutorService pool, int times, boolean debug, boolean bunch) throws Exception {

        AtomicInteger ran = new AtomicInteger(0);
        int repeat = 5;
        for (int r = 0; r < repeat; r++) {
            List<Callable<Long>> calls = new ArrayList<>(times);
            for (int i = 0; i < times; i++) {
                final int f = i;
                Callable<Long> futureTask = () -> {
//                    Atomic.incrementAndGet(ran);
                    if (debug) {
//                    System.out.println(inc);
                        DLog.print(1);
                    }
                    return System.nanoTime() + System.currentTimeMillis() + fib(10);
                };
                calls.add(futureTask);

            }
            if (bunch) {
                pool.invokeAll(calls);
            } else {
                for (Callable<Long> call : calls) {
                    pool.submit(call);
                }
            }
            if (debug) {
                DLog.print("Submitted");
            }
        }

//        int expected = repeat * times;
//        if (expected != ran.get()) {
//            System.out.print("Times missmatch:" + expected + " " + ran.get());
//        }
//        assert times == ran.get();
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
    }

}
