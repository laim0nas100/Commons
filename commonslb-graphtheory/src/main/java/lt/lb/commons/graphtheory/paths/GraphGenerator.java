/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.graphtheory.paths;

import java.util.HashSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.misc.F;
import lt.lb.commons.misc.RandomDistribution;

/**
 *
 * @author laim0nas100
 */
public class GraphGenerator {

    public static void generateSimpleConnected(RandomDistribution rnd, Orgraph gr, int nodeCount, Supplier<Double> linkWeight) {
        HashSet<Long> unconnected = new HashSet<>();
        HashSet<Long> connected = new HashSet<>();
        for (int i = 0; i < nodeCount; i++) {
            long id = i;
            gr.nodes.put(id, gr.newNode(id));
            unconnected.add(id);
        }
        unconnected.remove(0L);
        connected.add(0L);

        //connect 'em all up
        while (!unconnected.isEmpty()) {
            Long from = rnd.pickRandom(connected);
            Long to = rnd.removeRandom(unconnected);
            connected.add(to);
            gr.add2wayLink(gr.newLink(from, to, linkWeight.get()));
        }
    }

    public static void densify(RandomDistribution rnd, Orgraph gr, int minNodeDegree, Supplier<Double> linkWeight) {
        if (gr.nodes.size() < minNodeDegree) {
            throw new IllegalArgumentException("Impossible node degree:" + minNodeDegree + " nodes:" + gr.nodes.size());
        }
        F.find(gr.nodes, (ID, node) -> {
            int tryCount = 10000;
            Predicate<Long> suitableNode = (n) -> {
                return !(n.equals(ID) || node.linksTo.contains(n));
            };
            while (node.degree() < minNodeDegree) {
                boolean found = false;
                Long pickRandom = null;
                while (!found) {
                    pickRandom = rnd.pickRandom(gr.nodes.keySet());
                    found = suitableNode.test(pickRandom);
                    if (tryCount < 0) {
                        return false;
                    }
                    tryCount--;
                }
                gr.newLink(ID, pickRandom, linkWeight.get());
                if (tryCount < 0) {
                    return false;
                }
                tryCount--;
            }
            return false;
        });

    }

}
