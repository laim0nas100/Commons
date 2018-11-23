/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.interfaces;

import lt.lb.commons.Lambda;

/**
 *
 * @author laim0nas100
 */
public interface Modifier<Type> extends Lambda.L1R<Type,Type>{

    public default Modifier<Type> chainNext(Modifier<Type> next) {
        Modifier<Type> me = this;
        return (Type object) -> {
            return next.apply(me.apply(object));
        };
    }

    public default Modifier<Type> chainPrev(Modifier<Type> prev) {
        Modifier<Type> me = this;
        return (Type object) -> {
            return me.apply(prev.apply(object));
        };
    }

    /**
     * Any Modifiable object can be modified with builder pattern
     * @param <ObjectType> 
     */
    public static interface Modifiable<ObjectType> {
       
        public ObjectType apply(Modifier<ObjectType> mod);
    }
}
