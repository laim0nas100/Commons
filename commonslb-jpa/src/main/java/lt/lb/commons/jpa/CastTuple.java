package lt.lb.commons.jpa;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

/**
 *
 * @author laim0nas100
 */
public interface CastTuple extends Tuple, Iterator, Iterable {

    public default <X> X getCast(int index) {
        return (X) get(index);
    }

    public default <X> X getCast(String alias) {
        return (X) get(alias);
    }

    public default <X> X nextCast() {
        return (X) next();
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
        public <X> X get(TupleElement<X> tupleElement) {
            return real.get(tupleElement);
        }

        @Override
        public <X> X get(String alias, Class<X> type) {
            return real.get(alias, type);
        }

        @Override
        public Object get(String alias) {
            return real.get(alias);
        }

        @Override
        public <X> X get(int i, Class<X> type) {
            return real.get(i, type);
        }

        @Override
        public Object get(int i) {
            return real.get(i);
        }

        @Override
        public Object[] toArray() {
            return real.toArray();
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
            return real.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return real.equals(obj);
        }

        @Override
        public String toString() {
            return real.toString();
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
            return new CastTupleImpl(real);
        }
    }
}
