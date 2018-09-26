/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

/**
 *
 * @author laim0nas100
 */
public class FullFieldHolder<T> {

    FieldHolder<T> staticHolder;
    FieldHolder<T> localHolder;

    public FullFieldHolder(Class<T> cls) {
        staticHolder = new FieldHolder<>(cls, true);
        localHolder = new FieldHolder<>(cls);
    }

    public FieldHolder getStatic() {
        return staticHolder;
    }

    public FieldHolder getLocal() {
        return localHolder;
    }
    
    public Class<T> getFromClass(){
        return localHolder.getFromClass();
    }

}
