package lt.lb.commons.iteration.general.accessors.unchecked;

import lt.lb.commons.iteration.general.accessors.*;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.cons.IterMapBiCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;

/**
 *
 * @author laim0nas100
 */
public class DefaultAccessorResolverUnchecked implements AccessorResolver {

    protected IterableConsAccessor iterConsAccessor = new IterableConsAccessorUnchecked();

    protected IterableBiConsAccessor iterConsBiAccessor = new IterableBiConsAccessorUnchecked();

    protected MapConsAccessor mapConsAccessor = new MapConsAccessorUnchecked();

    protected MapBiConsAccessor mapConsBiAccessor = new MapBiConsAccessorUnchecked();

    @Override
    public IterIterableAccessor resolveAccessor(IterIterableCons iter) {
        if (iter instanceof IterIterableBiCons) {
            return iterConsBiAccessor;
        }
        if (iter instanceof IterIterableCons) {
            return iterConsAccessor;
        }

        throw new IllegalArgumentException("Failed to resolve for iteration type " + iter);
    }

    @Override
    public IterMapAccessor resolveAccessor(IterMapCons iter) {
        if (iter instanceof IterMapBiCons) {
            return mapConsBiAccessor;
        }
        if (iter instanceof IterMapCons) {
            return mapConsAccessor;
        }

        throw new IllegalArgumentException("Failed to resolve for iteration type " + iter);
    }

}
