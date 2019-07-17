package regression.core;

import empiric.core.StackOverflowTest.RecursionBuilder;
import java.math.BigInteger;
import java.util.Random;
import java.util.stream.Stream;
import lt.lb.commons.graphtheory.GNode;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
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
            TreeVisitor<GNode> it = treeVisitor(tree, rng.nextInt(tree.nodes.size()));

            assertThat(it.BFS(root))
                    .isEqualTo(it.DFS(root))
                    .isEqualTo(it.PosOrder(root))
                    .isEqualTo(it.DFSIterative(root))
                    .isEqualTo(it.PosOrderIterative(root));

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
