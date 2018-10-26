/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.graphtheory.paths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.graphtheory.GLink;
import lt.lb.commons.graphtheory.GNode;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.containers.tuples.Tuple3;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.misc.rng.RandomDistribution;

/**
 *
 * @author laim0nas100
 */
public class PathGenerator {

    public static ILinkPicker nearestNeighbour() {
        return (Tuple3<Orgraph, Set<Long>, GNode> f) -> {
            return f.g1.resolveLinkedTo(f.g3, n -> !f.g2.contains(n)).stream()
                    .min(GLink.Cmp.weightMinimizingCmp());
        };
    }

    public static ILinkPicker maxDegreeNeighbour() {
        return (Tuple3<Orgraph, Set<Long>, GNode> f) -> {
            Orgraph gr = f.g1;
            return gr.resolveLinkedTo(f.g3, n -> !f.g2.contains(n)).stream()
                    .max(GLink.Cmp.nodeDegreeMinimizingCmp(gr).reversed());
        };
    }

    public static ILinkPicker nodeDegreeDistributed(RandomDistribution rnd) {
        return (Tuple3<Orgraph, Set<Long>, GNode> f) -> {
            Orgraph gr = f.g1;
            ArrayList<Tuple<Integer, Long>> nodeList = new ArrayList<>();
            F.iterate(gr.resolveLinkedTo(f.g3, n -> !f.g2.contains(n)), (i, n) -> {
                Optional<GNode> node = gr.getNode(n.nodeTo);
                if (node.isPresent()) {
                    nodeList.add(new Tuple<>(node.get().degree(), node.get().ID));
                }
            });

            if (nodeList.isEmpty()) {
                return Optional.empty();
            }
            Long nodeTo = rnd.pickRandomDistributed(1, nodeList).getFirst();

            return gr.getLink(f.g3.ID, nodeTo);
        };
    }

    public interface ILinkPicker extends Function<Tuple3<Orgraph, Set<Long>, GNode>, Optional<GLink>> {
    }

    public static List<GLink> genericUniquePathVisit(Orgraph gr, long startNode, ILinkPicker picker) {
        List<GLink> path = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        genericUniquePathVisitContinued(gr, startNode, path, visited, picker);
        return path;
    }

    public static List<GLink> genericUniquePathVisitContinued(Orgraph gr, long startNode, List<GLink> path, Set<Long> visited, ILinkPicker picker) {
        Optional<GNode> node = gr.getNode(startNode);
        while (node.isPresent()) {
            visited.add(node.get().ID);
            node = genericPathVisitGetNextNode(gr, path, node.get(), visited, picker);
        }
        return path;
    }

    private static Optional<GNode> genericPathVisitGetNextNode(Orgraph gr, List<GLink> list, GNode currentNode, Set<Long> visited, ILinkPicker picker) {
        Optional<GLink> optLink = picker.apply(Tuples.of(gr, visited, currentNode));
        if (optLink.isPresent()) {
            GLink link = optLink.get();
            list.add(link);
            long nodeTo = link.nodeTo;
            return gr.getNode(nodeTo);
        } else {
            return Optional.empty();
        }
    }

    public static List<GLink> generateLongPathBidirectional(Orgraph gr, long startNode, ILinkPicker picker) {
        List<GLink> pathForward = new ArrayList<>();
        List<GLink> pathBackward = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        genericUniquePathVisitContinued(gr, startNode, pathForward, visited, picker);
        genericUniquePathVisitContinued(gr, startNode, pathBackward, visited, picker);

        List<GLink> finalList = new ArrayList<>();
        F.iterateBackwards(pathBackward, (i, item) -> {
            finalList.add(item.reverse());
        });

//        Log.print("Got path:", pathBackward, "And", pathForward, "Joined " + startNode);
        finalList.addAll(pathForward);
//        Log.print("FinalPath", finalList);

        return finalList;

    }

    public static String isPathValid(Orgraph gr, List<Long> nodes) {
        for (int i = 1; i < nodes.size(); i++) {
            Long prev = nodes.get(i - 1);
            Long n = nodes.get(i);

            if (gr.linkExists(prev, n)) {
                // all good
            } else {
                return "No such link:" + prev + " -> " + n;
            }
        }
        return "Yes";
    }

}
