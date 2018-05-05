/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.GraphTheory;

import java.util.HashSet;

/**
 *
 * @author Lemmin
 */
public class GNode {

    public GNode(long id) {
        this.ID = id;
    }

    public GNode(int id) {
        this.ID = id;
    }
    public HashSet<Long> linksTo = new HashSet<>();
    public HashSet<Long> linkedFrom = new HashSet<>();
    public long ID;

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
}
