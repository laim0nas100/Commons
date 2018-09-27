/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.graphtheory.longestpath.genetic;

import java.util.*;
import java.util.stream.Collectors;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.UUIDgenerator;
import lt.lb.commons.containers.Pair;
import lt.lb.commons.graphtheory.*;
import lt.lb.commons.misc.F;
import lt.lb.commons.reflect.DefaultFieldFactory;
import lt.lb.commons.reflect.FieldFactory;

/**
 *
 * @author laim0nas100
 */
public class GeneticSolution {

    FieldFactory factory = new DefaultFieldFactory();

    public static class GraphGenome {

        public String ID;
        public Set<Long> nodes;
        public List<Long> path;

        public int pathSimpleLength() {
            return path.size();
        }

        public GraphGenome(Collection<Long> path) {
            ID = UUIDgenerator.nextUUID("GraphGenome");
            nodes = new HashSet<>(path);
            this.path = new ArrayList<>(path);
        }
    }

    public static GraphGenome megredGenome(List<Long> first, Pair<Long> middle, List<Long> last) {
        List<Long> list = new ArrayList<>();
        list.addAll(first);
        list.add(middle.g1);
        list.add(middle.g2);
        list.addAll(last);
        return new GraphGenome(list);
    }

    public static List<GraphGenome> crossoverCommonLink(Orgraph gr, GraphGenome g1, GraphGenome g2, Pair<Long> bridge) {
        //assume they are valid for crossover

        if (g1.nodes.contains(bridge.g1)) {
            if (!g2.nodes.contains(bridge.g2)) {
                throw new IllegalArgumentException("cant create bridge: " + bridge);
            }
        } else if (g2.nodes.contains(bridge.g1)) {
            if (!g1.nodes.contains(bridge.g2)) {
                throw new IllegalArgumentException("cant create bridge: " + bridge.reverse());
            }
            bridge = bridge.reverse();
        } else {
            throw new IllegalArgumentException("cant create bridge: " + bridge);
        }

        Pair<Long> b = bridge;

        int cut1 = F.iterate(g1.path, (i, l) -> {
                         return Objects.equals(l, b.g1);
                     }).get().g1;
        int cut2 = F.iterate(g2.path, (i, l) -> {
                         return Objects.equals(l, b.g2);
                     }).get().g1;

        List<Long>[] subpaths = ArrayOp.replicate(4, List.class, () -> new ArrayList<>());
        //cut1

        F.iterate(g1.path, (i, l) -> {
              if (i < cut1) {
                  subpaths[0].add(l);
              } else if (i > cut1) {
                  subpaths[1].add(l);
              }
          });
        F.iterate(g2.path, (i, l) -> {
              if (i < cut2) {
                  subpaths[2].add(l);
              } else if (i > cut2) {
                  subpaths[3].add(l);
              }
          });

        /*
         * children
         * 0 + link + 2
         * 0 + link + 3
         * 1 + link + 2
         * 1 + link + 3
         */
        List<GraphGenome> children = new ArrayList<>();
        children.add(megredGenome(subpaths[0], b, subpaths[3]));
        children.add(megredGenome(subpaths[0], b, reversed(subpaths[2])));
        children.add(megredGenome(subpaths[2], b.reverse(), subpaths[1]));
        children.add(megredGenome(reversed(subpaths[3]), b.reverse(), subpaths[1]));

        return children;
    }

    public static <T> ArrayList<T> reversed(List<T> list) {
        ArrayList<T> reversed = new ArrayList<>();
        reversed.addAll(list);
        Collections.reverse(reversed);
        return reversed;
    }

    public static List<Long> getNodesIDs(List<GLink> path) {
        List<Long> nodes = new ArrayList<>();
        if (path.isEmpty()) {
            return nodes;
        }
        nodes.add(path.get(0).nodeFrom);
        F.iterate(path, (i, link) -> {
              nodes.add(link.nodeTo);
          });
        return nodes;
    }

    public static List<GNode> getNodes(Orgraph gr, List<GLink> path) {
        List<GNode> nodes = new ArrayList<>();
        if (path.isEmpty()) {
            return nodes;
        }
        F.iterate(getNodesIDs(path), (i, ID) -> {
              Optional<GNode> node = gr.getNode(ID);
              nodes.add(node.get());
          });
        return nodes;
    }

    public static List<Long> getIntersections(Orgraph gr, List<GLink> path1, List<GLink> path2) {
        List<GNode> nodes1 = getNodes(gr, path1);
        List<GNode> nodes2 = getNodes(gr, path2);
        Set<Long> nodeTable1 = nodes1.stream().map(n -> n.ID).collect(Collectors.toSet());
        Set<Long> nodeTable2 = nodes2.stream().map(n -> n.ID).collect(Collectors.toSet());
        return nodeTable1.stream().filter(n -> nodeTable2.contains(n)).collect(Collectors.toList());
    }

    public static List<GLink> getPossibleLinks(Orgraph gr, GNode n1, List<GNode> nodes) {
        ArrayList<GLink> links = new ArrayList<>();

        F.iterate(nodes, (i, n) -> {
              if (n1.linkedFrom.contains(n.ID)) {
                  links.add(gr.getLink(n.ID, n1.ID).get());
              }
              if (n1.linksTo.contains(n.ID)) {
                  links.add(gr.getLink(n1.ID, n.ID).get());
              }
          });
        return links;
    }

    public static List<GLink> getBridges(Orgraph gr, List<GLink> path1, List<GLink> path2) {

        List<GNode> nodes1 = getNodes(gr, path1);
        List<GNode> nodes2 = getNodes(gr, path2);

        List<GLink> bridges = new LinkedList<>();

        F.iterate(nodes1, (i, n) -> {
              bridges.addAll(getPossibleLinks(gr, n, nodes2));
          });
        F.filterDistinct(bridges, GLink::equalBidirectional);

        return bridges;

    }

}
