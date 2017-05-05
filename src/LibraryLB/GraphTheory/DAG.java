/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.GraphTheory;
import LibraryLB.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
/**
 *
 * @author Lemmin
 */
public class DAG extends Orgraph{
    
    public boolean addLinkIfNoCycles(GLink link){
        this.addLink(link);
        if(Algorithms.containsCycleBFS(this, link.nodeTo)){
            this.removeLink(link.nodeFrom, link.nodeTo);
            return false;
        }else{
            return true;
        }
    }
    
    public static DAG generateRandomDAGNaive(int linkCount, int maxWeight){
        HashSet<Integer> triedLinks = new HashSet<>();
        DAG graph = new DAG();
        graph.nodes.put(0, new GNode(0));
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        Double w = new Double(generator.nextInt(1,maxWeight));
        while(graph.links.size()<linkCount){
            int nodeFrom = 0;
            int nodeTo = 0;
            while(nodeTo == nodeFrom){
                nodeFrom = generator.nextInt(0, graph.nodes.size()+1);
                nodeTo = generator.nextInt(0, graph.nodes.size()+1);
            }
            
            
            GLink link = new GLink(nodeTo,nodeFrom,w);
            if(!triedLinks.contains(link.key())){
                triedLinks.add(link.key());
                if(graph.addLinkIfNoCycles(link)){
                    triedLinks.add(link.reverse().key());
                    
                    w = new Double(generator.nextInt(1,maxWeight));
                }   
            }
        }
        return graph;
        
    }
    
    public static DAG generateRandomDAGBetter(int linkCount, int maxWeight){
        DAG graph = new DAG();
        graph.nodes.put(0, new GNode(0));
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        
        while(graph.links.size()<linkCount){
            
            ArrayList<Integer> possibleCandidates = new ArrayList<>();
            possibleCandidates.addAll(graph.nodes.keySet());
            
            HashSet<Integer> parentSet;
            
            Double w = new Double(generator.nextInt(1,maxWeight));
            int nodeTo = 0;
            int nodeFrom = 0;
            
            nodeFrom = generator.nextInt(0, graph.nodes.size());
            parentSet = Algorithms.getParentSet(graph, nodeFrom);
            parentSet.add(nodeFrom);
            parentSet.addAll(graph.getNode(nodeFrom).linksTo);
            parentSet.addAll(graph.getNode(nodeFrom).linkedFrom);
            possibleCandidates.removeAll(parentSet);
            possibleCandidates.add(graph.nodes.size());
            Log.print(nodeFrom,parentSet,possibleCandidates);

            nodeTo = possibleCandidates.get(generator.nextInt(possibleCandidates.size()));

            graph.addLink(new GLink(nodeFrom,nodeTo,w));
            Log.print(nodeFrom,nodeTo);
            
            
            Log.print("Size:",graph.links.size());
            
        }
//        Log.print(Algorithms.containsCycleBFS(graph, null));
        return graph;
        
    }
    
}
