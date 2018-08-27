/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import lt.lb.commons.reflect.ReferenceCounter;

/**
 *
 * @author Lemmin
 */
public interface IFieldResolver {

    public void cloneField(Object source, Object parentObject, ReferenceCounter refCoounter) throws Exception;
}
