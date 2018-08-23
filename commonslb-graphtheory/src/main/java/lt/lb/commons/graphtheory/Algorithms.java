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
import java.util.Map;

/**
 *
 * @author Lemmin
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

    public static Long getMapExtremum(Map<Long, Number> m, boolean maximise) {
        Long ID = new ArrayList<>(m.keySet()).get(0);
        Number extremum = m.get(ID);

        for (Long k : m.keySet()) {
            boolean swap = false;
            if (m.get(k) == null) {
                continue;
            }
            Number o = m.get(k);
            if (maximise) {

                swap = extremum.doubleValue() < o.doubleValue();
            } else {
                swap = extremum.doubleValue() > o.doubleValue();
            }
            if (swap) {
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
            GNode node = graph.getNode(workList.pollFirst());
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

        public HashMap<Long, Number> distanceMap = new HashMap<>();
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
                GNode parent = graph.getNode(up);
                newSet.addAll(parent.linkedFrom);
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
            GNode node = graph.getNode(nodeID);
            Double currentDist = info.distanceMap.get(nodeID).doubleValue();
            for (long neighbour : node.linksTo) {
                if (currentDist == Double.NEGATIVE_INFINITY) {
                    currentDist = new Double(0);
                }
                Double neighbourDist = info.distanceMap.get(neighbour).doubleValue();
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
            GNode node = graph.getNode(nodeID);
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

}
