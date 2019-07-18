package regression.core;

import empiric.core.StackOverflowTest.RecursionBuilder;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
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
                    .isEqualTo(TreeVisitorImpl.DFSCaller(it, root, Optional.empty(), true).resolve())
                    .isEqualTo(TreeVisitorImpl.DFSCaller(it, root, Optional.empty(), false).resolve())
                    .isEqualTo(TreeVisitorImpl.PostOrder(it, root, Optional.empty()))
                    .isEqualTo(TreeVisitorImpl.PostOrderCaller(it, root, Optional.empty(), true).resolve())
                    .isEqualTo(TreeVisitorImpl.PostOrderCaller(it, root, Optional.empty(), false).resolve())
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
            TreeVisitorImpl.DFSCaller(treeVisitor(tree, id, list_3), root, Optional.empty(), true).resolve();
            TreeVisitorImpl.DFSCaller(treeVisitor(tree, id, list_4), root, Optional.empty(), false).resolve();

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
            TreeVisitorImpl.PostOrderCaller(treeVisitor(tree, id, list_3), root, Optional.empty(), true).resolve();
            TreeVisitorImpl.PostOrderCaller(treeVisitor(tree, id, list_4), root, Optional.empty(), false).resolve();

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

}
