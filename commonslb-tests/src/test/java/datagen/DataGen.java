/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datagen;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.fastid.FastIDGen;
import lt.lb.fastid.FastID;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.containers.values.LongValue;
import lt.lb.commons.misc.UUIDgenerator;
import lt.lb.commons.misc.rng.RandomDistribution;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Lemmin
 */
public class DataGen {

    /**
     * ID, Graph,
     *
     * ID pvz: GC-G(4)_GA-1_mutSwap_crossPartialyMapped_isBest_result
     */
    /**
     *
     * @param args
     */
    static RandomDistribution rand = RandomDistribution.uniform(new Random());
    static final String[] algoNames = ArrayOp.asArray(
            "GA",
            "ACO",
            "SA",
            "NS"
    );

    static final String[] iterNumber = ArrayOp.asArray(
            "1",
            "2",
            "3",
            "4",
            "5"
    );

    static final String[] epochNumber = ArrayOp.asArray(
            "1",
            "2"
    );

    static final String[] param0_1 = ArrayOp.asArray(
            "0",
            "0.2",
            "0.4",
            "0.6",
            "0.8",
            "1"
    );

    static final String[] param03_07 = ArrayOp.asArray(
            "0.3",
            "0.37",
            "0.44",
            "0.51",
            "0.58",
            "0.68"
    );

    static final String[] gcpNames = ArrayOp.asArray(
            "GCP-david",
            "GCP-huck",
            "GCP-queen8_8",
            "GCP-myciel5",
            "GCP-1-Fullins_3"
    );

    static final String[] tspNames = ArrayOp.asArray(
            "TSP-G(4)",
            "TSP-G(8)",
            "TSP-berllin52",
            "TSP-att48",
            "TSP-bays29"
    );
    static final String[] mutationNames = ArrayOp.asArray(
            "mutationNodeMove",
            "mutationNodeSwap",
            "mutationCentralInversion",
            "mutationInnerSequenceInversion",
            "mutationPathCutoff",
            "mutationCombined"
    );

    static final String[] tspCrossNames = ArrayOp.asArray(
            "crossoverPartiallyMapped",
            "crossoverOrder",
            "crossoverCycle"
    );

    static final String[] tspSimNames = ArrayOp.asArray(
            "simLongestCommonsSubseq",
            "simCommonLinks",
            "simScore"
    );

    static final String[] gcpCrossNames = ArrayOp.asArray(
            "crossoverPartition",
            "crossoverSubgraph",
            "crossoverSinglePoint",
            "crossoverTwoPoint"
    );

    static final String[] gcpSimNames = ArrayOp.asArray(
            "simColorPos",
            "simIzomorf",
            "simScore"
    );
    static final Tuple<String, L>[] algoPairsTSP = ArrayOp.asArray(
            Tuples.create("NS", L.of(tspSimNames, mutationNames, tspCrossNames)),
            Tuples.create("GC", L.of(mutationNames, tspCrossNames)),
            Tuples.create("ACO", L.of(mutationNames, param0_1, param03_07, epochNumber)),
            Tuples.create("SA", L.of(mutationNames, param0_1, param03_07, epochNumber))
    );
    static final Tuple<String, L>[] algoPairsGCP = ArrayOp.asArray(
            Tuples.create("NS", L.of(gcpSimNames, mutationNames, gcpCrossNames)),
            Tuples.create("GC", L.of(mutationNames, gcpCrossNames)),
            Tuples.create("ACO", L.of(mutationNames, param0_1, param03_07, epochNumber)),
            Tuples.create("SA", L.of(mutationNames, param0_1, param03_07, epochNumber))
    );

    static Tuple<String, DataProv>[] graphAndDataTSP = ArrayOp.asArray(
            Tuples.create(tspNames[0], new RandomData(1, ArrayOp.asArray(1020, 24))),
            Tuples.create(tspNames[1], new JitterData(1, 1032, () -> rand.nextInt(5) * 1000)),
            Tuples.create(tspNames[2], new JitterData(1, 7542, () -> rand.nextInt(1000) * 5)),
            Tuples.create(tspNames[3], new JitterData(1, 33522, () -> rand.nextInt(1000) * 2)),
            Tuples.create(tspNames[4], new JitterData(1, 2020, () -> rand.nextInt(1000) * 2))
    );
    static Tuple<String, DataProv>[] graphAndDataGCP = ArrayOp.asArray(
            Tuples.create(gcpNames[0], new JitterData(1, 11, () -> rand.nextInt(5))),
            Tuples.create(gcpNames[1], new JitterData(1, 13, () -> rand.nextInt(5))),
            Tuples.create(gcpNames[2], new JitterData(1, 9, () -> rand.nextInt(5))),
            Tuples.create(gcpNames[3], new JitterData(1, 6, () -> rand.nextInt(5))),
            Tuples.create(gcpNames[4], new JitterData(1, 4, () -> rand.nextInt(5)))
    );

