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
import lt.lb.commons.DLog;
import lt.lb.commons.misc.ReflectionUtils;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.graphtheory.GNode;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.iteration.impl.TreeVisitorImpl;
import lt.lb.commons.misc.rng.RandomDistribution;
import org.junit.Test;
import lt.lb.uncheckedutils.Checked;
/**
 *
 * @author laim0nas100
 */
public class TreeIterationTest {

//    @Test
    public void ok() {
        DLog.println("Init log");
        DLog.main().async = false;

        ReadOnlyBidirectionalIterator<Integer> of = ReadOnlyIterator.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        ReadOnlyBidirectionalIterator<Integer> of1 = ReadOnlyIterator.of(-1, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        ReadOnlyIterator<Integer> of2 = ReadOnlyIterator.composite();
        ReadOnlyIterator<Integer> comp = ReadOnlyIterator.composite(of2, of1, of);

        DLog.printLines(comp.iterator());

        DLog.print("ok");
        DLog.print("Stack depth:", ReflectionUtils.getMaximumStackDepth());

        Checked.uncheckedRun(() -> DLog.await(1, TimeUnit.HOURS));

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

//    @Test
    public void graphIteration() {
        Orgraph g = new Orgraph();
        g.linkNodes(0, 1, 0);
        g.linkNodes(0, 2, 0);
        g.linkNodes(0, 3, 0);
        g.linkNodes(1, 4, 0);
        g.linkNodes(1, 5, 0);
        g.linkNodes(2, 6, 0);
        g.linkNodes(3, 7, 0);
        DLog.println("", g.toStringNodes());
        DLog.println("", g.toStringLinks());
        TreeVisitor<GNode> it;
        it = treeVisitor(g, -1);

        DLog.print("BFS");
        GNode node = g.getNode(0).get();
        it.BFS(node);

        DLog.print("DFS rec");
        it.DFS(node);

        DLog.print("DFS it");
        it.DFSIterative(node);

        DLog.print("Post order");
        it.PostOrder(node);

        DLog.print("Post order it");
        it.PostOrderIterative(node);
        
        DLog.print("DFS order iterator");
        ReadOnlyIterator<GNode> DFSIterator = TreeVisitorImpl.DFSIterator(it,node,Optional.empty());
        for(GNode n :DFSIterator){
            DLog.print(n);
        }
        
        DLog.print("BFS order iterator");
        ReadOnlyIterator<GNode> BFSIterator = TreeVisitorImpl.BFSIterator(it,node,Optional.empty());
        for(GNode n :BFSIterator){
            DLog.print(n);
        }

//        Orgraph tree = generateTree(50000, 6);
//        DLog.print("TREE");
//        DLog.print(tree.toStringNodes());
//        it = treeVisitor(tree, 999);
//        node = tree.getNode(0).get();
//        DLog.print("Found BFS?:", it.BFS(node));
//        DLog.print("Found Post it?:", it.PosOrderIterative(node));
//        DLog.print("Found DFS it?:", it.DFSIterative(node));
//        DLog.print("Post order 2");
//        DLog.print("Found  ?:",TreeVisitorImpl.PostOrderCaller(it, node, Optional.empty(),false).resolve());
//        DLog.print("Post order 3");
//        DLog.print("Found  ?:",TreeVisitorImpl.PostOrderCaller(it, node, Optional.empty(),true).resolve());
//        
//        DLog.print("DFS order 2");
//        DLog.print("Found  ?:",TreeVisitorImpl.DFSCaller(it, node, Optional.empty(),false).resolve());
//        DLog.print("DFS order 3");
//        DLog.print("Found  ?:",TreeVisitorImpl.DFSCaller(it, node, Optional.empty(),true).resolve());
        Checked.uncheckedRun(() -> DLog.await(1, TimeUnit.HOURS));

    }

    public static TreeVisitor<GNode> treeVisitor(Orgraph gr, long id) {
        return new TreeVisitor<GNode>() {
            @Override
            public Boolean find(GNode item) {
//                DLog.print("Visiting:", item.ID);
                return item.ID == id;
            }

//            @Override
//            public ReadOnlyIterator<GNode> getChildrenIterator(GNode item) {
//                return ReadOnlyIterator.of(gr.resolveLinkedTo(item, i -> true))
//                        .map(link -> link.nodeFrom == item.ID ? gr.getNode(link.nodeTo) : gr.getNode(link.nodeFrom))
//                        .map(m -> m.get());
//            }
            @Override
            public ReadOnlyIterator<GNode> getChildren(GNode item) {
                return ReadOnlyIterator.of(item.linksTo).map(id -> gr.getNode(id).get());
            }
        };

    }
    
     public static TreeVisitor<GNode> treeLeafVisitor(Orgraph gr) {
        return new TreeVisitor<GNode>() {
            @Override
            public Boolean find(GNode item) {
//                DLog.print("Visiting:", item.ID);
                return item.linksTo.isEmpty();
            }

//            @Override
//            public ReadOnlyIterator<GNode> getChildrenIterator(GNode item) {
//                return ReadOnlyIterator.of(gr.resolveLinkedTo(item, i -> true))
//                        .map(link -> link.nodeFrom == item.ID ? gr.getNode(link.nodeTo) : gr.getNode(link.nodeFrom))
//                        .map(m -> m.get());
//            }
            @Override
            public ReadOnlyIterator<GNode> getChildren(GNode item) {
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
            }).print(DLog::print);

            bench.executeBench(times, "DFS it", () -> {
                TreeVisitorImpl.DFSIterative(it, root, Optional.empty());
            }).print(DLog::print);
            
            bench.executeBench(times, "Pos rec", () -> {
                TreeVisitorImpl.PostOrder(it, root, Optional.empty());
            }).print(DLog::print);

            bench.executeBench(times, "Pos it", () -> {
                TreeVisitorImpl.PostOrderIterative(it, root, Optional.empty());
            }).print(DLog::print);
            
        }

    }

    public static void main(String... args) {

        benchMe(30, 10000, 5000);
        DLog.close();
    }
}
