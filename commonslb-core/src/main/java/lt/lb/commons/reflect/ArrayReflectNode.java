/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.lang.reflect.Array;
import java.util.Map;

/**
 *
 * @author Laimonas-Beniusis-PC
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
            if (realComponentClass == null) {
                realComponentClass = get.getClass();
            }

            ReflectNode node;
            if (factory.isImmutable(realClass)) { // found common type
                node = new FinalReflectNode(factory,this.getName() + ":" + i, null, get, componentType, this.references);
            } else {
                node = new ReflectNode(factory, this.getName() + ":" + i, null, get, componentType, this.references);
            }
            node.parent = this;
            map.put("" + i, node);
        }
        this.populated = true;
    }

    public Class getComponentType() {
        return this.componentType;
    }

}
