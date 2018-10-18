/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.benchmarking;

/**
 *
 * @author laim0nas100
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
            String nameSuff = name;
            int left = 32 - name.length();
            while(left >0){
                name += "\t";
                left -=8;
            }
            return String.format(nameSuff + " Times(ms) Total(s): %.5f Min: %.5f Max: %.5f Avg: %.5f",totalTime / (mil * 1000),minTime / mil,maxTime / mil,averageTime / mil);
        }
    }
