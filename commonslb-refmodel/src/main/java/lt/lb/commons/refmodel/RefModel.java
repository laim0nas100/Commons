/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.refmodel;

import lt.lb.commons.refmodel.jparef.ListRef;
import lt.lb.commons.refmodel.jparef.SingularRef;

/**
 *
 * @author laim0nas100
 * Marker interface when Ref is composite
 */
public interface RefModel {
    
    
    public static class ActorRef extends SingularRef<ActorRef> implements RefModel{
        public SingularRef<String> name;
        public ListRef<ActorRef> friends;
        public void ok(){
        }
    }

}
