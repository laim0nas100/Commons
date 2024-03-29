package lt.lb.commons.benchmarking;

import java.util.function.Consumer;
import java.util.function.Function;

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

    private static final double MILL = 1000000;
    @Override
    public String toString() {
        return String.format(" Times(ms) Avg: %.5f Min: %.5f Max: %.5f Total(s): %.5f", averageTime / MILL, minTime / MILL, maxTime / MILL, totalTime / (MILL * 1000))+"\t"+name;
    }

    public BenchmarkResult print(Consumer<String> printer) {
        printer.accept(this.toString());
        return this;
    }
    
    public <U> U chain(Function<BenchmarkResult, ? extends U> consumer){
        return consumer.apply(this);
    }

    public BenchmarkResult merge(BenchmarkResult other) {
        BenchmarkResult res = new BenchmarkResult();
        res.totalTime = totalTime + other.totalTime;
        res.timesRan = timesRan + other.timesRan;
        res.name = name;
        res.maxTime = Math.max(maxTime, other.maxTime);
        res.minTime = Math.min(minTime, other.minTime);
        res.averageTime = (double) res.totalTime / res.timesRan;

        return res;
    }
}
