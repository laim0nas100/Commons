package regression.core;

import empiric.core.StackOverflowTest.RecursionBuilder;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lt.lb.commons.caller.Caller;
import lt.lb.commons.caller.CallerForBuilderSimple;
import lt.lb.commons.graphtheory.GNode;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.iteration.impl.TreeVisitorImpl;
import lt.lb.commons.misc.rng.RandomDistribution;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class CallerTest {

    @Test
    public void callerTest() {
        int exp = 20;
        BigInteger big = BigInteger.valueOf(100);
        BigInteger fibb = RecursionBuilder.fibb(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(exp));
        BigInteger resolve = RecursionBuilder.fibbCaller(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(exp)).resolve();
        assertThat(fibb).isEqualTo(resolve);

        BigInteger m = BigInteger.valueOf(2);
        BigInteger n = BigInteger.valueOf(8);
        assertThat(RecursionBuilder.ackermann(m, n)).isEqualTo(RecursionBuilder.ackermannCaller(m, n).resolve());
    }

    @Test
    public void graphGenerationTest() {
        RandomDistribution rng = RandomDistribution.uniform(new Random());

        Orgraph tree = generateTree(rng.nextInt(50, 1000), rng.nextInt(50, 1000));
        GNode root = tree.getNode(0).get();

        for (int i = 0; i < 200; i++) {
            Integer id = rng.nextInt(10, tree.nodes.size());
            TreeVisitor<GNode> it = treeVisitor(tree, id);

            assertThat(tree.getNode(id).isPresent());
            assertThat(tree.getNode(id))
                    .isEqualTo(TreeVisitorImpl.BFS(it, root, Optional.empty()))
                    .isEqualTo(TreeVisitorImpl.DFS(it, root, Optional.empty()))
                    .isEqualTo(DFSCaller(it, root, Optional.empty(), true).resolve())
                    .isEqualTo(DFSCaller(it, root, Optional.empty(), false).resolve())
                    .isEqualTo(TreeVisitorImpl.PostOrder(it, root, Optional.empty()))
                    .isEqualTo(PostOrderCaller(it, root, Optional.empty(), true).resolve())
                    .isEqualTo(PostOrderCaller(it, root, Optional.empty(), false).resolve())
                    .isEqualTo(TreeVisitorImpl.PostOrderIterative(it, root, Optional.empty()));

        }

    }

    @Test
    public void graphGenerationTestCollect() {
        RandomDistribution rng = RandomDistribution.uniform(new Random());

        Orgraph tree = generateTree(rng.nextInt(50, 100), rng.nextInt(30, 40));
        GNode root = tree.getNode(0).get();

        for (int i = 0; i < 200; i++) {
            Integer id = rng.nextInt(1, tree.nodes.size());
            List<Long> list_1 = new ArrayList<>();
            List<Long> list_2 = new ArrayList<>();
            List<Long> list_3 = new ArrayList<>();
            List<Long> list_4 = new ArrayList<>();
            TreeVisitorImpl.DFS(treeVisitor(tree, id, list_1), root, Optional.empty());
            TreeVisitorImpl.DFSIterative(treeVisitor(tree, id, list_2), root, Optional.empty());
            DFSCaller(treeVisitor(tree, id, list_3), root, Optional.empty(), true).resolve();
            DFSCaller(treeVisitor(tree, id, list_4), root, Optional.empty(), false).resolve();

            assertThat(list_1)
                    .isEqualTo(list_2)
                    .isEqualTo(list_3)
                    .isEqualTo(list_4);
        }
        for (int i = 0; i < 200; i++) {
            Integer id = rng.nextInt(1, tree.nodes.size());
            List<Long> list_1 = new ArrayList<>();
            List<Long> list_2 = new ArrayList<>();
            List<Long> list_3 = new ArrayList<>();
            List<Long> list_4 = new ArrayList<>();
            TreeVisitorImpl.PostOrder(treeVisitor(tree, id, list_1), root, Optional.empty());
            TreeVisitorImpl.PostOrderIterative(treeVisitor(tree, id, list_2), root, Optional.empty());
            PostOrderCaller(treeVisitor(tree, id, list_3), root, Optional.empty(), true).resolve();
            PostOrderCaller(treeVisitor(tree, id, list_4), root, Optional.empty(), false).resolve();

            assertThat(list_1)
                    .isEqualTo(list_2)
                    .isEqualTo(list_3)
                    .isEqualTo(list_4);
        }

    }

    public static TreeVisitor<GNode> treeVisitor(Orgraph gr, long id) {
        return new TreeVisitor<GNode>() {
            @Override
            public Boolean find(GNode item) {
                return item.ID == id;
            }

            @Override
            public ReadOnlyIterator<GNode> getChildrenIterator(GNode item) {
                return ReadOnlyIterator.of(gr.resolveLinkedTo(item, i -> true))
                        .map(link -> link.nodeFrom == item.ID ? gr.getNode(link.nodeTo) : gr.getNode(link.nodeFrom))
                        .map(m -> m.get());
            }
        };
    }

    public static TreeVisitor<GNode> treeVisitor(Orgraph gr, long id, List<Long> list) {
        return new TreeVisitor<GNode>() {
            @Override
            public Boolean find(GNode item) {
                list.add(item.ID);
                return item.ID == id;
            }

            @Override
            public ReadOnlyIterator<GNode> getChildrenIterator(GNode item) {
                return ReadOnlyIterator.of(gr.resolveLinkedTo(item, i -> true))
                        .map(link -> link.nodeFrom == item.ID ? gr.getNode(link.nodeTo) : gr.getNode(link.nodeFrom))
                        .map(m -> m.get());
            }
        };
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

    public static <T> Caller<Optional<T>> PostOrderCaller(TreeVisitor<T> visitor, T root, Optional<Collection<T>> visited, boolean lazy) {
        Optional<Caller<Optional<T>>> check = visitedCheckCaller(root, visited);
        if (check.isPresent()) {
            return check.get();
        }
        return new CallerForBuilderSimple<T, Optional<T>>(visitor.getChildrenIterator(root))
                .forEachCall(item -> PostOrderCaller(visitor, item, visited, lazy))
                .evaluate(lazy, item -> item.isPresent() ? Caller.ofResult(item).toForEnd() : Caller.forContinue())
                .afterwards(Caller.ofResult(Optional.empty()))
                .toCallerBuilderAsDep()
                .toCall(args -> {
                    Optional<T> result = args.get(0);
                    if (result.isPresent()) {
                        return Caller.ofResult(result);
                    } else {
                        return Caller.ofResult(Optional.ofNullable(root).filter(visitor::find));
                    }
                });
    }

    private static <T> Optional<Caller<Optional<T>>> visitedCheckCaller(T node, Optional<Collection<T>> visited) {
        if (visited.isPresent()) {
            Collection<T> get = visited.get();
            if (get.contains(node)) {
                return Optional.of(Caller.ofResult(Optional.empty())); // prevent looping
            } else {
                get.add(node);
            }
        }
        return Optional.empty();
    }

    public static <T> Caller<Optional<T>> DFSCaller(TreeVisitor<T> visitor, T root, Optional<Collection<T>> visited, boolean lazy) {
        Optional<Caller<Optional<T>>> check = visitedCheckCaller(root, visited);
        if (check.isPresent()) {
            return check.get();
        }

        if (visitor.find(root)) {
            return Caller.ofResult(Optional.ofNullable(root));
        } else {
            return new CallerForBuilderSimple<T, Optional<T>>(visitor.getChildrenIterator(root))
                    .forEachCall((i, item) -> DFSCaller(visitor, item, visited, lazy))
                    .evaluate(lazy, item -> item.isPresent() ? Caller.ofResult(item).toForEnd() : Caller.forContinue())
                    .afterwards(Caller.ofResult(Optional.empty()));

        }

    }

}
