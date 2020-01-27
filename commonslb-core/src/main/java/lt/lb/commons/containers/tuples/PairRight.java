package lt.lb.commons.containers.tuples;

/**
 *
 * @author laim0nas100
 */
public class PairRight<T> extends Pair<T>{

    public PairRight(T right) {
        super(null, right);
    }

    @Override
    public T getG1() {
        throw new UnsupportedOperationException("This objects only holds right side (2) values");
    }

    @Override
    public void setG1(T g1) {
        throw new UnsupportedOperationException("This objects only holds right side (2) values");
    }

    @Override
    public String toString() {
        return "PairRight{" +this.g2+ '}';
    }
    
    @Override
    public PairLeft<T> reverse() {
        return new PairLeft<>(this.g2);
    }
    
}
