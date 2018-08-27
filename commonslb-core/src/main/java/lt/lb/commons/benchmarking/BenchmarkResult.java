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
public class BenchmarkResult {

        public Long totalTime = 0L;
        public Long timesRan = 0L;
        public Double averageTime = null;
        public Long maxTime = null;
        public Long minTime = null;
        public String name = "Benchmark";

        @Override
        public String toString() {
            double mil = 1000000;
            return name + " Times(ms) Total(s):" + totalTime / (mil * 1000) + " Min:" + minTime / mil + " Max:" + maxTime / mil + " Avg:" + averageTime / mil;
        }
    }
