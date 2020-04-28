package lt.lb.commons.reflect.nodes;

import lt.lb.commons.reflect.FieldFactory;
import lt.lb.commons.reflect.ReferenceCounter;

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
