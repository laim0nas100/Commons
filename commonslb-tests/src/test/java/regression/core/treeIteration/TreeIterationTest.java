/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core.treeiteration;

import static empiric.core.TreeIterationTest.treeVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import lt.lb.commons.Log;
import lt.lb.commons.graphtheory.GNode;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.iteration.ChildrenIteratorProvider;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.iteration.impl.TreeVisitorImpl;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class TreeIterationTest {

    public ChildrenIteratorProvider<GNode> makeChildrenIterator(Orgraph graph) {
        return item -> ReadOnlyIterator.of(item.linksTo).map(id -> graph.getNode(id).get());
    }

    public TreeVisitor<GNode> nodeCollector(Orgraph graph, List<Integer> ids) {
        return TreeVisitor.ofAll(
                item -> ids.add((int) item.ID),
                makeChildrenIterator(graph)::getChildrenIterator
        );
    }

    public void testTreeOrder(
            Orgraph graph,
            Function<TreeVisitor<GNode>, Function<GNode, Optional<GNode>>> func,
            List<Integer> ids
    ) {
        List<Integer> collected = new ArrayList<>();
        TreeVisitor<GNode> nodeCollector = nodeCollector(graph, collected);
        Optional<GNode> root = graph.getNode(0);

        func.apply(nodeCollector).apply(root.get());

        assertThat(collected).isEqualTo(ids);

    }

    public void testTreeOrderIterator(
            Orgraph graph,
            Function<ChildrenIteratorProvider<GNode>, Function<GNode, ReadOnlyIterator<GNode>>> func,
            List<Integer> ids
    ) {
        ChildrenIteratorProvider<GNode> childrenIterator = makeChildrenIterator(graph);
        Optional<GNode> root = graph.getNode(0);

        ArrayList<Integer> toArrayList = func.apply(childrenIterator).apply(root.get()).map(m -> (int) m.ID).toArrayList();

        assertThat(toArrayList).isEqualTo(ids);

    }

    @Test
    public void baseTest() {
        Orgraph g = new Orgraph();
        g.linkNodes(0, 1, 0);
        g.linkNodes(0, 2, 0);
        g.linkNodes(0, 3, 0);
        g.linkNodes(1, 4, 0);
        g.linkNodes(1, 5, 0);
        g.linkNodes(2, 6, 0);
        g.linkNodes(3, 7, 0);

        List<Integer> dfs = Arrays.asList(0, 1, 4, 5, 2, 6, 3, 7);
        testTreeOrder(g, it -> it::DFS, dfs);
        testTreeOrder(g, it -> it::DFSIterative, dfs);

        List<Integer> bfs = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        testTreeOrder(g, it -> it::BFS, bfs);

        List<Integer> postOrder = Arrays.asList(4, 5, 1, 6, 2, 7, 3, 0);
        testTreeOrder(g, it -> it::PostOrder, postOrder);
        testTreeOrder(g, it -> it::PostOrderIterative, postOrder);

        //iterators
        testTreeOrderIterator(g, cp -> cp::DFSiterator, dfs);
        testTreeOrderIterator(g, cp -> cp::BFSiterator, bfs);
        testTreeOrderIterator(g, cp -> cp::PostOrderIterator, postOrder);

    }

    @Test
    public void multipleLinkTest() {
        Orgraph g = new Orgraph();
        g.linkNodes(0, 1, 0);
        g.linkNodes(0, 2, 0);
        g.linkNodes(0, 3, 0);
        g.linkNodes(1, 4, 0);
        g.linkNodes(1, 5, 0);
        g.linkNodes(2, 6, 0);
        g.linkNodes(3, 7, 0);
        g.linkNodes(5, 2, -1);
        g.linkNodes(5, 3, -1);

        List<Integer> dfs = Arrays.asList(0, 1, 4, 5, 2, 6, 3, 7);
        testTreeOrder(g, it -> n -> it.DFS(n, new HashSet<>()), dfs);
        testTreeOrder(g, it -> n -> it.DFSIterative(n, new HashSet<>()), dfs);

        List<Integer> bfs = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        testTreeOrder(g, it -> n -> it.BFS(n, new HashSet<>()), bfs);

        List<Integer> postOrder = Arrays.asList(4, 6, 2, 7, 3, 5, 1, 0);
        testTreeOrder(g, it -> n -> it.PostOrder(n, new HashSet<>()), postOrder);
        testTreeOrder(g, it -> n -> it.PostOrderIterative(n, new HashSet<>()), postOrder);

        //iterators
        testTreeOrderIterator(g, cp -> n -> cp.DFSiterator(n, new HashSet<>()), dfs);
        testTreeOrderIterator(g, cp -> n -> cp.BFSiterator(n, new HashSet<>()), bfs);
        testTreeOrderIterator(g, cp -> n -> cp.PostOrderIterator(n, new HashSet<>()), postOrder);

    }

    @Test
    public void loopLinkTest() {
        Orgraph g = new Orgraph();
        g.linkNodes(0, 1, 0);
        g.linkNodes(1, 2, 0);
        g.linkNodes(2, 3, 0);
        g.linkNodes(3, 0, 0);
        g.linkNodes(3, 4, 0);
        g.linkNodes(4, 1, 0);

        List<Integer> dfs = Arrays.asList(0, 1, 2, 3, 4);
        testTreeOrder(g, it -> n -> it.DFS(n, new HashSet<>()), dfs);
        testTreeOrder(g, it -> n -> it.DFSIterative(n, new HashSet<>()), dfs);

        List<Integer> bfs = Arrays.asList(0, 1, 2, 3, 4);
        testTreeOrder(g, it -> n -> it.BFS(n, new HashSet<>()), bfs);

        List<Integer> postOrder = Arrays.asList(4, 3, 2, 1, 0);
        testTreeOrder(g, it -> n -> it.PostOrder(n, new HashSet<>()), postOrder);
        testTreeOrder(g, it -> n -> it.PostOrderIterative(n, new HashSet<>()), postOrder);

        //iterators
        testTreeOrderIterator(g, cp -> n -> cp.DFSiterator(n, new HashSet<>()), dfs);
        testTreeOrderIterator(g, cp -> n -> cp.BFSiterator(n, new HashSet<>()), bfs);
        testTreeOrderIterator(g, cp -> n -> cp.PostOrderIterator(n, new HashSet<>()), postOrder);

    }
}