    public static List<Iterations> iterTSP() {
        return iter(algoPairsTSP, graphAndDataTSP);
    }

    public static List<Iterations> iterGCP() {
        return iter(algoPairsGCP, graphAndDataGCP);
    }

    public static List<Iterations> iter(Tuple<String, L>[] algoPairs, Tuple<String, DataProv>[] graphAndData) {
        List<Iterations> list = new ArrayList<>();
        for (Tuple<String, L> tupAlgo : algoPairs) {
            for (Tuple<String, DataProv> tup : graphAndData) {
                Iterations add = new Iterations()
                        .addArray(tupAlgo.g1)
                        .addArray(tup.g1);

                tupAlgo.g2.list.forEach(arr -> {
                    add.addArray(arr);
                });

                add.addArray(iterNumber)
                        .add(tup.g2);
                add.reverseOrder();
                add.printReverse = true;
                list.add(add);
            }
        }

        return list;
    }

    public static List<Iterations> NS() {
        List<Iterations> list = new ArrayList<>();
        list.add(new Iterations()
                .addArray("NS")
                .addArray(tspNames[0])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new RandomData(1, ArrayOp.asArray(1020, 24)))
        );
        list.add(new Iterations()
                .addArray("NS")
                .addArray(tspNames[1])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new ArrayData(1032))
        );
        list.add(new Iterations()
                .addArray("NS")
                .addArray(tspNames[2])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new JitterData(1, 7542, () -> rand.nextInt(1000)))
        );
        list.add(new Iterations()
                .addArray("NS")
                .addArray(tspNames[3])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new JitterData(1, 33522, () -> rand.nextInt(1000)))
        );
        list.add(new Iterations()
                .addArray("NS")
                .addArray(tspNames[4])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new JitterData(1, 2020, () -> rand.nextInt(1000)))
        );

        list.add(new Iterations()
                .addArray("NS")
                .addArray(gcpNames[0])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new RandomData(1, ArrayOp.asArray(1020, 24)))
        );
        list.add(new Iterations()
                .addArray("NS")
                .addArray(gcpNames[1])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new ArrayData(1032))
        );
        list.add(new Iterations()
                .addArray("NS")
                .addArray(gcpNames[2])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new JitterData(1, 7542, () -> rand.nextInt(1000)))
        );
        list.add(new Iterations()
                .addArray("NS")
                .addArray(gcpNames[3])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new JitterData(1, 33522, () -> rand.nextInt(1000)))
        );
        list.add(new Iterations()
                .addArray("NS")
                .addArray(gcpNames[4])
                .addArray(iterNumber)
                .addArray(mutationNames)
                .addArray(tspCrossNames)
                .addArray(tspSimNames)
                .add(new JitterData(1, 2020, () -> rand.nextInt(1000)))
        );

        return list;
    }

    public static class L {

        public List<String[]> list = new ArrayList<>();

        public L() {

        }

        public static L of() {
            return new L();
        }

        public static L of(String[]... strigns) {
            L l = new L();
            for (String[] ar : strigns) {
                l.list.add(ar);
            }
            return l;
        }

    }

    public static void main(String[] args) throws Exception {
        DLog.println("hi");
        FastIDGen id1 = new FastIDGen();

//        List<Iterations> iterTSP = iterTSP();
//        List<Iterations> iterGCP = iterGCP();
        Benchmark bench = new Benchmark();
        bench.threads = 32;
        bench.useGVhintAfterFullBench = true;
        bench.useGChint = false;
        bench.warmupTimes = 100;
        for(int i = 0; i < 1000; i++){
            FastID.getAndIncrementGlobal();
        }

        int benchTime = 500;
        int times = 2500;
        AtomicLong l = new AtomicLong();
        
        ThreadLocal<LongValue> lv = ThreadLocal.withInitial(()->new LongValue(0));
        

        bench.executeBenchParallel(benchTime, "Counter", () -> {
            for (int i = 0; i < times; i++) {
                l.incrementAndGet();
            }
        }).print(DLog::print);
        bench.executeBenchParallel(benchTime, "My UUID", () -> {
            for (int i = 0; i < times; i++) {
                UUIDgenerator.nextUUID();
            }
        }).print(DLog::print);
        
        bench.executeBenchParallel(benchTime, "FastID", () -> {
            for (int i = 0; i < times; i++) {
                id1.getAndIncrement();
            }
        }).print(DLog::print);
        bench.executeBenchParallel(benchTime, "FastID global", () -> {
            for (int i = 0; i < times; i++) {
                FastID.getAndIncrementGlobal();
            }
        }).print(DLog::print);
        
        bench.executeBenchParallel(benchTime, "Long value thread local", () -> {
            for (int i = 0; i < times; i++) {
                lv.get().incrementAndGet();
            }
        }).print(DLog::print);
        
        DLog.print(FastID.getAndIncrementGlobal());
        
//Deterministic generation

//        writeToFile("GCP", iterGCP);
//        writeToFile("TSP", iterTSP);
//        iter.addAll(iterGCP());
//        for (Iterations i : iter) {
//            i.iterate(list -> {
//                DLog.print(list);
//            });
//        }
        DLog.print(l.get());
        
        
        DLog.print("ID copy");
        FastID id = FastID.getAndIncrementGlobal();
        
        DLog.print(id);
        
        FastID idCopy= new FastID(id.toString());
        DLog.print(idCopy);
        
        DLog.close();

    }

    public static void writeToFile(String fileName, List<Iterations> allIter) throws FileNotFoundException, UnsupportedEncodingException {
        ArrayList<String> list = new ArrayList<>();
        for (Iterations iter : allIter) {
            iter.iterate(cons -> {
                list.add(asLine(cons));
            });
        }

        lt.lb.commons.io.text.TextFileIO.writeToFile(fileName, list);
    }

    public static interface DataProv {

        public int size();

        public String get(int i);
    }

    public static class ArrayData implements DataProv {

        private Object[] array;

        public ArrayData(Object... objs) {
            array = objs;
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public String get(int i) {
            return array[i] + "";
        }

    }

    public static class RandomData implements DataProv {

        private List collection;
        private int size;

        public RandomData(int size, Object[] objs) {
            collection = Stream.of(objs).collect(Collectors.toList());
            this.size = size;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public String get(int i) {
            return rand.pickRandom(collection) + "";
        }
    }

    public static class JitterData implements DataProv {

        private int size;
        private int value;
        private Supplier<Integer> rng;

        public JitterData(int size, int value, Supplier<Integer> rng) {
            this.size = size;
            this.value = value;
            this.rng = rng;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public String get(int i) {
            return (rng.get() + value) + "";
        }
    }

    public static class Iterations {

        public List<DataProv> list = new ArrayList<>();
        public boolean printReverse = false;

        public Iterations addArray(String... str) {
            return add(new ArrayData(str));
        }

        public Iterations add(DataProv prov) {
            list.add(prov);
            return this;
        }

        public void reverseOrder() {
            Collections.reverse(list);
        }

        public void iterate(Consumer<String[]> cons) {
            int size = list.size();
            int[] listSize = new int[size];
            int[] listIndex = new int[size];
            for (int i = 0; i < size; i++) {
                listSize[i] = list.get(i).size();
                listIndex[i] = 0;
            }

            while (true) {
                String[] args = new String[size];
                for (int i = 0; i < size; i++) {
                    args[i] = list.get(i).get(listIndex[i]);
                }
                if (this.printReverse) {
                    ArrayUtils.reverse(args);
                }
                cons.accept(args);
                boolean end = true;
                for (int i = 0; i < size; i++) {
                    if (listIndex[i] + 1 < listSize[i]) {
                        listIndex[i]++;
                        for (int j = 0; j < i; j++) {
                            listIndex[j] = 0;
                        }
                        end = false;
                        break;
                    }
                }
                if (end) {
                    return;
                }

            }
            // else
        }
    }

    public static String asLine(String[] arr) {
        LineStringBuilder sb = new LineStringBuilder();
        for (String s : arr) {
            sb.append(s).append(", ");
        }

        if (sb.length() > 0) {
            sb.removeFromEnd(2);
        }

        return sb.toString();
    }

}
