package empiric.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.PrefillArrayList;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.values.NumberValue;
import lt.lb.commons.containers.collections.PrefillArrayMap;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.misc.rng.FastRandom;
import org.junit.Test;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author laim0nas100
 */
public class CollectionTest {
    
    static {
        DLog.main().async = true;
    }
    
    public static void print(Object... args) {
        String s = "";
        for (Object o : args) {
            s += o + " ";
        }
        System.out.println(s);
    }
    
    public void test() throws InterruptedException, TimeoutException {
        PrefillArrayList<Long> list = new PrefillArrayList<>(0L);
        for (int i = 0; i < 10; i++) {
            list.put(i, (long) i * 2);
        }
        DLog.print(list.toString());
        
        ListIterator<Long> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
        }
        listIterator.remove();
        DLog.print(list.toString());
        DLog.println();
        while (listIterator.hasPrevious()) {
            DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex());
        listIterator.add(13L);
        DLog.print(list.toString());
        DLog.println();
        for (int i = 0; i < 10; i++) {
            DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
            DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        DLog.println();
        DLog.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
        
        listIterator.set(20L);
        DLog.print(list.toString());
        DLog.await(1, TimeUnit.HOURS);
        
    }
    
    public UncheckedRunnable makeRun(Map<Integer, String> map, Random r, int times) {
        return () -> {
            map.put(0, r.nextInt() + "");
            for (int i = 0; i < times; i++) {
                int key = r.nextInt(5000) - r.nextInt(5000);
                String val = r.nextInt() + "";
                if (r.nextBoolean()) {
                    map.put(key, val);
                } else {
                    map.remove(key);
                }
                map.get(key);
                map.containsKey(key);
                
            }
        };
    }
    
    public void overflow() {
        ArrayDeque list = new ArrayDeque<>();

        //137300000
        //2147483647
        //532200000
        //536800000
        DLog.print(Integer.MAX_VALUE);
        DLog.main().async = false;
        
        Object ref = new Object();
        for (long i = 0; i < (long) Integer.MAX_VALUE + 1; i++) {
            list.add(ref);
            if (i % 100000 == 0) {
                DLog.print(i);
            }
        }
        
        DLog.print("Size", list.size());
    }
    
    public void benchHash() {
        Benchmark b = new Benchmark();
        
        Map<Integer, String> map1 = new HashMap<>();
        Map<Integer, String> map2 = new PrefillArrayMap<>();
        
        b.threads = 1;
        b.useGVhintAfterFullBench = true;
        DLog.println(b.executeBench(5000, "HashMap", makeRun(map1, new FastRandom(1337), 10000)));
        DLog.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new FastRandom(1337), 10000)));
        
        DLog.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new FastRandom(1337), 10000)));
        DLog.println(b.executeBench(5000, "HashMap", makeRun(map1, new FastRandom(1337), 10000)));
        
        DLog.println(b.executeBench(5000, "HashMap", makeRun(map1, new FastRandom(1337), 10000)));
        DLog.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new FastRandom(1337), 10000)));
        
        DLog.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new FastRandom(1337), 10000)));
        DLog.println(b.executeBench(5000, "HashMap", makeRun(map1, new FastRandom(1337), 10000)));
        F.checkedRun(() -> {
            DLog.await(1, TimeUnit.HOURS);
        });
    }
    
//    @Test
    public void testComprator() throws InterruptedException {
        DLog.main().async = false; 
        Comparator<NumberValue<Integer>> ofValue = Comparator.comparing(f -> f.getValue());
        PriorityBlockingQueue<NumberValue<Integer>> list = new PriorityBlockingQueue<>(1,ofValue.reversed());
        
        for (int i = 0; i < 10; i++) {
            list.add(new IntegerValue(i));
        }
//        list.add(NumberValue.of(null));
        for (int i = 0; i < 10; i++, i++) {
            list.add(new IntegerValue(i));
        }
        
        DLog.print(list);
        
        for(int i = 0; i < 15; i++){
            DLog.print(list.take());
        }
        
//        Collections.sort(list, ofValue);
        
        DLog.print(list);
        
    }
}
