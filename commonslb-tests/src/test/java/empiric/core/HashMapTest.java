/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.containers.collections.ImmutableLinearSet;
import lt.lb.commons.containers.collections.ImmutableLinearSetHashed;
import lt.lb.commons.iteration.streams.MakeStream;

/**
 *
 * @author laim0nas100
 */
public class HashMapTest {

    public static void main(String[] args) throws Exception {

        DLog.main().async = false;

        bench();
        DLog.await(1, TimeUnit.MINUTES);
    }
    private static long hashCounter;
    private static long linearHashCounter;
    private static long linearCounter;

    public static void bench() {
        Benchmark bench = new Benchmark();
//        List<Integer> counts = ImmutableCollections.listOf(2, 4, 8, 9, 10, 11, 12, 13, 14, 15, 16,20,25,29,32,64);
        List<Integer> counts = ImmutableCollections.listOf(16, 32, 64, 128, 256, 512);

        int repeat = 20000;
        Random rng = new Random(1337);
        for (int count : counts) {
            List<Object> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                list.add(rng.nextLong());
            }
            List shuffled = new ArrayList<>(list);
            Collections.shuffle(shuffled);
            ImmutableLinearSet linear = new ImmutableLinearSet<>(MakeStream.from(list).distinct().toArray());
            ImmutableLinearSetHashed linearHashed = new ImmutableLinearSetHashed<>(MakeStream.from(list).distinct().toArray());

            Set linked = MakeStream.from(list).distinct().toUnmodifiableLinkedSet();

//            bench.executeBench(repeat, "Hash Create " + count, () -> {
//                new LinkedHashSet<>(MakeStream.from(list).distinct().toList());
//            }).print(DLog::print);
//            bench.executeBench(repeat, "Linear Create " + count, () -> {
//                new ImmutableLinearSet<>(MakeStream.from(list).distinct().toArray());
//            }).print(DLog::print);
            bench.executeBench(repeat, "Hash contains all " + count, () -> {
                if (linked.containsAll(shuffled)) {
                    hashCounter++;
                }
            }).print(DLog::print);

            bench.executeBench(repeat, "Linear Hash contains all " + count, () -> {
                if (linearHashed.containsAll(shuffled)) {
                    linearHashCounter++;
                }
            }).print(DLog::print);

            bench.executeBench(repeat, "Linear contains all " + count, () -> {
                if (linear.containsAll(shuffled)) {
                    linearCounter++;
                }
            }).print(DLog::print);

            bench.executeBench(repeat, "Hash contains some " + count, () -> {
                for (int i = 0; i < count / 2; i++) {
                    if (linked.contains(shuffled.get(i))) {
                        hashCounter++;
                    }
                }
            }).print(DLog::print);

            bench.executeBench(repeat, "Linear hash contains some " + count, () -> {
                for (int i = 0; i < count / 2; i++) {
                    if (linearHashed.contains(shuffled.get(i))) {
                        linearHashCounter++;
                    }
                }
            }).print(DLog::print);

            bench.executeBench(repeat, "Linear contains some " + count, () -> {
                for (int i = 0; i < count / 2; i++) {
                    if (linear.contains(shuffled.get(i))) {
                        linearCounter++;
                    }
                }
            }).print(DLog::print);

        }
        DLog.print(linearCounter, linearHashCounter, hashCounter);
    }

    public static void test() {
        Map<Integer, String> map = ImmutableCollections.mapOf(1, "one", 2, "two", 3, "two");
        DLog.print(map.entrySet());
        DLog.print(map.get(1));
        DLog.print(map.keySet());
        Collection<String> values = map.values();
        DLog.print(values);
        DLog.print(map.values() == values);
    }
}
