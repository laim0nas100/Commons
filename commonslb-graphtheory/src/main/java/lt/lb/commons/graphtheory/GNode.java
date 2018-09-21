/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.graphtheory;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Lemmin
 */
public class GNode implements Cloneable{

    public GNode(Number id) {
        this.ID = id.longValue();
    }
    public Set<Long> linksTo = new HashSet<>();
    public Set<Long> linkedFrom = new HashSet<>();
    
    public int degree(){
        return linksTo.size();
    }
    public final long ID;
    
    @Override
    public String toString() {
        String linkStr = "{}";
        String linkedFromStr = "{}";
        if (!linksTo.isEmpty()) {
            linkStr = linksTo.toString();
        }
        if (!linkedFrom.isEmpty()) {
            linkedFromStr = linkedFrom.toString();
        }
        return ID + " -> " + linkStr + " : " + linkedFromStr;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException{
        GNode clone = (GNode) super.clone();
        clone.linkedFrom = (Set<Long>) ((HashSet<Long>)this.linkedFrom).clone();
        clone.linksTo = (Set<Long>) ((HashSet<Long>)this.linksTo).clone();
        return clone;
    }
    
    
}
