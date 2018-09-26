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
public class RootReflectNode extends ReflectNode{
    
    public RootReflectNode(FieldFactory fac, String name, String fieldName, Object ob, Class clz, ReferenceCounter<ReflectNode> references) {
        super(fac, name, fieldName, ob, clz, references);
    }
    
    public RootReflectNode(FieldFactory fac, Object ob){
        super(fac,ob);
    }
    
    @Override
    public String getName(){
        return name;
    }
    
}
