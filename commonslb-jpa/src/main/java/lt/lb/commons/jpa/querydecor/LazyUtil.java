package lt.lb.commons.jpa.querydecor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Predicate;
import lt.lb.commons.containers.collections.CollectionOp;

/**
 *
 * @author laim0nas100
 */
public class LazyUtil {

    public static <T, E extends T> ArrayList<T> lazyAdd(ArrayList<T> current, E... items) {
        return CollectionOp.lazyAdd(ArrayList::new, current, items);
    }

    public static <T> ArrayList<T> lazyInit(ArrayList<T> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return new ArrayList<>(items);
    }

    public static <T> void lazyConsumers(Collection<Consumer<T>> consumers, T item) {
        if (consumers == null || consumers.isEmpty()) {
            return;
        }
        for (Consumer<T> cons : consumers) {
            if (cons == null) {
                continue;
            }
            cons.accept(item);
        }
    }

    public static <T> void lazyPredicates(Collection<Function<T, Predicate>> predMakers, T item, Consumer<Predicate> collector) {
        if (predMakers == null || predMakers.isEmpty()) {
            return;
        }
        for (Function<T, Predicate> maker : predMakers) {
            if (maker == null) {
                continue;
            }
            Predicate apply = maker.apply(item);
            if (apply == null) {
                continue;
            }
            collector.accept(apply);
        }
    }

    public static class LazyPredAdd {

        public List<Predicate> preds;

        public LazyPredAdd() {
        }

        public void add(Predicate pred) {
            if (preds == null) {
                preds = new ArrayList<>();
            }
            preds.add(pred);
        }

        public boolean hasItems() {
            return preds != null;
        }

        public Predicate[] toArray() {
            if (hasItems()) {
                return preds.toArray(new Predicate[preds.size()]);
            }
            return new Predicate[0];
        }
    }

    public static class HiddenTypedQuery<X> implements TypedQuery<X> {

        protected final Query q;

        @Override
        public List getResultList() {
            return q.getResultList();
        }

        @Override
        public Stream getResultStream() {
            return q.getResultStream();
        }

        @Override
        public X getSingleResult() {
            return (X) q.getSingleResult();
        }

        @Override
        public int executeUpdate() {
            return q.executeUpdate();
        }

        @Override
        public TypedQuery<X> setMaxResults(int maxResult) {
            q.setMaxResults(maxResult);
            return this;
        }

        @Override
        public int getMaxResults() {
            return q.getMaxResults();
        }

        @Override
        public TypedQuery<X> setFirstResult(int startPosition) {
            q.setFirstResult(startPosition);
            return this;
        }

        @Override
        public int getFirstResult() {
            return q.getFirstResult();
        }

        @Override
        public TypedQuery<X> setHint(String hintName, Object value) {
            q.setHint(hintName, value);
            return this;
        }

        @Override
        public Map<String, Object> getHints() {
            return q.getHints();
        }

        @Override
        public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
            q.setParameter(param, value);
            return this;
        }

        @Override
        public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
            q.setParameter(param, value, temporalType);
            return this;
        }

        @Override
        public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
            q.setParameter(param, value, temporalType);
            return this;
        }

        @Override
        public TypedQuery<X> setParameter(String name, Object value) {
            q.setParameter(name, value);
            return this;
        }

        @Override
        public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
            q.setParameter(name, value, temporalType);
            return this;
        }

        @Override
        public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
            q.setParameter(name, value, temporalType);
            return this;
        }

        @Override
        public TypedQuery<X> setParameter(int position, Object value) {
            q.setParameter(position, value);
            return this;
        }

        @Override
        public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
            q.setParameter(position, value, temporalType);
            return this;
        }

        @Override
        public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
            q.setParameter(position, value, temporalType);
            return this;
        }

        @Override
        public Set<Parameter<?>> getParameters() {
            return q.getParameters();
        }

        @Override
        public Parameter<?> getParameter(String name) {
            return q.getParameter(name);
        }

        @Override
        public <T> Parameter<T> getParameter(String name, Class<T> type) {
            return q.getParameter(name, type);
        }

        @Override
        public Parameter<?> getParameter(int position) {
            return q.getParameter(position);
        }

        @Override
        public <T> Parameter<T> getParameter(int position, Class<T> type) {
            return q.getParameter(position, type);
        }

        @Override
        public boolean isBound(Parameter<?> param) {
            return q.isBound(param);
        }

        @Override
        public <T> T getParameterValue(Parameter<T> param) {
            return q.getParameterValue(param);
        }

        @Override
        public Object getParameterValue(String name) {
            return q.getParameterValue(name);
        }

        @Override
        public Object getParameterValue(int position) {
            return q.getParameterValue(position);
        }

        @Override
        public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
            q.setFlushMode(flushMode);
            return this;
        }

        @Override
        public FlushModeType getFlushMode() {
            return q.getFlushMode();
        }

        @Override
        public TypedQuery<X> setLockMode(LockModeType lockMode) {
            q.setLockMode(lockMode);
            return this;
        }

        @Override
        public LockModeType getLockMode() {
            return q.getLockMode();
        }

        @Override
        public <T> T unwrap(Class<T> cls) {
            return q.unwrap(cls);
        }

        public HiddenTypedQuery(Query q) {
            this.q = Objects.requireNonNull(q);
        }

    }

}
