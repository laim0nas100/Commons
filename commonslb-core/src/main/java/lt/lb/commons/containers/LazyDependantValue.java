package lt.lb.commons.containers;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class LazyDependantValue<T> extends LazyValue<T> {

    public LazyDependantValue(Supplier<T> supply) {
        super(supply);
    }

    public LazyDependantValue(T value) {
        super(value);
    }

    public <U, L extends LazyValue<U>> L createDependantChild(L child) {
        LazyDependantValue<T> me = this;
        child.loader = () -> me.loader.get() && me.loaded <= child.loaded;
        Supplier<U> sup = child.supply;
        child.supply = () -> {
            me.get();
            return sup.get();
        };
        return child;
    }

    public <U> LazyDependantValue<U> map(Function<? super T, ? extends U> mapper) {
        LazyDependantValue<U> lazyDepVal = new LazyDependantValue<>(() -> mapper.apply(this.get()));
        return this.createDependantChild(lazyDepVal);
    }
    

}
