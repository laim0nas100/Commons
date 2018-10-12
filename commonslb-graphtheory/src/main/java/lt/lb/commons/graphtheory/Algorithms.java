/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.graphtheory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lt.lb.commons.ArrayOp;

/**
 *
 * @author laim0nas100
 */
public class Algorithms {

    public static <K> ArrayList<K> iterateMapKeys(Map<K, K> m, K start, K endKey) {
        ArrayList<K> path = new ArrayList<>();
        K currentKey = start;
        while (currentKey != endKey) {
            path.add(currentKey);
            currentKey = m.get(currentKey);
        }
        return path;
    }

    public static <T, V extends Comparable> T getMapExtremum(Map<T, V> m, boolean maximise) {
        T ID = new ArrayList<>(m.keySet()).get(0);
        V extremum = m.get(ID);

        for (T k : m.keySet()) {
            if (m.get(k) == null) {
                continue;
            }
            V o = m.get(k);
            int cmp = extremum.compareTo(o);

            if ((maximise && cmp > 0) || (!maximise && cmp < 0)) {
                ID = k;
                extremum = m.get(k);
            }

        }
        return ID;
    }

    public static Map<Long, Double> bellmanFord(Orgraph graph, long sourceID) {
        HashMap<Long, Double> distanceMap = new HashMap<>();
        for (GNode node : graph.nodes.values()) {
            distanceMap.put(node.ID, Double.POSITIVE_INFINITY);
        }
        distanceMap.put(sourceID, new Double(0));

        long iterationMax = distanceMap.size();

        for (long i = 0; i < iterationMax; i++) {
            boolean changesMade = false;
            for (GNode node : graph.nodes.values()) {
//                # prlong(node)
                Double nodeValue = distanceMap.get(node.ID);
//                # print(str(nodeValue) + " "+str(node.ID))
                if (nodeValue >= 0) {
                    for (long neighbour : node.linksTo) {
                        Double distanceValue = distanceMap.get(neighbour);
                        Double newLinkValue = nodeValue + graph.weight(node.ID, neighbour);
//                        # print(linkValue)
                        if (distanceValue > newLinkValue) {
                            distanceMap.put(neighbour, newLinkValue);
                            changesMade = true;
                        }
                    }
                }
            }
            if (!changesMade) {
//                # print("Iterations: "+str(i+1))
                break;
            }
        }
        return distanceMap;
    }

    public static ArrayList<Long> topologicalSortKahn(Orgraph graph) {
        ArrayDeque<Long> workList = new ArrayDeque<>();
        ArrayList<Long> order = new ArrayList<>();
//        # calculate in-degree for each Node
        HashMap<Long, Long> degreeMap = new HashMap<>();
        for (GNode node : graph.nodes.values()) {
            long degree = node.linkedFrom.size();
            degreeMap.put(node.ID, degree);
            if (degree == 0) {
                workList.add(node.ID);
            }

        }
        while (!workList.isEmpty()) {
            Optional<GNode> optNode = graph.getNode(workList.pollFirst());
            if (!optNode.isPresent()) {
                break;
            }
            GNode node = optNode.get();
            order.add(node.ID);
            for (long nextNodeID : node.linksTo) {
                degreeMap.put(nextNodeID, degreeMap.get(nextNodeID) - 1);
                if (degreeMap.get(nextNodeID) == 0) {
                    workList.add(nextNodeID);
                }
            }
        }
        return order;
    }

    public static class CriticalPathInfo {

        public HashMap<Long, Double> distanceMap = new HashMap<>();
        public HashMap<Long, Long> pathMap = new HashMap<>();
        public long startAt;
    }

    public static HashSet<Long> getParentSet(Orgraph graph, long node) {
        HashSet<Long> newSet = new HashSet<>();
        HashSet<Long> visitNextIteration = new HashSet<>();
        HashSet<Long> parentSet = new HashSet<>();
        visitNextIteration.add(node);
        while (!visitNextIteration.isEmpty()) {
            newSet.clear();

            for (long up : visitNextIteration) {
                if (parentSet.contains(up)) {
                    continue;
                }
                parentSet.add(up);
                Optional<GNode> parent = graph.getNode(up);
                if (parent.isPresent()) {
                    newSet.addAll(parent.get().linkedFrom);
                }

            }
            visitNextIteration.clear();
            visitNextIteration.addAll(newSet);

        }
        return parentSet;
    }

    public static CriticalPathInfo criticalPath(Orgraph graph, ArrayList<Long> order) {
        CriticalPathInfo info = new CriticalPathInfo();

        for (long k : graph.nodes.keySet()) {
            info.distanceMap.put(k, Double.NEGATIVE_INFINITY);
            info.pathMap.put(k, null);
        }

        for (long nodeID : order) {
            Optional<GNode> optNode = graph.getNode(nodeID);
            if (!optNode.isPresent()) {
                continue;
            }
            GNode node = optNode.get();
            Double currentDist = info.distanceMap.get(nodeID);
            for (long neighbour : node.linksTo) {
                if (currentDist == Double.NEGATIVE_INFINITY) {
                    currentDist = new Double(0);
                }
                Double neighbourDist = info.distanceMap.get(neighbour);
                Double tryNewDist = currentDist + graph.weight(nodeID, neighbour);
                if (neighbourDist < tryNewDist) {
                    info.distanceMap.put(neighbour, tryNewDist);
                    info.pathMap.put(neighbour, nodeID);
                }
            }
        }
        info.startAt = Algorithms.getMapExtremum(info.distanceMap, true);
        return info;
    }

    public static boolean containsCycleBFS(Orgraph graph, Long startingNode) {
        HashSet<Long> visited = new HashSet<>();
        ArrayDeque<Long> visitNextIteration = new ArrayDeque<>();
        ArrayDeque<Long> newList = new ArrayDeque<>();
        if (startingNode == null) {
            for (long n : graph.nodes.keySet()) {
                if (containsCycleBFS(graph, n)) {
                    return true;
                }
            }
            return false;
        } else {
            visitNextIteration.add(startingNode);
        }

        //propagate
        while (!visitNextIteration.isEmpty()) {
            long nodeID = visitNextIteration.pollFirst();
            visited.add(nodeID);
            Optional<GNode> optNode = graph.getNode(nodeID);
            if (!optNode.isPresent()) {
                continue;
            }
            GNode node = optNode.get();
            for (long link : node.linksTo) {
                if (visited.contains(link)) {
                } else if (link == startingNode) {
                    return true;
                } else {
                    newList.add(link);
                }
            }
            if (visitNextIteration.isEmpty()) {
                ArrayDeque<Long> temp = newList;
                newList = visitNextIteration;
                visitNextIteration = temp;
            }
        }
        return false;
    }

    public static Double getPathWeight(List<GLink> path) {
        return path.stream().mapToDouble(l -> l.weight).sum();
    }

    public static Double getPathWeight(List<Long> path, Orgraph gr) {

        Long[] array = ArrayOp.newArray(path, Long.class);
        double sum = 0d;
        for (int i = 1; i < array.length; i++) {
            long from = array[i - 1];
            long to = array[i];
            Optional<GLink> link = gr.getLink(from, to);
            if (link.isPresent()) {
                sum += link.get().weight;
            } else {
                throw new IllegalArgumentException("No such link " + from + " -> " + to);
            }
        }
        return sum;

    }

}
