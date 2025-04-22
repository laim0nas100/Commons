package empiric.threading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.threads.executors.FastExecutor;
import lt.lb.commons.threads.executors.FastExecutorOld;
import lt.lb.uncheckedutils.Checked;

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

        int times = 50_000;
        int t = 6;
        int b = 100;

        bench.executeBench(b, "IN_PLACE", () -> {
            submitEmptyTasks(new FastExecutorOld(0), times);
        }).print(System.out::println);
        bench.executeBench(b, "Checked", () -> {
            submitEmptyTasks(Checked.createDefaultExecutorService(), times);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework single", () -> {
            submitEmptyTasks(new FastExecutor(1), times);
        }).print(System.out::println);
        bench.executeBench(b, "Fast rework", () -> {
            submitEmptyTasks(new FastExecutor(t), times);
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
        List<Future> futures = new ArrayList<>(times);
        AtomicInteger ran = new AtomicInteger(0);
        for (int i = 0; i < times; i++) {
            futures.add(pool.submit(() -> {
                int inc = ran.incrementAndGet();
                
                if (debug) {
//                    System.out.println(inc);
                    DLog.print(inc);
                }

            }));
        }
        if (debug) {
            DLog.print("Submitted");
        }
        for (Future f : futures) {
            f.get();
        }
        if(times != ran.get()){
            System.out.print("Times missmatch:"+times+" "+ran.get());
        }
        assert times == ran.get();
        pool.shutdown();
    }

}
