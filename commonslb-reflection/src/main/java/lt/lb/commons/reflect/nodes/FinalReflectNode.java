package lt.lb.commons.reflect.nodes;

import lt.lb.commons.reflect.FieldFactory;
import lt.lb.commons.reflect.ReferenceCounter;

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
