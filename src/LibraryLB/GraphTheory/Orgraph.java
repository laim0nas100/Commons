/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.GraphTheory;

import LibraryLB.Log;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Lemmin
 */
public class Orgraph {
    public HashMap<Long,GNode> nodes;
    public HashMap<Long,GLink> links;

    public Orgraph(){
        this.links = new HashMap<>();
        this.nodes = new HashMap<>();
    }
    
    public Orgraph(double[][] matrix){
        this();
        int h = matrix.length;
        int w = matrix[0].length;
        for(int i = 0; i < h; i++){
            for(int j = 0; j < w; j++){
                this.addLink(new GLink(i,j,matrix[i][j]));
            }
        }
    }
    
    public final boolean addLink(GLink link){
        GNode n1 = this.createNodeIfAbsent(link.nodeFrom);
        GNode n2 = this.createNodeIfAbsent(link.nodeTo);
        n1.linksTo.add(n2.ID);
        n2.linkedFrom.add(n1.ID);
        boolean collision = false;
        if(this.links.containsKey(link.key())){
            Log.print("Collision");
            collision = true;
        }
        this.links.put(link.key(), link);
        return collision;
    }
    public void linkNodes(long nodeFrom, long nodeTo, double weight){
        this.addLink(new GLink(nodeFrom,nodeTo,weight));
    }
    
    public void add2wayLink(GLink link){
        this.linkNodes(link.nodeFrom, link.nodeTo, link.weight);
        this.linkNodes(link.nodeTo, link.nodeFrom, link.weight);
    } 
    
    public void removeLink(long nodeFrom, long nodeTo){
        if(nodes.containsKey(nodeFrom)){
            this.removeConnectionFromNode(nodes.get(nodeFrom), nodeTo);
        }
    }
    
    public void remove2wayLink(GLink link){
        this.removeLink(link.nodeFrom,link.nodeTo);
        this.removeLink(link.nodeTo,link.nodeFrom);
    }
    
    public void removeNode(long ID){
        if(nodes.containsKey(ID)){
            GNode removeMe = nodes.get(ID);
            ArrayList<Long> linksTo = new ArrayList<>();
            linksTo.addAll(removeMe.linksTo);
            for(Long n : linksTo){
                this.remove2wayLink(new GLink(n,ID,0));
            }
            nodes.remove(ID);
        }
    }
    
    public boolean linkExists(long nodeFrom, long nodeTo){
        return this.links.containsKey(GLink.hashMe(nodeFrom, nodeTo));
    }
    
    public Double weight(long nodeFrom, long nodeTo){
        if(linkExists(nodeFrom,nodeTo)){
            return links.get(GLink.hashMe(nodeFrom, nodeTo)).weight;
        }else{
            return null;
        }
    }
    
    public GNode getNode(long ID){
        if(nodes.containsKey(ID)){
            return nodes.get(ID);
        }else{
            return new GNode(-1);
        }
    }
    
    public boolean nodeIsLeaft(long ID){
        return nodes.containsKey(ID) && nodes.get(ID).linksTo.isEmpty();
    }
    
    private void removeConnectionFromNode(GNode nodeFrom,long linkTo){       
        nodeFrom.linksTo.remove(linkTo);
        if (nodes.containsKey(linkTo)){
            GNode other = nodes.get(linkTo);
            other.linkedFrom.remove(nodeFrom.ID);
        }
        links.remove(GLink.hashMe(nodeFrom.ID, linkTo));

    }
    private GNode createNodeIfAbsent(long id){
        if (!nodes.containsKey(id)){
            GNode newnode = new GNode(id);
            this.nodes.put(id, newnode);
            return newnode;
        }else{
            return this.nodes.get(id);
        }
    }
    
    public String toStringLinks(){
        String str = "";
        for(GLink link:links.values()){
            str += link.toString()+"\n";
        }
        return str;
    }
    
    public String toStringNodes(){
        String str = "";
        for(GNode n:nodes.values()){
            str += n.toString()+"\n";
        }
        return str;
    }  
}
