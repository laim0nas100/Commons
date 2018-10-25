/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.graphtheory.paths;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.F;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.graphtheory.GLink;
import lt.lb.commons.graphtheory.GNode;
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

    public static void addSomeBidirectionalLinksToAllNodes(RandomDistribution rnd, Orgraph gr, int minNodeDegree, Supplier<Double> linkWeight) {
        while (true) {
            Optional<Tuple<Long, GNode>> find = F.find(gr.nodes, (ID, node) -> {
                return node.degree() < minNodeDegree;
            });
            if (!find.isPresent()) {
                return;
            }
            GraphGenerator.addSomeBidirectionalLinksToNode(rnd, gr, find.get().g2.ID, minNodeDegree, linkWeight);
        }
    }

    public static void addSomeBidirectionalLinksToNode(RandomDistribution rnd, Orgraph gr, long nodeId, int minNodeDegree, Supplier<Double> linkWeight) {
        if (gr.nodes.size() < minNodeDegree) {
            throw new IllegalArgumentException("Impossible node degree:" + minNodeDegree + " nodes:" + gr.nodes.size());
        }

        Optional<GNode> getNode = gr.getNode(nodeId);
        if (!getNode.isPresent()) {
            return;
        }
        GNode node = getNode.get();

        List<Long> candidates = new LinkedList<>(gr.nodes.keySet());
        F.filterInPlace(candidates, n -> {
            return !(node.linkedFrom.contains(n) || node.linksTo.contains(n));
        });
        
        int howMany = minNodeDegree - node.degree();
        
        F.iterate(rnd.pickRandom(candidates, howMany), (i, can) -> {
            gr.add2wayLink(new GLink(node.ID, can, linkWeight.get()));
        });

    }

}
