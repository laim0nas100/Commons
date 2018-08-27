/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.util.Date;
import lt.lb.commons.Log;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class DefaultFieldFactory extends FieldFactory{
    
    public DefaultFieldFactory(){
        Log.print(FieldFactory.JDK_IMMUTABLE_TYPES);
        this.addImmutableType(FieldFactory.JDK_IMMUTABLE_TYPES);
        this.addExplicitClone(Date.class, (date) -> new Date(date.getTime()));
    }
    
}
