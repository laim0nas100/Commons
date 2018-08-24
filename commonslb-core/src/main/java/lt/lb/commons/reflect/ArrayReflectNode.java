/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import lt.lb.commons.ArrayOp;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ArrayReflectNode extends ReflectNode {

    private Class componentType;

    public ArrayReflectNode(String name, Object ob, Class clz) {
        super(name, ob, clz);
        componentType = this.getRealClass().getComponentType();
        //populate array
        if (!this.isArray() || componentType == null) {
            throw new IllegalArgumentException(ob + " with class" + clz + " is not an array");
        }

    }
    
    @Override
    protected void populate() throws Exception{
        if(this.populated){
            return;
        }
        int length = Array.getLength(this.getValue());

        Map<String, ReflectNode> map = this.values;
        int countCommon = ArrayOp.count(cl -> cl.equals(componentType), FieldHolder.COMMON_TYPES);
        if (countCommon == 0) {
            map = this.children;
        }
        for (int i = 0; i < length; i++) {
            Object get = Array.get(this.getValue(), i);

            ReflectNode node;
            if (countCommon > 0) { // found common type
                node = new FinalReflectNode(this.getName() + ":" + i, get, componentType);
            } else {
                node = new ReflectNode(this.getName() + ":" + i, get, componentType);
            }
            node.parent = this;
            map.put("" + i, node);
        }
        this.populated = true;
    }
    
    public Class getComponentType(){
        return this.componentType;
    }

}
