/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.graphtheory;

import java.util.Comparator;
import java.util.Optional;
import java.util.Random;
import lt.lb.commons.containers.Pair;

/**
 *
 * @author laim0nas100
 */
public class GLink implements Cloneable{

    public static class Cmp {

        public static Comparator<GLink> weightMinimizingCmp() {
            return (GLink o1, GLink o2) -> Double.compare(o1.weight, o2.weight);
        }

        public static Comparator<GLink> nodeDegreeMinimizingCmp(Orgraph gr) {
            return (link1, link2) -> {
                Optional<GNode> n1 = gr.getNode(link1.nodeTo);
                Optional<GNode> n2 = gr.getNode(link2.nodeTo);
                if (!n1.isPresent()) {
                    return 1;
                }
                if (!n2.isPresent()) {
                    return -1;
                }
                return Integer.compare(n1.get().degree(), n2.get().degree());
            };

        }
        
    }

    public GLink(long nodeFrom, long nodeTo, double w) {
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.weight = w;
    }

    public GLink reverse() {
        return new GLink(nodeTo, nodeFrom, weight);
    }

    public final long nodeFrom;
    public final long nodeTo;
    public final double weight;

    public Object key() {
        return GLink.hashMe(nodeFrom, nodeTo);
    }

    public static Object hashMe(Long nodeFrom, Long nodeTo) {
        return new Pair<>(nodeFrom, nodeTo);

//        Cantor pairing function
//        Double h = 0.5;
//        return (long) (h * (nodeFrom + nodeTo) * (nodeFrom + nodeTo + 1) + nodeTo);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
    
    public static boolean equal(GLink link1, GLink link2){
        return link1.nodeFrom == link2.nodeFrom && link1.nodeTo == link2.nodeTo;
    }
    
    public static boolean equalBidirectional(GLink link1, GLink link2){
        return equal(link1,link2) || equal(link1.reverse(),link2);
    }
    
    

    @Override
    public String toString() {
        return this.nodeFrom + " -" + this.weight + "->" + this.nodeTo;
    }
}
