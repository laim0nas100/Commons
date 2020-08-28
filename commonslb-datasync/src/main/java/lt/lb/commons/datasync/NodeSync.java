package lt.lb.commons.datasync;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * @author laim0nas100
 */
public abstract class NodeSync<P, D, N, V extends NodeValid<P, N>> extends GenDataSync<P, D, V> {

    public NodeSync(N node) {
        this(ReadOnlyIterator.of(node).toArrayList());
    }

    public NodeSync(List<N> nodes) {
        this.nodes = nodes;
    }

    public final List<N> nodes;

    /**
     * Copy validation strategy except the nodes.
     *
     * @param valid
     */
    public void addPersistValidation(V valid) {
        V v = createValidation();
        v.errorSupl = valid.errorSupl;
        v.isValid = valid.isValid;
        v.referenceSupl = () -> nodes;
        withPersistValidation(v);
    }

    /**
     * Create base class, so that we can supply with nodes, error message etc..
     *
     * @return
     */
    protected abstract V createValidation();

    public void addPersistValidation(String error, Predicate<P> isValid) {
        addPersistValidation(m -> error, isValid);
    }

    public void addPersistValidation(Function<? super P, String> error, Predicate<P> isValid) {
        V valid = createValidation();
        valid.errorSupl = error;
        valid.isValid = isValid;
        valid.referenceSupl = () -> nodes;
        withPersistValidation(valid);
    }

}
