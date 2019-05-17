/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.ReflectionUtils;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.graphtheory.GNode;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.iteration.impl.CompositeROI;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class TreeIterationTest {

    @Test
    public void ok() {
        Log.println("Init log");
        Log.main().async = false;

        ReadOnlyBidirectionalIterator<Integer> of = ReadOnlyIterator.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        ReadOnlyBidirectionalIterator<Integer> of1 = ReadOnlyIterator.of(-1, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        ReadOnlyIterator<Integer> of2 = ReadOnlyIterator.composite();
        ReadOnlyIterator<Integer> comp = ReadOnlyIterator.composite(of2, of1, of);

        Log.printLines(comp.iterator());

        Log.print("ok");
        Log.print("Stack depth:",ReflectionUtils.getMaximumStackDepth());

        F.unsafeRun(() -> Log.await(1, TimeUnit.HOURS));

    }

    public Orgraph generateTree(int layers, int childPerLayer) {
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
        it = treeVisitor(g,-1);

        Log.print("BFS");
        it.BFS(g.getNode(0).get());

        Log.print("DFS rec");
        it.DFS(g.getNode(0).get());

        Log.print("DFS it");
        it.DFSIterative(g.getNode(0).get());

        Orgraph tree = this.generateTree(50000, 6);
        Log.print("TREE");
//        Log.print(tree.toStringNodes());
        it = treeVisitor(tree,999);
        Log.print("Found DFS it?:",it.DFSIterative(tree.getNode(0).get()));
        Log.print("Found DFS ?:",it.DFS(tree.getNode(0).get()));

        F.unsafeRun(() -> Log.await(1, TimeUnit.HOURS));

    }

    public static TreeVisitor<GNode> treeVisitor(Orgraph gr, long id) {
        return new TreeVisitor<GNode>() {
            @Override
            public Boolean find(GNode item) {
//                Log.print("Visiting:", item.ID);
                return item.ID == id;
            }

            @Override
            public ReadOnlyIterator<GNode> getChildrenIterator(GNode item) {
                Stream<GNode> collect = gr.resolveLinkedTo(item, i -> true)
                        .stream()
                        .map(link -> link.nodeFrom == item.ID ? gr.getNode(link.nodeTo) : gr.getNode(link.nodeFrom))
                        .map(m -> m.get());
                return ReadOnlyIterator.of(collect);
            }
        };
    }
}
