package lt.lb.commons.jpa.tuple;

import java.util.List;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

/**
 *
 * @author laim0nas100
 */
public interface DelegatedTuple extends Tuple {
    
    public Tuple delegatedTuple();

    @Override
    public default <X> X get(TupleElement<X> tupleElement) {
        return delegatedTuple().get(tupleElement);
    }

    @Override
    public default <X> X get(String alias, Class<X> type) {
        return delegatedTuple().get(alias, type);
    }

    @Override
    public default Object get(String alias) {
        return delegatedTuple().get(alias);
    }

    @Override
    public default <X> X get(int i, Class<X> type) {
        return delegatedTuple().get(i, type);
    }

    @Override
    public default Object get(int i) {
        return delegatedTuple().get(i);
    }

    @Override
    public default Object[] toArray() {
        return delegatedTuple().toArray();
    }

    @Override
    public default List<TupleElement<?>> getElements() {
        return delegatedTuple().getElements();
    }
    
    
}
