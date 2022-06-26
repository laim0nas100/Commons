package lt.lb.commons.jpa.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

/**
 *
 * @author laim0nas100
 */
public interface CastTuple extends DelegatedTuple, Iterator, Iterable {

    public default <X> X getCast(int index) {
        return (X) get(index);
    }

    public default <X> X getCast(String alias) {
        return (X) get(alias);
    }

    public default <X> X nextCast() {
        return (X) next();
    }
    
    public default List toList(){
        return Arrays.asList(toArray());
    }

    public static CastTuple of(Tuple tuple) {
        return new CastTupleImpl(tuple);
    }

    public static class CastTupleImpl implements CastTuple {

        public final Tuple real;
        private int index = -1;
        private List<TupleElement<?>> elems;

        public CastTupleImpl(Tuple real) {
            this.real = Objects.requireNonNull(real);
        }

        @Override
        public List<TupleElement<?>> getElements() {
            if (elems == null) {
                elems = real.getElements();
            }
            return elems;
        }

        @Override
        public int hashCode() {
            return delegatedTuple().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return delegatedTuple().equals(obj);
        }

        @Override
        public String toString() {
            return delegatedTuple().toString();
        }

        @Override
        public boolean hasNext() {
            return getElements().size() > index + 1;
        }

        @Override
        public Object next() {
            return get(++index);
        }

        @Override
        public Iterator iterator() {
            return new CastTupleImpl(delegatedTuple());
        }

        @Override
        public Tuple delegatedTuple() {
            return real;
        }
    }
}
