package lt.lb.commons.iteration;

import lt.lb.commons.iteration.general.impl.ImmutableSimpleIterationIterable;
import lt.lb.commons.iteration.general.impl.ImmutableSimpleMapIterable;
import lt.lb.commons.iteration.general.impl.SimpleIterationIterable;
import lt.lb.commons.iteration.general.impl.SimpleMapIterable;

/**
 *
 * @author laim0nas100
 */
public abstract class For {
    protected static final ImmutableSimpleIterationIterable immutableIterable = new ImmutableSimpleIterationIterable();
    protected static final ImmutableSimpleMapIterable immutableMapIterable = new ImmutableSimpleMapIterable();

    public static SimpleIterationIterable elements() {
        return immutableIterable;
    }
    
    public static SimpleMapIterable entries(){
        return immutableMapIterable;
    }
    
}
