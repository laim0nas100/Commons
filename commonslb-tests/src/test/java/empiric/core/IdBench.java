/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.Log;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.fastid.FastID;
import lt.lb.fastid.FastIDGen;

/**
 *
 * @author Lemmin
 */
public class IdBench {
    
    public static long id;
    public static void main(String...args){
        
        FastIDGen gen = new FastIDGen();
        AtomicLong atomic = new AtomicLong(0L);
        
        Log.main().async = false;
        int times = 500000;
        Benchmark bench = new Benchmark();
        bench.threads = 20;
        
        bench.executeBenchParallel(times, "sync", ()-> getInc()).print(Log::print);
        bench.executeBenchParallel(times, "FastID", ()-> FastID.getAndIncrementGlobal()).print(Log::print);
        bench.executeBenchParallel(times, "atomic", ()-> atomic.getAndIncrement()).print(Log::print);
        bench.executeBenchParallel(times, "sync", ()-> getInc()).print(Log::print);
        bench.executeBenchParallel(times, "FastID", ()-> FastID.getAndIncrementGlobal()).print(Log::print);
        bench.executeBenchParallel(times, "atomic", ()-> atomic.getAndIncrement()).print(Log::print);
        
        
        
    }
    
    
    public static synchronized long getInc(){
        return id++;
    }
    
    
    
    
}
