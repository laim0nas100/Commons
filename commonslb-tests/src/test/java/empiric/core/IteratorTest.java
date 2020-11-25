package empiric.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.misc.ReflectionUtils;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.datafill.NumberFill;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.iteration.Iter.IterNoStop;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.impl.ArrayROI;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.reflect.ReflectionPrint;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class IteratorTest {

//    @Test
    public void test() throws Exception {
        Integer[] arr = ArrayOp.asArray(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        List<Integer> asList = Arrays.asList(arr);
        Log.main().stackTrace = false;
        IterNoStop it = (i, item) -> {

            if (i == 0) {
                Log.print("####");
            }
            Log.print(i, "Item=" + item);

        };

        Log.print("as List from 10");
        Iter.iterate(asList, 10, 14, it);

        Iter.iterate(asList, it);

        Iter.iterate(ReadOnlyIterator.of(asList), it);

        Iter.iterate(asList.stream(), it);

        Iter.iterate(arr, it);

        Iter.iterate(ReadOnlyIterator.of(arr), it);

//        Log.print(() -> ReflectionUtils.reflectionString(Log.main(), 2));
        Log.await(1, TimeUnit.HOURS);
    }

//    @Test
    public void testBench() throws Exception {
        FastRandom rng = new FastRandom();
        ArrayList<Integer> asList = NumberFill.fillArrayList(855555, () -> rng.nextInt());
        Integer[] arr = asList.stream().toArray(t -> new Integer[t]);
        Collection<Integer> col = asList;
        Benchmark bench = new Benchmark();
        bench.threads = 1;

        Log.main().stackTrace = false;
        Iter it = (i, item) -> {
            return (i == 755555);
        };
        int times = 100;

        bench.executeBench(times, "List", () -> {
            Iter.find(asList, it);
        }).print(Log::print);

        bench.executeBench(times, "Collection", () -> {
            Iter.find(col, it);
        }).print(Log::print);
        bench.executeBench(times, "ROI list", () -> {
            Iter.find(ReadOnlyIterator.of(asList), it);
        }).print(Log::print);
        bench.executeBench(times, "ROI stream ", () -> {
            Iter.find(asList.stream(), it);
        }).print(Log::print);
        
        
        Iter.find(asList.stream().iterator(), it);
        bench.executeBench(times, "Stream iter", () -> {
            Iter.find(asList.stream().iterator(), it);
        }).print(Log::print);

        bench.executeBench(times, "pure stream ", () -> {
            IntegerValue val = new IntegerValue(0);
            asList.stream().filter(f -> it.visit(val.getAndIncrement(), f)).findFirst();
        }).print(Log::print);
        bench.executeBench(times, "ROI array", () -> {
            Iter.find(new ArrayROI<>(arr), it);
        }).print(Log::print);
        
        
        
        bench.executeBench(times, "Stream array", () -> {
            IntegerValue val = new IntegerValue(0);
            Stream.of(arr).filter(f -> it.visit(val.getAndIncrement(), f)).findFirst();
        }).print(Log::print);
        bench.executeBench(times, "Array   ", () -> {
            Iter.find(arr, it);
        }).print(Log::print);
        bench.executeBench(times, "For List", () -> {
            int i = 0;
            for (Integer value : asList) {
                if (it.visit(i++, value)) {
                    break;
                }
            }
        }).print(Log::print);

//        Log.print(() -> ReflectionUtils.reflectionString(Log.main(), 2));
        Log.await(1, TimeUnit.HOURS);
    }

}
