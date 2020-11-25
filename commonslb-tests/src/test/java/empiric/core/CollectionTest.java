package empiric.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.PrefillArrayList;
import lt.lb.commons.Log;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.values.NumberValue;
import lt.lb.commons.containers.collections.PrefillArrayMap;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.misc.compare.ExtComparator;
import lt.lb.commons.misc.rng.FastRandom;
import org.junit.Test;

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
        Log.main().async = true;
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
        Log.print(list.toString());
        
        ListIterator<Long> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
        }
        listIterator.remove();
        Log.print(list.toString());
        Log.println();
        while (listIterator.hasPrevious()) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex());
        listIterator.add(13L);
        Log.print(list.toString());
        Log.println();
        for (int i = 0; i < 10; i++) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        Log.println();
        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
        
        listIterator.set(20L);
        Log.print(list.toString());
        Log.await(1, TimeUnit.HOURS);
        
    }
    
    public UnsafeRunnable makeRun(Map<Integer, String> map, Random r, int times) {
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
        Log.print(Integer.MAX_VALUE);
        Log.main().async = false;
        
        Object ref = new Object();
        for (long i = 0; i < (long) Integer.MAX_VALUE + 1; i++) {
            list.add(ref);
            if (i % 100000 == 0) {
                Log.print(i);
            }
        }
        
        Log.print("Size", list.size());
    }
    
    public void benchHash() {
        Benchmark b = new Benchmark();
        
        Map<Integer, String> map1 = new HashMap<>();
        Map<Integer, String> map2 = new PrefillArrayMap<>();
        
        b.threads = 1;
        b.useGVhintAfterFullBench = true;
        Log.println(b.executeBench(5000, "HashMap", makeRun(map1, new FastRandom(1337), 10000)));
        Log.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new FastRandom(1337), 10000)));
        
        Log.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new FastRandom(1337), 10000)));
        Log.println(b.executeBench(5000, "HashMap", makeRun(map1, new FastRandom(1337), 10000)));
        
        Log.println(b.executeBench(5000, "HashMap", makeRun(map1, new FastRandom(1337), 10000)));
        Log.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new FastRandom(1337), 10000)));
        
        Log.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new FastRandom(1337), 10000)));
        Log.println(b.executeBench(5000, "HashMap", makeRun(map1, new FastRandom(1337), 10000)));
        F.checkedRun(() -> {
            Log.await(1, TimeUnit.HOURS);
        });
    }
    
//    @Test
    public void testComprator() throws InterruptedException {
        Log.main().async = false; 
        ExtComparator<NumberValue<Integer>> ofValue = ExtComparator.ofValue(f -> f.getValue());
        PriorityBlockingQueue<NumberValue<Integer>> list = new PriorityBlockingQueue<>(1,ofValue.reversed());
        
        for (int i = 0; i < 10; i++) {
            list.add(NumberValue.of(i));
        }
//        list.add(NumberValue.of(null));
        for (int i = 0; i < 10; i++, i++) {
            list.add(NumberValue.of(i));
        }
        
        Log.print(list);
        
        for(int i = 0; i < 15; i++){
            Log.print(list.take());
        }
        
//        Collections.sort(list, ofValue);
        
        Log.print(list);
        
    }
}
