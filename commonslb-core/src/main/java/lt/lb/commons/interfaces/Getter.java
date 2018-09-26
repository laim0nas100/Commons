/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.interfaces;

/**
 *
 * @author laim0nas100
 * @param <From> Type to inspect
 * @param <What> Type to receive
 */
public interface Getter<From, What> {

    public What get(From f);
}
