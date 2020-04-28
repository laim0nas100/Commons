package lt.lb.commons.reflect.nodes;

import lt.lb.commons.reflect.FieldFactory;
import lt.lb.commons.reflect.ReferenceCounter;

/**
 *
 * @author laim0nas100
 */
public class RepeatedReflectNode extends FinalReflectNode {

    protected ReflectNode ref;

    public RepeatedReflectNode(FieldFactory fac, String name, String fieldName, Object ob, Class clz, ReflectNode ref, ReferenceCounter<ReflectNode> references) {
        super(fac, name, fieldName, ob, clz, references);
        this.ref = ref;
        this.repeated = true;
        this.children = ref.children;
        this.values = ref.values;
        this.holder = ref.holder;
        this.obj = ref.obj;
    }

    public ReflectNode getRef() {
        return ref;
    }

}
