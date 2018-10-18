/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.refmodel;

/**
 *
 * @author laim0nas100
 */
public class Ref<Type> {

    public String local;
    public String relative;

    public String get() {
        return relative;
    }

    public Ref() {
    }

    @Override
    public String toString() {
        return get();
    }

}
