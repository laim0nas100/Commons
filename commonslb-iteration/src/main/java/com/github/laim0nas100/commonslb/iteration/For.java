package com.github.laim0nas100.commonslb.iteration;

import com.github.laim0nas100.commonslb.iteration.general.IterationIterable;
import com.github.laim0nas100.commonslb.iteration.general.IterationIterableUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.IterationMap;
import com.github.laim0nas100.commonslb.iteration.general.IterationMapUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.impl.ImmutableSimpleIterationIterable;
import com.github.laim0nas100.commonslb.iteration.general.impl.ImmutableSimpleMapIterable;
import com.github.laim0nas100.commonslb.iteration.general.impl.SimpleIterationIterable;
import com.github.laim0nas100.commonslb.iteration.general.impl.SimpleMapIterable;
import com.github.laim0nas100.commonslb.iteration.general.impl.unchecked.ImmutableSimpleIterationIterableUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.impl.unchecked.ImmutableSimpleMapIterableUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.impl.unchecked.SimpleIterationIterableUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.impl.unchecked.SimpleMapIterableUnchecked;

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
