package empiric.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.datafill.NumberFill;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.iteration.Iter.IterNoStop;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.impl.ArrayROI;
import lt.lb.commons.misc.rng.FastRandom;

/**
 *
 * @author laim0nas100
 */
public class IteratorTest {

    public static void main(String[] args) throws Exception {
        DLog.main().disable = true;
        testBench();
        DLog.main().disable = false;
        testBench();
        DLog.await(1, TimeUnit.MINUTES);
    }

//    @Test
    public static void test() throws Exception {
        Integer[] arr = ArrayOp.asArray(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        List<Integer> asList = Arrays.asList(arr);
        DLog.main().stackTrace = false;
        IterNoStop it = (i, item) -> {

            if (i == 0) {
                DLog.print("####");
            }
            DLog.print(i, "Item=" + item);

        };

        DLog.print("as List from 10");
        Iter.iterate(asList, 10, 14, it);

        Iter.iterate(asList, it);

        Iter.iterate(ReadOnlyIterator.of(asList), it);

        Iter.iterate(asList.stream(), it);

        Iter.iterate(arr, it);

        Iter.iterate(ReadOnlyIterator.of(arr), it);

//        DLog.print(() -> ReflectionUtils.reflectionString(DLog.main(), 2));
        DLog.await(1, TimeUnit.MINUTES);
    }

//    @Test
    public static void testBench() throws Exception {
        FastRandom rng = new FastRandom();
        ArrayList<Integer> asList = NumberFill.fillArrayList(200000, () -> rng.nextInt(10000));
        Integer[] arr = asList.stream().toArray(t -> new Integer[t]);
        Collection<Integer> col = asList;
        Benchmark bench = new Benchmark();
        bench.threads = 1;

        DLog.main().stackTrace = false;
        Iter it1 = (i, item) -> {
            return (i == 755555);
        };
        IterIterableBiCons it2 = (i, item) -> {
            return (i == 755555);
        };
        int times = 250;

        bench.executeBench(times, "Iter List      ", () -> {
            Iter.find(asList, it1);
        }).print(DLog::print);

        bench.executeBench(times, "Iter Collection", () -> {
            Iter.find(col, it1);
        }).print(DLog::print);
        bench.executeBench(times, "Iter ROI list", () -> {
            Iter.find(ReadOnlyIterator.of(asList), it1);
        }).print(DLog::print);
        bench.executeBench(times, "Iter ROI stream       ", () -> {
            Iter.find(ReadOnlyIterator.of(asList.stream()), it1);
        }).print(DLog::print);

        Iter.find(asList.stream().iterator(), it1);
        bench.executeBench(times, "Iter Stream iter       ", () -> {
            Iter.find(asList.stream().iterator(), it1);
        }).print(DLog::print);

        bench.executeBench(times, "pure stream        ", () -> {
            IntegerValue val = new IntegerValue(0);
            asList.stream().forEach(f -> it1.visit(val.getAndIncrement(), f));
        }).print(DLog::print);
        bench.executeBench(times, "Iter ROI array", () -> {
            Iter.find(new ArrayROI<>(arr), it1);
        }).print(DLog::print);

        bench.executeBench(times, "Stream array         ", () -> {
            IntegerValue val = new IntegerValue(0);
            Stream.of(arr).forEach(f -> it1.visit(val.getAndIncrement(), f));
        }).print(DLog::print);
        bench.executeBench(times, "Iter Array   ", () -> {
            Iter.find(arr, it1);
        }).print(DLog::print);
        bench.executeBench(times, "For List        ", () -> {
            int i = 0;
            for (Integer value : asList) {
                if (it1.visit(i++, value)) {
                    break;
                }
            }
        }).print(DLog::print);
        
        //NEW 
        
        
        bench.executeBench(times, "New For List        ", () -> {
            For.elements().find(asList, it2);
        }).print(DLog::print);

        bench.executeBench(times, "New For Collection   ", () -> {
            For.elements().find(col, it2);
        }).print(DLog::print);
        bench.executeBench(times, "New For ROI list      ", () -> {
            For.elements().find(ReadOnlyIterator.of(asList), it2);
        }).print(DLog::print);
        bench.executeBench(times, "New For ROI stream   ", () -> {
            For.elements().find(ReadOnlyIterator.of(asList.stream()), it2);
        }).print(DLog::print);

        For.elements().find(asList.stream().iterator(), it2);
        bench.executeBench(times, "New For Stream iter   ", () -> {
            For.elements().find(asList.stream().iterator(), it2);
        }).print(DLog::print);

        bench.executeBench(times, "New For ROI array      ", () -> {
            For.elements().find(new ArrayROI<>(arr), it2);
        }).print(DLog::print);
 
        bench.executeBench(times, "New For Array       ", () -> {
            For.elements().find(arr, it2);
        }).print(DLog::print);
        

    }

}
