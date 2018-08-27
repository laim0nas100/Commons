/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.benchmarking;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class Benchmark {

    public boolean useGChint = true;
    public boolean useGVhintAfterFullBench = true;

    public BenchmarkResult executeBench(Integer times, String name, Runnable run) {
        BenchmarkResult res = new BenchmarkResult();
        res.name = name;
        if(useGVhintAfterFullBench){
            System.gc();
        }
        for (int i = 0; i < times; i++) {
            if (useGChint) {
                System.gc();
            }
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
}
