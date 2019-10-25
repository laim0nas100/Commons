/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import empiric.core.StackOverflowTest.RecursionBuilder;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.Log;
import lt.lb.commons.benchmarking.Benchmark;

/**
 *
 * @author laim0nas100
 */
public class CallerBenchmark {

    public static void main(String... args) {
        Benchmark bench = new Benchmark();
        int times = 100;
        bench.threads = 1;
        bench.warmupTimes = 2;
//        bench.useGVhintAfterFullBench = true;

        BigInteger n = BigInteger.valueOf(5);
        BigInteger m = BigInteger.valueOf(3);
        BigInteger limit = BigInteger.valueOf(99999999999999L);
        long fibb2 = 25;

        for (int i = 0; i < 5; i++) {
//            bench.executeBench(times, "Caller acker", () -> {
//                RecursionBuilder.ackermannCaller(m, n).resolve();
//            }).print(Log::print);
//            bench.executeBench(times, "Caller fibb1", () -> {
//                RecursionBuilder.fibbCaller(m, n, limit).resolve();
//            }).print(Log::print);
//            bench.executeBench(times, "Caller fibb2", () -> {
//                RecursionBuilder.fibb2Caller(fibb2).resolve();
//            }).print(Log::print);
            bench.executeBench(times, "Caller boi", ()->{
                RecursionBuilder.recBoiCaller(7,new AtomicLong()).resolveThreaded();
            }).print(Log::print);
            
//            bench.executeBench(times, "acker", () -> {
//                RecursionBuilder.ackermann(m, n);
//            }).print(Log::print);
//            bench.executeBench(times, "fibb", () -> {
//                RecursionBuilder.fibb(m, n, limit);
//            }).print(Log::print);
//
//            bench.executeBench(times, "fibb2", () -> {
//                RecursionBuilder.fibb2(fibb2);
//            }).print(Log::print);
            
            bench.executeBench(times, "boi", ()->{
                RecursionBuilder.recBoi(7,new AtomicLong());
            }).print(Log::print);
        }
        Log.close();
    }
}
