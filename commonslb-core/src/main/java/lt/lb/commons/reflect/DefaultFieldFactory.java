/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.util.Date;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class DefaultFieldFactory extends FieldFactory{
    
    public DefaultFieldFactory(){
        this.addImmutableType(FieldFactory.JVM_IMMUTABLE_TYPES);
        this.addExplicitClone(Date.class, (date) -> new Date(date.getTime()));
    }
    
}
