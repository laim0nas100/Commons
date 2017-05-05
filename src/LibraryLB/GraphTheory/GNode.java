/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.GraphTheory;

import java.util.HashSet;

/**
 *
 * @author Lemmin
 */
public class GNode {
    public GNode(int id){
        this.ID = id;
        this.linkedFrom = new HashSet<>();
        this.linksTo = new HashSet<>();
    }
    public HashSet<Integer> linksTo;
    public HashSet<Integer> linkedFrom;
    public int ID;
    
    @Override
    public String toString(){
        String linkStr = "{}";
        String linkedFromStr = "{}";
        if(!linksTo.isEmpty()){
            linkStr = linksTo.toString();
        }
        if(!linkedFrom.isEmpty()){
            linkedFromStr = linkedFrom.toString();
        }
        return ID + " -> "+ linkStr+" : "+linkedFromStr;
    }
}
