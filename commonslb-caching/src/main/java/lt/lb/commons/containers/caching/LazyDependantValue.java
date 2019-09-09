package lt.lb.commons.containers.caching;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 * 
 * LazyValue, but with dependant connections
 */
public class LazyDependantValue<T> extends LazyValue<T> {

    public LazyDependantValue(Supplier<T> supply) {
        super(supply);
    }

    public LazyDependantValue(T value) {
        super(value);
    }

    /**
     * Creates a new dependant child that has to be always updated (or reupdated) after this one
     * @param <U>
     * @param <L>
     * @param child
     * @return 
     */
    public <U, L extends LazyValue<U>> L createDependantChild(L child) {
        LazyDependantValue<T> me = this;
        Supplier<Boolean> oldLoader = child.loader;
        child.loader = () -> oldLoader.get() && me.loader.get() && (me.loaded <= child.loaded);
        Supplier<U> sup = child.supply;
        child.supply = () -> {
            me.get();
            return sup.get();
        };
        return child;
    }

    
    /**
     * Create explicit mapping that updates child when parent is updated
     * @param <U>
     * @param mapper
     * @return 
     */
    public <U> LazyDependantValue<U> map(Function<? super T, ? extends U> mapper) {
        LazyDependantValue<T> me = this;
        LazyDependantValue<U> lazyDepVal = new LazyDependantValue<>(() -> mapper.apply(me.get()));
        return createDependantChild(lazyDepVal);
    }

}
