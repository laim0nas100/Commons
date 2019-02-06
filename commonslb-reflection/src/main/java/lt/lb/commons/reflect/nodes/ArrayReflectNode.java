/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect.nodes;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lt.lb.commons.reflect.FieldFactory;
import lt.lb.commons.reflect.ReferenceCounter;

/**
 *
 * @author laim0nas100
 */
public class ArrayReflectNode extends ReflectNode {

    private Class componentType;

    public ArrayReflectNode(FieldFactory fac, String name, String fieldName, Object ob, Class clz, ReferenceCounter<ReflectNode> references) {
        super(fac, name, fieldName, ob, clz, references);
        componentType = this.getRealClass().getComponentType();
        //populate array
        if (!this.isArray() || componentType == null) {
            throw new IllegalArgumentException(ob + " with class" + clz + " is not an array");
        }

    }

    @Override
    protected void populate() throws Exception {
        if (this.populated) {
            return;
        }
        int length = Array.getLength(this.getValue());

        Map<String, ReflectNode> map = this.values;
        boolean isImmutable = FieldFactory.isJVMImmutable.test(componentType);
        if (!isImmutable) {
            map = this.children;
        }
        Class realComponentClass = null;
        for (int i = 0; i < length; i++) {
            Object get = Array.get(this.getValue(), i);
            if(get == null){
                ReflectNode node = new FinalReflectNode(factory,this.getName() + ":" + i, null, get, componentType, this.references);
                node.parent = this;
                map.put(""+i, node);
                continue;
            }
            if (realComponentClass == null) {
                realComponentClass = get.getClass();
            }

            ReflectNode node;
            if (factory.isImmutable(realComponentClass)) { // found common type
                node = new FinalReflectNode(factory,this.getName() + ":" + i, null, get, realComponentClass, this.references);
            } else {
                node = new ReflectNode(factory, this.getName() + ":" + i, null, get, realComponentClass, this.references);
            }
            node.parent = this;
            map.put("" + i, node);
        }
        this.populated = true;
    }

    public Class getComponentType() {
        return this.componentType;
    }
    
    public Collection<String> getAllValuesKeys(){
        ArrayList<String> list = new ArrayList<>();
        list.addAll(super.getAllValuesKeys());
        Collections.sort(list, (s1,s2)->{
            int i1 = Integer.parseInt(s1);
            int i2 = Integer.parseInt(s2);
            return i1 - i2;
            
        });
        return list;
    }
    
     public Collection<String> getAllChildrenKeys(){
        ArrayList<String> list = new ArrayList<>();
        list.addAll(super.getAllChildrenKeys());
        Collections.sort(list, (s1,s2)->{
            int i1 = Integer.parseInt(s1);
            int i2 = Integer.parseInt(s2);
            return i1 - i2;
            
        });
        return list;
    }

}
