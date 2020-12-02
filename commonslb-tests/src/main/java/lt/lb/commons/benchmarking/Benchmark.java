package lt.lb.commons.benchmarking;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.misc.Timer;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.threads.Promise;

/**
 *
 * @author laim0nas100
 */
public class Benchmark {

    public boolean useGChint = false;
    public boolean useGVhintAfterFullBench = true;
    public int threads = 8;
    public int warmupTimes = 5;

    public BenchmarkResult executeBenchParallel(Integer times, String name, UnsafeRunnable run) {

        Executor serv;
        if (threads > 1) {
            serv = Executors.newFixedThreadPool(threads);
        } else {
            serv = r -> r.run();
        }
        for (int i = 0; i < warmupTimes; i++) {
            execute(serv, run);
        }

        BenchmarkResult res = new BenchmarkResult();
        res.name = name;
        if (useGVhintAfterFullBench) {
            System.gc();
        }
        for (int i = 0; i < times; i++) {
            if (useGChint) {
                System.gc();
            }
            long time = execute(serv, ArrayOp.replicate(threads, run));
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
        if (serv instanceof ExecutorService) {
            ExecutorService cast = F.cast(serv);
            cast.shutdownNow();
        }
        return res;

    }

    public BenchmarkResult executeBench(Integer times, String name, UnsafeRunnable... run) {

        Executor serv;
        if (threads > 1) {
            serv = Executors.newFixedThreadPool(threads);
        } else {
            serv = r -> r.run();
        }
        for (int i = 0; i < warmupTimes; i++) {
            execute(serv, run);
        }

        BenchmarkResult res = new BenchmarkResult();
        res.name = name;
        if (useGVhintAfterFullBench) {
            System.gc();
        }
        for (int i = 0; i < times; i++) {
            if (useGChint) {
                System.gc();
            }
            long time = execute(serv, run);
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
        if (serv instanceof ExecutorService) {
            ExecutorService cast = F.cast(serv);
            cast.shutdownNow();
        }
        return res;

    }

    public long execute(Executor exe, UnsafeRunnable... run) {

        List<Promise> promises = new LinkedList<>();
        for (UnsafeRunnable r : run) {
            promises.add(new Promise(r));
        }
        Promise waiter = new Promise().waitFor(promises);

        Timer t = new Timer();

        for (Promise p : promises) {
            exe.execute(p);
        }
        exe.execute(waiter);
        try {
            waiter.get();
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        return t.stopNanos();
    }
}
