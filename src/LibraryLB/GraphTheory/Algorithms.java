/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.GraphTheory;

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
    public static <K> ArrayList<K> iterateMapKeys(Map<K,K> m,K start,K endKey){
        ArrayList<K> path = new ArrayList<>();
        K currentKey = start;
        while(currentKey != endKey){
            path.add(currentKey);
            currentKey = m.get(currentKey);
        }
        return path;
    }
    
    public static Integer getMapExtremum(Map<Integer,Number> m, boolean maximise){
        Integer ID = new ArrayList<>(m.keySet()).get(0);
        Number extremum = m.get(ID);
        
        for(Integer k:m.keySet()){
            boolean swap = false;
            if(m.get(k) == null){
                continue;
            }
            Number o = m.get(k);
            if(maximise){
                
                swap = extremum.doubleValue() < o.doubleValue();
            }else{
                swap = extremum.doubleValue() > o.doubleValue();
            }
            if(swap){
                ID = k;
                extremum = m.get(k);
            }
                
        }
        return ID;
    }
    
    public static Map<Integer,Double> bellmanFord(Orgraph graph,int sourceID){
        HashMap<Integer,Double> distanceMap = new HashMap<>();
        for (GNode node : graph.nodes.values()){
            distanceMap.put(node.ID, Double.POSITIVE_INFINITY);
        }
        distanceMap.put(sourceID,new Double(0));

        int iterationMax = distanceMap.size();
            
        for (int i=0; i<iterationMax; i++){
            boolean changesMade = false;
            for(GNode node : graph.nodes.values()){
//                # print(node)
                Double nodeValue = distanceMap.get(node.ID);
//                # print(str(nodeValue) + " "+str(node.ID))
                if (nodeValue >= 0){
                    for (int neighbour : node.linksTo){
                        Double distanceValue = distanceMap.get(neighbour);
                        Double newLinkValue = nodeValue + graph.weight(node.ID,neighbour);
//                        # print(linkValue)
                        if (distanceValue > newLinkValue){
                            distanceMap.put(neighbour, newLinkValue);
                            changesMade = true;
                        }
                    }
                }
            }
            if (! changesMade){
//                # print("Iterations: "+str(i+1))
                break;
            }
        }
        return distanceMap;
    }
    
    public static ArrayList<Integer> topologicalSortKahn(Orgraph graph){
        ArrayDeque<Integer> workList = new ArrayDeque<>();
        ArrayList<Integer> order = new ArrayList<>();
//        # calculate in-degree for each Node
        HashMap<Integer,Integer> degreeMap = new HashMap<>();
        for(GNode node : graph.nodes.values()){
            int degree = node.linkedFrom.size();
            degreeMap.put(node.ID, degree);
            if (degree == 0){
                workList.add(node.ID);
            }
                
        }
        while (!workList.isEmpty()){
            GNode node = graph.getNode(workList.pollFirst());
            order.add(node.ID);
            for (int nextNodeID : node.linksTo){
                degreeMap.put(nextNodeID, degreeMap.get(nextNodeID)-1);
                if (degreeMap.get(nextNodeID) == 0){
                    workList.add(nextNodeID);
                }     
            }
        }
        return order;
    }
    public static class CriticalPathInfo{
        public HashMap<Integer,Number> distanceMap = new HashMap<>();
        public HashMap<Integer,Integer> pathMap = new HashMap<>();
        public int startAt;
    }
    public static HashSet<Integer> getParentSet(Orgraph graph, int node){
        HashSet<Integer> newSet = new HashSet<>();
        HashSet<Integer> visitNextIteration = new HashSet<>();
        HashSet<Integer> parentSet = new HashSet<>();
        visitNextIteration.add(node);
        while(!visitNextIteration.isEmpty()){
            newSet.clear();
            
            for(int up:visitNextIteration){
                if(parentSet.contains(up)){
                    continue;
                }
                parentSet.add(up);
                GNode parent = graph.getNode(up);
                newSet.addAll(parent.linkedFrom);
            }
            visitNextIteration.clear();
            visitNextIteration.addAll(newSet);
            newSet.clear();
            
        }
        return parentSet;
    }
    
    public static CriticalPathInfo criticalPath(Orgraph graph,ArrayList<Integer> order){
        CriticalPathInfo info = new CriticalPathInfo();
        
        for(int k : graph.nodes.keySet()){
            info.distanceMap.put(k, Double.NEGATIVE_INFINITY);
            info.pathMap.put(k, null);
        }
        
        for(int nodeID :order){
            GNode node = graph.getNode(nodeID);
            Double currentDist = info.distanceMap.get(nodeID).doubleValue();
            for(int neighbour : node.linksTo){
                if(currentDist == Double.NEGATIVE_INFINITY){
                    currentDist = new Double(0);
                }
                Double neighbourDist = info.distanceMap.get(neighbour).doubleValue();
                Double tryNewDist = currentDist + graph.weight(nodeID, neighbour);
                if(neighbourDist < tryNewDist){
                    info.distanceMap.put(neighbour, tryNewDist);
                    info.pathMap.put(neighbour, nodeID);
                }
            }
        }
        info.startAt = Algorithms.getMapExtremum(info.distanceMap, true);
        return info;
    } 
    public static boolean containsCycleBFS(Orgraph graph,Integer startingNode){
        HashSet<Integer> visited = new HashSet<>();
        ArrayDeque<Integer> visitNextIteration = new ArrayDeque<>();
        ArrayDeque<Integer> newList = new ArrayDeque<>();
        if(startingNode == null){
            for(int n:graph.nodes.keySet()){
                if(containsCycleBFS(graph,n)){
                    return true;
                }
            }
            return false;
        }else{
            visitNextIteration.add(startingNode);
        }
        
        //propagate
        while(!visitNextIteration.isEmpty()){
            int nodeID = visitNextIteration.pollFirst();
            visited.add(nodeID);
            GNode node = graph.getNode(nodeID);
            for(int link :node.linksTo){
                if(visited.contains(link)){
                }
                else if(link == startingNode){
                    return true;
                }
                else{
                    newList.add(link);
                }
            }
            if(visitNextIteration.isEmpty()){
                ArrayDeque temp = newList;
                newList = visitNextIteration;
                visitNextIteration = temp;
            }
        }
        return false;
    }
    
}
