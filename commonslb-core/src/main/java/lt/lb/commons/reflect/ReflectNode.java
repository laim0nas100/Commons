/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lt.lb.commons.Getter;
import lt.lb.commons.Log;
import lt.lb.commons.Visitor;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ReflectNode {

    protected FullFieldHolder holder;

    protected ReflectNode parent;
    protected ReflectNode superClassNode;
    protected ReflectNode hides;
    protected LinkedHashMap<String, ReflectNode> children = new LinkedHashMap<>();
    protected LinkedHashMap<String, ReflectNode> values = new LinkedHashMap<>();
    protected Object obj;
    protected Class declaredClass;
    protected Class realClass;
    protected String name;
    
    protected ArrayList<ReflectNode> arrayValues;
    
    protected boolean populated = false;
    protected boolean fullyPopulated = false;

    public ReflectNode(String name, Object ob) {
        this(name, ob, ob.getClass());
    }

    public ReflectNode(String name, Object ob, Class clz) {
        this.name = name;
        this.obj = ob;
        this.declaredClass = clz;
        this.realClass = clz;
        populated = isNull();
        fullyPopulated = isNull();
        if (!isNull() && clz.isInterface()) { //get true class
            realClass = ob.getClass();
        }
        holder = new FullFieldHolder<>(realClass);
    }

    public ReflectNode(Object ob) {
        //root node
        this(ob.getClass().getSimpleName(), ob);
    }

    protected void fullPopulate() {
        if (fullyPopulated) {
            return;
        }
        try {
            populate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // populate nested children
        this.collectThroughSuperNode(n -> n.getChildren(), putHiding);

        //populate nested values
        this.collectThroughSuperNode(n -> n.getValues(), putHiding);

        this.fullyPopulated = true;

    }

    protected void populate() throws Exception {
        if (populated) {
            return;
        }
        children = new LinkedHashMap<>();
        values = new LinkedHashMap<>();
        FieldMap localFields = holder.getLocal().getFields();

        for (Map.Entry<String, Field> entry : localFields.entrySet()) {
            Field f = entry.getValue();
            boolean accessible = f.isAccessible();
            f.setAccessible(true);
            String fieldName = entry.getKey();

            Object value = f.get(obj);

            Class type = f.getType();
            if (FieldHolder.IS_COMMON.test(f)) {
                ReflectNode node = new FinalReflectNode(fieldName, value, type);
                values.put(fieldName, node);
                node.parent = this;
            } else if(type.isArray()){
                ReflectNode node = new ArrayReflectNode(fieldName, value, type);
                children.put(fieldName, node);
                node.parent = this;
            } else {
                ReflectNode node = new ReflectNode(fieldName, value, type);
                children.put(fieldName, node);
                node.parent = this;
            }
            f.setAccessible(accessible);
        }
        Class superCls = this.getRealClass().getSuperclass();
        if (superCls != null) {
            String newNodeName = this.getName() + "." + superCls.getSimpleName();

            this.superClassNode = new ReflectNode(newNodeName, this.obj, superCls);
            this.superClassNode.populate();
        }

        populated = true;

    }

    protected void setHiddenNested(ReflectNode toHide) {
        if (this.getHidden() == null) {
            this.hides = toHide;
        } else {
            this.getHidden().setHiddenNested(toHide);
        }
    }

    public Map<String, ReflectNode> getChildren() {
        try {
            populate();
        } catch (Exception ex) {
            throw new Error(ex);
        }
        return this.children;
    }

    private HashMap<String, ReflectNode> collectThroughSuperNode(NodeGetter getter, NodeVisitor visiter) {
        ReflectNode node = this;
        HashMap<String, ReflectNode> accumulator = new HashMap<>();
        while (node != null) {
            for (Map.Entry<String, ReflectNode> entry : getter.get(node).entrySet()) {
                visiter.visit(accumulator, entry);
            }
            node = node.superClassNode;

        }
        return accumulator;
    }

    public static interface NodeVisitor extends Visitor<Map<String, ReflectNode>, Map.Entry<String, ReflectNode>> {
    }

    public static interface NodeGetter extends Getter<ReflectNode, Map<String, ReflectNode>> {
    }

    private static NodeVisitor putIfAbsentVisiter = (map, entry) -> {
        map.putIfAbsent(entry.getKey(), entry.getValue());
    };

    private static NodeVisitor putHiding = (map, entry) -> {
        String key = entry.getKey();
        ReflectNode newChild = entry.getValue();
        if (map.containsKey(key)) {
            ReflectNode child = map.get(key);
            child.setHiddenNested(newChild);
        } else {
            map.put(key, newChild);
        }
    };

    public Map<String, ReflectNode> getAllChildren() {
        this.fullPopulate();
        return this.collectThroughSuperNode((ReflectNode f) -> f.getChildren(), putIfAbsentVisiter);
    }

    public Map<String, ReflectNode> getValues() {
        try {
            populate();
        } catch (Exception ex) {
            throw new Error(ex);
        }
        return this.values;
    }

    public Map<String, ReflectNode> getAllValues() {
        this.fullPopulate();
        return this.collectThroughSuperNode((ReflectNode f) -> f.getValues(), putIfAbsentVisiter);
    }

    public FieldHolder getStaticFieldHolder() {
        return this.holder.staticHolder;
    }

    public FieldHolder getLocalFieldHolder() {
        return this.holder.localHolder;
    }

    public ReflectNode getParent() {
        return parent;
    }

    public final String getName() {
        return name;
    }

    public Object getValue() {
        return this.obj;
    }

    public final Class getRealClass() {
        return realClass;
    }
    
    public final Class getDeclaredClass(){
        return this.declaredClass;
    }

    public ReflectNode getHidden() {
        this.fullPopulate();
        return this.hides;
    }

    public final boolean isNull() {
        return obj == null;
    }

    public final boolean isHiding() {
        this.fullPopulate();
        return hides != null;
    }

    public final boolean isArray() {
        return this.getRealClass().isArray();
    }

}
