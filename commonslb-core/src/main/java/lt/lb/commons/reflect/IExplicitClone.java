/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

/**
 *
 * @author Lemmin
 */
public interface IExplicitClone<T> {

    public T clone(T value);
}
