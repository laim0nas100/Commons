/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package empiric.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.fastid.FastIDGen;

/**
 *
 * @author Lemmin
 */
public class BunchingTreeTest {

    public static class XY implements Comparable<XY>{

        static FastIDGen gen = new FastIDGen();
        public final String id;
        public final double x;
        public final double y;

        public XY(String id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        public XY(double x, double y) {
            id = gen.getAndIncrement().toString();
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.id);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final XY other = (XY) obj;
            return Objects.equals(this.id, other.id);
        }

        @Override
        public String toString() {
            return id + ":" + x + ":" + y;
        }

        public static AtomicLong counter = new AtomicLong(0);

        public static double distance(XY one, XY two) {
            counter.incrementAndGet();
            double dx = one.x - two.x;
            double dy = one.y - two.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public int compareTo(XY o) {
            return id.compareTo(o.id);
        }

    }

    public static class XYTree extends BunchingTree.BunchingEuclideanTree<XY> {

        public XYTree(double maxPossibleDistance, BiFunction<XY, XY, Double> distanceFunc) {
            super(maxPossibleDistance, distanceFunc);
        }


        @Override
        public Double distance(XY one, XY two) {
            return XY.distance(one, two);
        }

    }

    public static void main(String... args) {
        RandomDistribution r = RandomDistribution.uniform(new FastRandom(1));
        Random rng = new Random(2);
        double range = 1000_000;

        int size = 100_000;
        List<XY> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(new XY(rng.nextInt((int) range), rng.nextInt((int) range)));
        }
        double maxDistance = XY.distance(new XY(0, 0), new XY(range, range));
        DLog.print("maxDistance", maxDistance);

        XYTree tree = new XYTree(maxDistance, XY::distance);
        
        XY test = new XY(rng.nextInt((int) range), rng.nextInt((int) range));
        Set<XY> skip = new HashSet<>();
        int slices = 3;
        
        double slice = range / (slices+1);
        for(int i = 1; i < slices+1; i++){
            for(int j=1; j < slices+1; j++){
                XY findNearest = findNearest(list, new XY(slice*i,slice*j));
                tree.add(findNearest);
//                tree.addRootNode(findNearest);
                skip.add(findNearest);
//                test = findNearest;
            }
            
        }
        
        

        int progress = 0;
        for (XY xy : list) {
            if(!skip.contains(xy)){
                tree.add(xy);
            }
            
            progress++;
            if(progress % 1000 == 0){
                DLog.print("tree progress",progress);
            }
        }
        DLog.print("Tree building", XY.counter.getAndSet(0));
        test= new XY(range/2, range/2);
        
        

        final int count = 10;

        List<XY> top10 = findNearest(list, test, count);
        Collections.sort(top10);
        DLog.print("Full list sorting", XY.counter.getAndSet(0));

        List<XY> findNearest = tree.findNearest(count, test);
        Collections.sort(findNearest);

        DLog.print("tree.findNearest", XY.counter.getAndSet(0));

        Benchmark bench = new Benchmark();

//        bench.executeBench(500, "List.sorting", () -> {
//            XY xy = new XY(rng.nextInt((int) range), rng.nextInt((int) range));
//            findNearest(list, xy, count);
//        }).print(DLog::print);
//
//        bench.executeBench(500, "Tree.findNearest", () -> {
//            XY xy = new XY(rng.nextInt((int) range), rng.nextInt((int) range));
//            tree.findNearest(count, xy);
//        }).print(DLog::print);

        DLog.print(test);
        DLog.printLines(top10);
        DLog.printLines(findNearest);
//        DLog.print("BFS");
//        tree.visitor(f->{
//            DLog.print(f.getDepth(),f.getValue());
//            return false;
//        }).BFS(tree.getRoot());
//         DLog.print("DFS");
////         
//         tree.visitor(f->{
//            DLog.print(f.getDepth(),f.getValue());
//            return false;
//        }).DFS(tree.getRoot());
        DLog.print(tree.nodeCount());

    }

    public static XY findNearest(List<XY> list, XY test){
        return MakeStream.from(list).map(m -> Tuples.create(m, XY.distance(m, test))).sorted((a, b) -> {
            return Double.compare(a.g2, b.g2);
        }).map(m -> m.g1).findFirst().get();
    }
    
    public static List<XY> findNearest(List<XY> list, XY test, int count){
        return MakeStream.from(list).map(m -> Tuples.create(m, XY.distance(m, test))).sorted((a, b) -> {
            return Double.compare(a.g2, b.g2);
        }).map(m -> m.g1).limit(count).toList();
    }
    
    /*
     *  coordiante calculation
    ACOS(COS(RADIANS(90-Lat1)) *COS(RADIANS(90-Lat2)) +SIN(RADIANS(90-Lat1)) *SIN(RADIANS(90-Lat2)) *COS(RADIANS(Long1-Long2))) *6371
     */
    static AtomicLong counter = new AtomicLong(0);

    public static double WSGdistance(double lat1, double lon1, double lat2, double lon2) {
        counter.incrementAndGet();

        double val = Math.cos(Math.toRadians(90 - lat1)) * Math.cos(Math.toRadians(90 - lat2));

        double val2 = Math.sin(Math.toRadians(90 - lat1)) * Math.sin(Math.toRadians(90 - lat2)) * Math.cos(Math.toRadians(lon1 - lon2));

        return Math.acos(val + val2) * 6371;

//        return Math.acos(
//                Math.cos(Math.toRadians(90 - lat1))
//                * Math.cos(Math.toRadians(90 - lat2))
//                + Math.sin(Math.toRadians(90 - lat1))
//                * Math.sin(Math.toRadians(90 - lat2))
//                * Math.cos(Math.toRadians(lon1 - lon2))
//        ) * 6371;
    }

    public static double WSGdistance(double[] one, double[] two) {
        return WSGdistance(one[0], one[1], two[0], two[1]);
    }

    public static class LocationTree extends BunchingTree<double[], Double> {

        public LocationTree(int div, int layerLimit) {
            super(div, layerLimit);
        }

        @Override
        public Double distance(double[] one, double[] two) {
            return WSGdistance(one, two);
        }

        @Override
        public boolean include(int depth, double[] value, Double distance, boolean searching) {

            if (depth == 0) {// root
                return true;
            }
            return distance < 800d / depth;
        }

        public void add(double one, double two) {
            add(new double[]{one, two});
        }

        @Override
        public int overlapMax(int depth, double[] value, boolean searching) {
            return 1;
        }

    }

}
