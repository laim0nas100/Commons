/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.threading;

import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.Log;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.LongValue;
import lt.lb.commons.threads.sync.ThreadBottleneck;
import org.junit.Test;

/**
 *
 * @author Lemmin
 */
public class ThreadBottleneckTest {

    public static interface Incrementer {

        public long increment();
    }

    public static Runnable makeRun(Incrementer inc, int times) {
        return () -> {
            for (int i = 0; i < times; i++) {
                inc.increment();
            }
        };
    }

    public static Incrementer inc1() {
        return new Incrementer() {
            long val = 0L;

            @Override
            public synchronized long increment() {
                return ++val;
            }
        };
    }

    public static Incrementer inc2() {
        return new Incrementer() {
            ThreadBottleneck bottle = new ThreadBottleneck(1);
            LongValue val = new LongValue(0L);

            @Override
            public long increment() {
                bottle.execute(() -> {
                    val.incrementAndGet();
                });
                return val.get();
            }
        };
    }

    public static Incrementer inc3() {
        return new Incrementer() {
            AtomicLong val = new AtomicLong(0L);

            @Override
            public long increment() {
                return val.incrementAndGet();
            }
        };
    }

    public void bench() {

        Benchmark bench = new Benchmark();

        bench.threads = 4;

        for (int i = 0; i < 4; i++) {
            bench.executeBench(100, "first", makeRun(inc1(), 1000000)).print(Log::print);
            bench.executeBench(100, "second", makeRun(inc2(), 1000000)).print(Log::print);
            bench.executeBench(100, "third", makeRun(inc3(), 1000000)).print(Log::print);
        }

//        Log.println("",inc1.increment(),inc2.increment(),inc3.increment());
        Log.close();
    }

}
