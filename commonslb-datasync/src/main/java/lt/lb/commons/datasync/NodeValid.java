package lt.lb.commons.datasync;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public abstract class NodeValid<T, N> extends BaseValid<T> {

    public Supplier<List<N>> referenceSupl;

    public NodeValid() {
    }

    public NodeValid(Supplier<List<N>> referenceSupl, Function<? super T, String> errorSupl, Predicate<T> isValid) {
        super(errorSupl, isValid);
        this.referenceSupl = referenceSupl;
    }

}
