package com.github.laim0nas100.commonslb.reflect.nodes;

import com.github.laim0nas100.commonslb.reflect.FieldFactory;
import com.github.laim0nas100.commonslb.reflect.ReferenceCounter;

/**
 *
 * @author laim0nas100
 */
public class RootReflectNode extends ReflectNode {

    public RootReflectNode(FieldFactory fac, String name, String fieldName, Object ob, Class clz, ReferenceCounter<ReflectNode> references) {
        super(fac, name, fieldName, ob, clz, references);
    }

    public RootReflectNode(FieldFactory fac, Object ob) {
        super(fac, ob);
    }

    @Override
    public String getName() {
        return name;
    }

}
