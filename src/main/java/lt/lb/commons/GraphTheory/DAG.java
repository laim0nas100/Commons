/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.GraphTheory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Lemmin
 */
public class DAG extends Orgraph {

    public boolean addLinkIfNoCycles(GLink link) {
        this.addLink(link);
        if (Algorithms.containsCycleBFS(this, link.nodeTo)) {
            this.removeLink(link.nodeFrom, link.nodeTo);
            return false;
        } else {
            return true;
        }
    }

    public static DAG generateRandomDAGNaive(long linkCount, long maxWeight) {
        HashSet<Long> triedLinks = new HashSet<>();
        DAG graph = new DAG();
        graph.nodes.put(0l, new GNode(0));
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        Double w = new Double(generator.nextLong(1, maxWeight));
        while (graph.links.size() < linkCount) {
            long nodeFrom = 0;
            long nodeTo = 0;
            while (nodeTo == nodeFrom) {
                nodeFrom = generator.nextInt(0, graph.nodes.size() + 1);
                nodeTo = generator.nextInt(0, graph.nodes.size() + 1);
            }

            GLink link = new GLink(nodeTo, nodeFrom, w);
            if (!triedLinks.contains(link.key())) {
                triedLinks.add(link.key());
                if (graph.addLinkIfNoCycles(link)) {
                    triedLinks.add(link.reverse().key());

                    w = new Double(generator.nextLong(1, maxWeight));
                }
            }
        }
        return graph;

    }

    public static DAG generateRandomDAGBetter(long linkCount, long maxWeight, double batchSize) {
        DAG graph = new DAG();
        graph.nodes.put(0l, new GNode(0));
        ThreadLocalRandom generator = ThreadLocalRandom.current();

        while (graph.links.size() < linkCount) {

            ArrayList<Long> possibleCandidates = new ArrayList<>();
            possibleCandidates.addAll(graph.nodes.keySet());

            HashSet<Long> parentSet;

            long nodeFrom = generator.nextInt(0, graph.nodes.size() - 1);
            parentSet = Algorithms.getParentSet(graph, nodeFrom);
            parentSet.add(nodeFrom);
            parentSet.addAll(graph.getNode(nodeFrom).linksTo);

            possibleCandidates.removeAll(parentSet);
            possibleCandidates.add((long) graph.nodes.size());

            long iterLimit = (long) Math.min(batchSize * possibleCandidates.size(), possibleCandidates.size());
            iterLimit = (long) Math.max(Math.min(linkCount - graph.links.size(), iterLimit), 1);

            Collections.shuffle(possibleCandidates);
            ArrayDeque<Long> candidates = new ArrayDeque<>(possibleCandidates);
            for (long i = 0; i < iterLimit; i++) {
                double w = generator.nextDouble(maxWeight);
                graph.addLink(new GLink(nodeFrom, candidates.removeFirst(), w));
            }
        }
        return graph;

    }

}
