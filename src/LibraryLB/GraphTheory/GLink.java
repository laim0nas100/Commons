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
    public GLink(int nodeFrom, int nodeTo, double w){
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.weight = w;
    }
    public GLink reverse(){
        return new GLink(nodeTo,nodeFrom,weight);
    }
    
    public int nodeFrom;
    public int nodeTo;
    public double weight;
    public Integer key(){
        return GLink.hashMe(nodeFrom, nodeTo);
    }
    public static Integer hashMe(Integer nodeFrom, Integer nodeTo){
        return (nodeFrom+" ** "+nodeTo).hashCode();
    }

    @Override
    public String toString(){
        return this.nodeFrom+" -"+this.weight+"->"+this.nodeTo;
    }
}
