package lt.lb.commons.iteration.general.impl;

import lt.lb.commons.iteration.general.IterationAbstract;
import lt.lb.commons.iteration.general.accessors.AccessorResolver;
import lt.lb.commons.iteration.general.accessors.DefaultAccessorResolver;
import lt.lb.commons.iteration.general.accessors.IterIterableAccessor;
import lt.lb.commons.iteration.general.accessors.IterMapAccessor;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;

/**
 *
 * @author laim0nas100
 */
public abstract class SimpleAbstractIteration<E extends SimpleAbstractIteration<E>> implements IterationAbstract<E> {

    protected int onlyIncludingLast = -1;
    protected int onlyIncludingFirst = -1;

    @Override
    public E first(int amountToInclude) {
        this.onlyIncludingFirst = amountToInclude;
        this.onlyIncludingLast = -1;
        return me();
    }

    @Override
    public E last(int amountToInclude) {
        this.onlyIncludingLast = amountToInclude;
        this.onlyIncludingFirst = -1;
        return me();
    }

    protected abstract E me();
    
    protected AccessorResolver accessorResolver = new DefaultAccessorResolver();
    
    protected AccessorResolver getResolver(){
        return accessorResolver;
    }


    protected IterIterableAccessor resolveAccessor(IterIterableCons iter) {
        return getResolver().resolveAccessor(iter);
    }

    protected IterMapAccessor resolveAccessor(IterMapCons iter) {
       return getResolver().resolveAccessor(iter);
    }
}
