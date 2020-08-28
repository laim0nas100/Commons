package lt.lb.commons.datasync;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public abstract class NodeValid<T, N> implements Valid<T> {

    public Supplier<List<N>> referenceSupl;
    public Function<? super T, String> errorSupl;
    public Predicate<T> isValid;

    @Override
    public boolean isValid(T from) {
        return isValid.test(from);
    }

}
