package lt.lb.commons.iteration;

import lt.lb.commons.iteration.general.IterationIterable;
import lt.lb.commons.iteration.general.IterationIterableUnchecked;
import lt.lb.commons.iteration.general.IterationMap;
import lt.lb.commons.iteration.general.IterationMapUnchecked;
import lt.lb.commons.iteration.general.impl.ImmutableSimpleIterationIterable;
import lt.lb.commons.iteration.general.impl.ImmutableSimpleMapIterable;
import lt.lb.commons.iteration.general.impl.SimpleIterationIterable;
import lt.lb.commons.iteration.general.impl.SimpleMapIterable;
import lt.lb.commons.iteration.general.impl.unchecked.ImmutableSimpleIterationIterableUnchecked;
import lt.lb.commons.iteration.general.impl.unchecked.ImmutableSimpleMapIterableUnchecked;
import lt.lb.commons.iteration.general.impl.unchecked.SimpleIterationIterableUnchecked;
import lt.lb.commons.iteration.general.impl.unchecked.SimpleMapIterableUnchecked;

/**
 *
 * @author laim0nas100
 */
public abstract class For {

    protected static final ImmutableSimpleIterationIterable immutableIterable = new ImmutableSimpleIterationIterable();
    protected static final ImmutableSimpleMapIterable immutableMapIterable = new ImmutableSimpleMapIterable();

    protected static final ImmutableSimpleIterationIterableUnchecked immutableIterableUnchecked = new ImmutableSimpleIterationIterableUnchecked();
    protected static final ImmutableSimpleMapIterableUnchecked immutableMapIterableUnchecked = new ImmutableSimpleMapIterableUnchecked();

    public static IterationIterable<SimpleIterationIterable> elements() {
        return immutableIterable;
    }

    public static IterationIterableUnchecked<SimpleIterationIterableUnchecked> elementsUnchecked() {
        return immutableIterableUnchecked;
    }

    public static IterationMap<SimpleMapIterable> entries() {
        return immutableMapIterable;
    }
    
    public static IterationMapUnchecked<SimpleMapIterableUnchecked> entriesUnchecked() {
        return immutableMapIterableUnchecked;
    }

}
