package lt.lb.commons.containers.tuples;

/**
 *
 * @author laim0nas100
 */
public class PairLeft<T> extends Pair<T> {

    public PairLeft(T left) {
        super(left, null);
    }

    @Override
    public T getG2() {
        throw new UnsupportedOperationException("This objects only holds left side (2) values");
    }

    @Override
    public void setG2(T g1) {
        throw new UnsupportedOperationException("This objects only holds left side (2) values");
    }

    @Override
    public String toString() {
        return "PairLeft{" + this.g1 + '}';
    }

    @Override
    public PairRight<T> reverse() {
        return new PairRight<>(this.g1);
    }

}
