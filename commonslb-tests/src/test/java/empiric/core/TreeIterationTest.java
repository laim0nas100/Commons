/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.ReflectionUtils;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.graphtheory.GNode;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.iteration.impl.TreeVisitorImpl;
import lt.lb.commons.misc.rng.RandomDistribution;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class TreeIterationTest {

//    @Test
    public void ok() {
        Log.println("Init log");
        Log.main().async = false;

        ReadOnlyBidirectionalIterator<Integer> of = ReadOnlyIterator.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        ReadOnlyBidirectionalIterator<Integer> of1 = ReadOnlyIterator.of(-1, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        ReadOnlyIterator<Integer> of2 = ReadOnlyIterator.composite();
        ReadOnlyIterator<Integer> comp = ReadOnlyIterator.composite(of2, of1, of);

        Log.printLines(comp.iterator());

        Log.print("ok");
        Log.print("Stack depth:", ReflectionUtils.getMaximumStackDepth());

        F.unsafeRun(() -> Log.await(1, TimeUnit.HOURS));

    }

    public static Orgraph generateTree(int layers, int childPerLayer) {
        Orgraph g = new Orgraph();
        for (int i = 0; i < childPerLayer; i++) {
            g.linkNodes(0, i + 1, 0);
        }
        for (int i = 0; i < layers; i++) {
            for (int j = 0; j < childPerLayer; j++) {
                long from = i + j + 1;
                long to = from + childPerLayer;
                g.linkNodes(from, to, 0);
            }
        }

        return g;
    }

    @Test
    public void graphIteration() {
        Orgraph g = new Orgraph();
        g.linkNodes(0, 1, 0);
        g.linkNodes(0, 2, 0);
        g.linkNodes(0, 3, 0);
        g.linkNodes(1, 4, 0);
        g.linkNodes(1, 5, 0);
        g.linkNodes(2, 6, 0);
        g.linkNodes(3, 7, 0);
        Log.println("", g.toStringNodes());
        Log.println("", g.toStringLinks());
        TreeVisitor<GNode> it;
        it = treeVisitor(g, -1);

        Log.print("BFS");
        GNode node = g.getNode(0).get();
        it.BFS(node);

        Log.print("DFS rec");
        it.DFS(node);

        Log.print("DFS it");
        it.DFSIterative(node);

        Log.print("Post order");
        it.PosOrder(node);

        Log.print("Post order it");
        it.PosOrderIterative(node);

//        Orgraph tree = generateTree(50000, 6);
//        Log.print("TREE");
//        Log.print(tree.toStringNodes());
//        it = treeVisitor(tree, 999);
//        node = tree.getNode(0).get();
//        Log.print("Found BFS?:", it.BFS(node));
//        Log.print("Found Post it?:", it.PosOrderIterative(node));
//        Log.print("Found DFS it?:", it.DFSIterative(node));
        Log.print("Post order 2");
        Log.print("Found  ?:",TreeVisitorImpl.PostOrderCaller(it, node, Optional.empty(),false).resolve());
        Log.print("Post order 3");
        Log.print("Found  ?:",TreeVisitorImpl.PostOrderCaller(it, node, Optional.empty(),true).resolve());
        
        Log.print("DFS order 2");
        Log.print("Found  ?:",TreeVisitorImpl.DFSCaller(it, node, Optional.empty(),false).resolve());
        Log.print("DFS order 3");
        Log.print("Found  ?:",TreeVisitorImpl.DFSCaller(it, node, Optional.empty(),true).resolve());
        F.unsafeRun(() -> Log.await(1, TimeUnit.HOURS));

    }

    public static TreeVisitor<GNode> treeVisitor(Orgraph gr, long id) {
        return new TreeVisitor<GNode>() {
            @Override
            public Boolean find(GNode item) {
                Log.print("Visiting:", item.ID);
                return item.ID == id;
            }

//            @Override
//            public ReadOnlyIterator<GNode> getChildrenIterator(GNode item) {
//                return ReadOnlyIterator.of(gr.resolveLinkedTo(item, i -> true))
//                        .map(link -> link.nodeFrom == item.ID ? gr.getNode(link.nodeTo) : gr.getNode(link.nodeFrom))
//                        .map(m -> m.get());
//            }
            @Override
            public ReadOnlyIterator<GNode> getChildrenIterator(GNode item) {
                return ReadOnlyIterator.of(item.linksTo).map(id -> gr.getNode(id).get());
            }
        };

    }

    public static void benchMe(int times, int layers, int children) {
        Benchmark bench = new Benchmark();
        bench.threads = 1;
        Orgraph tree = generateTree(layers, children);

        RandomDistribution rng = RandomDistribution.uniform(new Random());
        GNode root = tree.getNode(0).get();

        for (int i = 0; i < 10; i++) {
            Integer find = rng.nextInt(tree.nodes.size());
            TreeVisitor<GNode> it = treeVisitor(tree, find);

            bench.executeBench(times, "DFS rec", () -> {
                TreeVisitorImpl.DFS(it, root, Optional.empty());
            }).print(Log::print);

            bench.executeBench(times, "DFS it", () -> {
                TreeVisitorImpl.DFSIterative(it, root, Optional.empty());
            }).print(Log::print);
            
            bench.executeBench(times, "DFS cal", () -> {
                TreeVisitorImpl.DFSCaller(it, root, Optional.empty(),true).resolve();
            }).print(Log::print);
            
            bench.executeBench(times, "Pos rec", () -> {
                TreeVisitorImpl.PostOrder(it, root, Optional.empty());
            }).print(Log::print);

            bench.executeBench(times, "Pos it", () -> {
                TreeVisitorImpl.PostOrderIterative(it, root, Optional.empty());
            }).print(Log::print);
            
            bench.executeBench(times, "Pos cal", () -> {
                TreeVisitorImpl.PostOrderCaller(it, root, Optional.empty(),true).resolve();
            }).print(Log::print);
        }

    }

    public static void main(String... args) {

        benchMe(20, 10000, 5000);
        Log.close();
    }
}
