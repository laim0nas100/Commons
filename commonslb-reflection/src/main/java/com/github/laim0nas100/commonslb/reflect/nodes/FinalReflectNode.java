package com.github.laim0nas100.commonslb.reflect.nodes;

import com.github.laim0nas100.commonslb.reflect.FieldFactory;
import com.github.laim0nas100.commonslb.reflect.ReferenceCounter;

/**
 *
 * 
 * @author laim0nas100
 */
public class FinalReflectNode extends ReflectNode {

    public FinalReflectNode(FieldFactory fac,String name, String fieldName, Object ob, Class clz, ReferenceCounter<ReflectNode> references) {
        super(fac, name, fieldName, ob, clz, references);
        populated = true;
        fullyPopulated = true;
    }

}
