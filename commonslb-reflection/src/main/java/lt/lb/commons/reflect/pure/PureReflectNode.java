package lt.lb.commons.reflect.pure;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.F;
import lt.lb.commons.Predicates;
import lt.lb.commons.containters.caching.LazyValue;
import lt.lb.commons.reflect.FieldFactory;
import lt.lb.commons.reflect.FieldHolder;
import lt.lb.commons.reflect.FullFieldHolder;

/**
 * Saves only class info, no values
 *
 * @author laim0nas100
 */
public class PureReflectNode {

    protected Map<Class, PureReflectNode> cache;
    protected Class cls;
    protected LazyValue<PureReflectNode> superNode;
    protected LazyValue<FullFieldHolder> holder;
    protected LazyValue<Map<String, PureReflectNode>> compositeChildNodes;

    public PureReflectNode(Map<Class,PureReflectNode> cache, Class cls) {
        this.cache = cache;
        this.cls = cls;
        Objects.requireNonNull(cls);
        Objects.requireNonNull(cache);
        holder = new LazyValue<>(() -> new FullFieldHolder(cls));
        superNode = new LazyValue<>(() -> {
            Class superCls = cls.getSuperclass();
            if (superCls == null || Object.class.equals(superCls)) {
                return null;
            }
            return cache.computeIfAbsent(superCls, (c) -> {
                return new PureReflectNode(cache, superCls);
            });
        });
        compositeChildNodes = new LazyValue<>(() -> {
            Map<String, PureReflectNode> children = new HashMap<>();
            F.iterate(getLocalCompositeFields(), (name, field) -> {
                Class type = field.getType();
                PureReflectNode child = cache.computeIfAbsent(type, (c) -> {
                    return new PureReflectNode(cache, c);
                });
                children.put(name, child);
            });
            return children;
        });

    }

    public Class getFromClass() {
        return cls;
    }
    
    public PureReflectNode getSuperNode(){
        return superNode.get();
    }

    public FieldHolder.FieldMap getLocalPrimitiveOrWrapperFields() {
        return this.holder.get().getLocal().getFieldsWith(Predicates.ofMapping(FieldFactory.isJVMImmutable, m -> m.getType()));
    }

    public FieldHolder.FieldMap getLocalCompositeFields() {
        return this.holder.get().getLocal().getFieldsWith(Predicates.ofMapping(FieldFactory.isJVMImmutable.negate(), m -> m.getType()));
    }
 

}
