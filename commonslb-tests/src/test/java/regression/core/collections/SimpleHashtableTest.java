/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package regression.core.collections;

import de.vandermeer.asciitable.AT_Context;
import de.vandermeer.asciitable.AT_Renderer;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.asciithemes.a7.A7_Grids;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.benchmarking.BenchmarkResult;
import lt.lb.commons.containers.collections.SimpleHashtable;
import lt.lb.commons.iteration.For;

/**
 *
 * @author laim0nas100
 */
public class SimpleHashtableTest {

    public static void benchInsert(int seed, int times, Map map) {
        Random r = new Random(seed);
        for (int i = 0; i < times; i++) {
            map.put(r.nextInt(), r.nextInt());
        }
    }

    public static void benchGet(int seed, int times, Map map) {
        Random r = new Random(seed);
        for (int i = 0; i < times; i++) {
            map.get(r.nextInt());
        }
    }

    public static void benchRemove(int seed, int times, Map map) {
        Random r = new Random(seed);
        for (int i = 0; i < times; i++) {
            map.remove(r.nextInt());
        }
    }

    public static void main(String[] args) {
        DLog.print("SUP");
        DLog.main().async = false;
        SimpleHashtable<String, String> table = new SimpleHashtable<>(8, true, 0.5f);

        int s = 10000;
        for (int i = 0; i < s; i++) {
            table.put("key-" + i, "val" + i);
        }
        for (int i = 0; i < s - 2; i++) {
            table.remove("key-" + i);
        }

//        DLog.printLines(table.entrySet());
//        String remove = table.remove("key-998");
//
//        DLog.print(remove);
//        DLog.printLines(table.entrySet());
        DLog.print(table);
        if (true) {
            return;
        }

        Benchmark bench = new Benchmark();
        bench.useGVhintAfterFullBench = false;
        bench.warmupTimes = 0;

        int times = 4;
        int size = 5_000_000;

        AsciiTable ascii = new AsciiTable();
        ascii.addRow("Name", "Avg(ms)", "Min(ms)", "Max(ms)", "Total(s)");
        ascii.addRule();

        Map<Integer, List<BenchmarkResult>> results = new LinkedHashMap<>();

        int offset = new Random().nextInt();
        benchMap(offset, results, times, size, bench, new HashMap<>());
        benchMap(offset, results, times, size, bench, new LinkedHashMap<>());
        benchMap(offset, results, times, size, bench, new SimpleHashtable());
        AT_Renderer renderer = ascii.getRenderer();
        renderer.setCWC(new CWC_LongestLine());
        AT_Context context = ascii.getContext();
        context.setGrid(A7_Grids.minusBarPlusEquals());

        For.entries().iterate(results, (k, val) -> {
            for (BenchmarkResult res : val) {
                append(ascii, res);
            }

        });

        System.out.println(ascii.render());

    }
    private static final double MILL = 1000_000;
    private static final double SEC = 1000_000_000;

    public static void append(AsciiTable table, BenchmarkResult result) {
        table.addRow(result.name, result.averageTime / MILL, result.minTime / MILL, result.maxTime / MILL, result.totalTime / SEC);
    }

    public static void benchMap(int offset, Map<Integer, List<BenchmarkResult>> results, int times, int size, Benchmark bench, Map map) {
        bench.executeBench(times, map.getClass().getSimpleName() + " insert", () -> {
            benchInsert(offset + 1, size, map);
        }).chain(b -> {
            results.computeIfAbsent(0, k -> new ArrayList<>()).add(b);
            return b;
        }).print(DLog::print);

        bench.executeBench(times, map.getClass().getSimpleName() + " get 1", () -> {
            benchGet(offset + 2, size, map);
        }).chain(b -> {
            results.computeIfAbsent(1, k -> new ArrayList<>()).add(b);
            return b;
        }).print(DLog::print);
        bench.executeBench(times, map.getClass().getSimpleName() + " remove", () -> {
            benchRemove(offset + 3, size, map);
        }).chain(b -> {
            results.computeIfAbsent(2, k -> new ArrayList<>()).add(b);
            return b;
        }).print(DLog::print);
        bench.executeBench(times, map.getClass().getSimpleName() + " get 2", () -> {
            benchGet(offset + 4, size, map);
        }).chain(b -> {
            results.computeIfAbsent(3, k -> new ArrayList<>()).add(b);
            return b;
        }).print(DLog::print);

        bench.executeBench(times, map.getClass().getSimpleName() + " insert 2", () -> {
            benchInsert(offset + 5, size, map);
        }).chain(b -> {
            results.computeIfAbsent(4, k -> new ArrayList<>()).add(b);
            return b;
        }).print(DLog::print);

        bench.executeBench(times, map.getClass().getSimpleName() + " get 3", () -> {
            benchGet(offset + 6, size, map);
        }).chain(b -> {
            results.computeIfAbsent(5, k -> new ArrayList<>()).add(b);
            return b;
        }).print(DLog::print);
        map.clear();
        System.gc();
    }

}
