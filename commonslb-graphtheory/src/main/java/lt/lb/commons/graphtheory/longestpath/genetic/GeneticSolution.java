/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.graphtheory.longestpath.genetic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.UUIDgenerator;
import lt.lb.commons.containers.Pair;
import lt.lb.commons.graphtheory.GLink;
import lt.lb.commons.graphtheory.GNode;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.misc.F;
import lt.lb.commons.reflect.DefaultFieldFactory;
import lt.lb.commons.reflect.FieldFactory;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class GeneticSolution {

    FieldFactory factory = new DefaultFieldFactory();

    public static class GraphGenome {

        public String ID;
        public Set<Long> nodes;
        public List<GLink> links;

        public int pathSimpleLength() {
            return links.size();
        }

        public double pathWeightedLength() {
            return links.stream().mapToDouble(link -> link.weight).sum();
        }
        
        public GraphGenome(Collection<GLink> links){
            ID = UUIDgenerator.nextUUID("GraphGenome");
            this.links = new ArrayList<>(links);
            
        }
    }

    public static List<GraphGenome> crossoverCommonLink(Orgraph gr,GraphGenome g1, GraphGenome g2, Pair<Long> bridge) {
        //assume they are valid for crossover
        Optional<GLink> link = gr.getLink(bridge.g1, bridge.g2);
        
        
        if(g1.nodes.contains(bridge.g1)){
            if(!g2.nodes.contains(bridge.g2)){
                throw new IllegalArgumentException("cant create bridge: "+bridge);
            }
        }else if(g2.nodes.contains(bridge.g1)){
            if(!g1.nodes.contains(bridge.g2)){
                throw new IllegalArgumentException("cant create bridge: "+bridge.reverse());
            }
            bridge = bridge.reverse();
        }else{
            throw new IllegalArgumentException("cant create bridge: "+bridge);
        }
        
        Pair<Long> b = bridge;
        
        int cut1 = F.iterate(g1.links, (i,l)->{
            return l.nodeFrom == b.g1;
        }).get().g1;
        int cut2 = F.iterate(g2.links, (i,l)->{
            return l.nodeFrom == b.g2;
        }).get().g1;
        
        List<GLink>[] subpaths = ArrayOp.replicate(4, ()->new ArrayList<>());
        //cut1
        
        F.iterate(g1.links, (i,l)->{
            if(i < cut1){
                subpaths[0].add(l);
            }else{
                subpaths[1].add(l);
            }
        });
        F.iterate(g1.links, (i,l)->{
            if(i < cut2){
                subpaths[2].add(l);
            }else{
                subpaths[3].add(l);
            }
        });
        
        
        
        
        throw new UnsupportedOperationException();
        
        
        
        
        

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
    
    public static List<Long> getIntersections(Orgraph gr, List<GLink> path1, List<GLink> path2){
        List<GNode> nodes1 = getNodes(gr, path1);
        List<GNode> nodes2 = getNodes(gr, path2);
        Set<Long> nodeTable1 = nodes1.stream().map(n -> n.ID).collect(Collectors.toSet());
        Set<Long> nodeTable2 = nodes2.stream().map(n -> n.ID).collect(Collectors.toSet());
        return nodeTable1.stream().filter(n -> nodeTable2.contains(n)).collect(Collectors.toList());
    }
    
    
    
    public static List<GLink> getPossibleLinks(Orgraph gr, GNode n1, List<GNode> nodes){
        ArrayList<GLink> links = new ArrayList<>();
        
        
        F.iterate(nodes, (i,n)->{
            if(n1.linkedFrom.contains(n.ID)){
                links.add(gr.getLink(n.ID, n1.ID).get());
            }
            if(n1.linksTo.contains(n.ID)){
                links.add(gr.getLink(n1.ID, n.ID).get());
            }
        });
        return links;
    }

    public static List<GLink> getBridges(Orgraph gr, List<GLink> path1, List<GLink> path2) {

        List<GNode> nodes1 = getNodes(gr, path1);
        List<GNode> nodes2 = getNodes(gr, path2);
        
        List<GLink> bridges = new LinkedList<>();
        
        
        F.iterate(nodes1, (i,n)->{
            bridges.addAll(getPossibleLinks(gr,n,nodes2));
        });
        F.filterDistinct(bridges, GLink::equalBidirectional);
        
        return bridges;

    }

}
