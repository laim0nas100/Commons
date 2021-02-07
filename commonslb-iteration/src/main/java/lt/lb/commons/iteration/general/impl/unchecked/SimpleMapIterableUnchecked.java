package lt.lb.commons.iteration.general.impl.unchecked;

import java.util.Map;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.IterationMapUnchecked;
import lt.lb.commons.iteration.general.accessors.AccessorResolver;
import lt.lb.commons.iteration.general.accessors.unchecked.DefaultAccessorResolverUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterMapConsUnchecked;
import lt.lb.commons.iteration.general.impl.*;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public class SimpleMapIterableUnchecked extends SimpleAbstractIteration<SimpleMapIterableUnchecked> implements IterationMapUnchecked<SimpleMapIterableUnchecked> {

    
    protected AccessorResolver resolver = new DefaultAccessorResolverUnchecked();
    protected SimpleMapIterable main = new SimpleMapIterable(){
        @Override
        protected AccessorResolver getResolver() {
            return resolver;
        }
    
        
    };
    
    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, IterMapConsUnchecked<K, V> iter) {
        return main.find(map, iter);
    }

    @Override
    protected SimpleMapIterableUnchecked me() {
        return this;
    }

}
