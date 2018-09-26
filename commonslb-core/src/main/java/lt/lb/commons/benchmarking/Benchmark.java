/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.benchmarking;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import lt.lb.commons.threads.Promise;
import lt.lb.commons.threads.UnsafeRunnable;

/**
 *
 * @author laim0nas100
 */
public class Benchmark {

    public boolean useGChint = false;
    public boolean useGVhintAfterFullBench = true;
    public int threads = 8;

    public BenchmarkResult executeBench(Integer times, String name, Runnable... run) {
        ExecutorService serv = Executors.newFixedThreadPool(threads);
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
        serv.shutdownNow();
        return res;

    }

    public long execute(Executor exe, Runnable... run) {

        List<Promise> promises = new LinkedList<>();
        for (Runnable r : run) {
            promises.add(new Promise(UnsafeRunnable.from(r)));
        }
        Promise waiter = new Promise().waitFor(promises);

        long time = System.nanoTime();

        for (Promise p : promises) {
            exe.execute(p);
        }
        exe.execute(waiter);
        try {
            waiter.get();
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        return System.nanoTime() - time;
    }
}
