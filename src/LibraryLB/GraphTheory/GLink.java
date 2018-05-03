/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.GraphTheory;

/**
 *
 * @author Lemmin
 */
public class GLink {

    public GLink(long nodeFrom, long nodeTo, double w) {
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.weight = w;
    }

    public GLink reverse() {
        return new GLink(nodeTo, nodeFrom, weight);
    }

    public long nodeFrom;
    public long nodeTo;
    public double weight;

    public Long key() {
        return GLink.hashMe(nodeFrom, nodeTo);
    }

    public static Long hashMe(Long nodeFrom, Long nodeTo) {
//        Cantor pairing function
        Double h = 0.5;
        return (long) (h * (nodeFrom + nodeTo) * (nodeFrom + nodeTo + 1) + nodeTo);
//        return (nodeFrom+" ** "+nodeTo).hashCode();
    }

    @Override
    public String toString() {
        return this.nodeFrom + " -" + this.weight + "->" + this.nodeTo;
    }
}
