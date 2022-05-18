package lt.lb.commons.jpa.querydecor;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * In memory modifications of final query results.
 *
 * @author laim0nas100
 */
public interface JpaQueryResultProvider<X> {

    Query originalQuery();

    List<X> getResultList();

    default Stream<X> getResultStream() {
        return getResultList().stream();
    }

    JpaQueryResultProvider<X> modified(Function<List<X>, List<X>> mapper);

    public static <T> BasicJpaResultProvider<T> of(TypedQuery<T> query) {
        return new BasicJpaResultProvider<>(query);
    }

    public static class BasicJpaResultProvider<T> implements JpaQueryResultProvider<T> {

        protected TypedQuery<T> query;
        protected List<T> cached;

        public BasicJpaResultProvider(TypedQuery<T> query) {
            this.query = Objects.requireNonNull(query, "Supplied query must not be null");
        }

        @Override
        public List<T> getResultList() {
            if (cached == null) {
                cached = query.getResultList();
            }
            return cached;
        }

        @Override
        public JpaQueryResultProvider<T> modified(Function<List<T>, List<T>> mapper) {
            return new ModifiedJpaResultProvider<>(mapper, this);
        }

        @Override
        public Query originalQuery() {
            return query;
        }
    }

    public static class ModifiedJpaResultProvider<T> implements JpaQueryResultProvider<T> {

        protected Function<List<T>, List<T>> mapper;
        protected JpaQueryResultProvider<T> supply;
        protected List<T> cached;

        public ModifiedJpaResultProvider(Function<List<T>, List<T>> mapper, JpaQueryResultProvider<T> supply) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper is null");
            this.supply = Objects.requireNonNull(supply, "Supply is null");
        }

        @Override
        public List<T> getResultList() {
            if(cached == null){
                cached = mapper.apply(supply.getResultList());
            }
            return cached;
        }

        @Override
        public JpaQueryResultProvider<T> modified(Function<List<T>, List<T>> mapper) {
            return new ModifiedJpaResultProvider<>(mapper, this);
        }

        @Override
        public Query originalQuery() {
            return supply.originalQuery();
        }

    }

}
