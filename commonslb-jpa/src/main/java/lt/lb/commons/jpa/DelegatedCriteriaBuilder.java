package lt.lb.commons.jpa;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

/**
 *
 * @author laim0nas100
 */
public interface DelegatedCriteriaBuilder extends CriteriaBuilder {

    public CriteriaBuilder delegate();

    @Override
    public default CriteriaQuery<Object> createQuery() {
        return delegate().createQuery();
    }

    @Override
    public default <T> CriteriaQuery<T> createQuery(Class<T> resultClass) {
        return delegate().createQuery(resultClass);
    }

    @Override
    public default CriteriaQuery<Tuple> createTupleQuery() {
        return delegate().createTupleQuery();
    }

    @Override
    public default <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
        return delegate().createCriteriaUpdate(targetEntity);
    }

    @Override
    public default <T> CriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
        return delegate().createCriteriaDelete(targetEntity);
    }

    @Override
    public default <Y> CompoundSelection<Y> construct(Class<Y> resultClass, Selection<?>... selections) {
        return delegate().construct(resultClass, selections);
    }

    @Override
    public default CompoundSelection<Tuple> tuple(Selection<?>... selections) {
        return delegate().tuple(selections);
    }

    @Override
    public default CompoundSelection<Object[]> array(Selection<?>... selections) {
        return delegate().array(selections);
    }

    @Override
    public default Order asc(Expression<?> x) {
        return delegate().asc(x);
    }

    @Override
    public default Order desc(Expression<?> x) {
        return delegate().desc(x);
    }

    @Override
    public default <N extends Number> Expression<Double> avg(Expression<N> x) {
        return delegate().avg(x);
    }

    @Override
    public default <N extends Number> Expression<N> sum(Expression<N> x) {
        return delegate().sum(x);
    }

    @Override
    public default Expression<Long> sumAsLong(Expression<Integer> x) {
        return delegate().sumAsLong(x);
    }

    @Override
    public default Expression<Double> sumAsDouble(Expression<Float> x) {
        return delegate().sumAsDouble(x);
    }

    @Override
    public default <N extends Number> Expression<N> max(Expression<N> x) {
        return delegate().max(x);
    }

    @Override
    public default <N extends Number> Expression<N> min(Expression<N> x) {
        return delegate().min(x);
    }

    @Override
    public default <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x) {
        return delegate().greatest(x);
    }

    @Override
    public default <X extends Comparable<? super X>> Expression<X> least(Expression<X> x) {
        return delegate().least(x);
    }

    @Override
    public default Expression<Long> count(Expression<?> x) {
        return delegate().count(x);
    }

    @Override
    public default Expression<Long> countDistinct(Expression<?> x) {
        return delegate().countDistinct(x);
    }

    @Override
    public default Predicate exists(Subquery<?> subquery) {
        return delegate().exists(subquery);
    }

    @Override
    public default <Y> Expression<Y> all(Subquery<Y> subquery) {
        return delegate().all(subquery);
    }

    @Override
    public default <Y> Expression<Y> some(Subquery<Y> subquery) {
        return delegate().some(subquery);
    }

    @Override
    public default <Y> Expression<Y> any(Subquery<Y> subquery) {
        return delegate().any(subquery);
    }

    @Override
    public default Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
        return delegate().and(x, y);
    }

    @Override
    public default Predicate and(Predicate... restrictions) {
        return delegate().and(restrictions);
    }

    @Override
    public default Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
        return delegate().or(x, y);
    }

    @Override
    public default Predicate or(Predicate... restrictions) {
        return delegate().or(restrictions);
    }

    @Override
    public default Predicate not(Expression<Boolean> restriction) {
        return delegate().not(restriction);
    }

    @Override
    public default Predicate conjunction() {
        return delegate().conjunction();
    }

    @Override
    public default Predicate disjunction() {
        return delegate().disjunction();
    }

    @Override
    public default Predicate isTrue(Expression<Boolean> x) {
        return delegate().isTrue(x);
    }

    @Override
    public default Predicate isFalse(Expression<Boolean> x) {
        return delegate().isFalse(x);
    }

    @Override
    public default Predicate isNull(Expression<?> x) {
        return delegate().isNull(x);
    }

    @Override
    public default Predicate isNotNull(Expression<?> x) {
        return delegate().isNotNull(x);
    }

    @Override
    public default Predicate equal(Expression<?> x, Expression<?> y) {
        return delegate().equal(x, y);
    }

    @Override
    public default Predicate equal(Expression<?> x, Object y) {
        return delegate().equal(x, y);
    }

    @Override
    public default Predicate notEqual(Expression<?> x, Expression<?> y) {
        return delegate().notEqual(x, y);
    }

    @Override
    public default Predicate notEqual(Expression<?> x, Object y) {
        return delegate().notEqual(x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Expression<? extends Y> y) {
        return delegate().greaterThan(x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Y y) {
        return delegate().greaterThan(x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y) {
        return delegate().greaterThanOrEqualTo(x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Y y) {
        return delegate().greaterThanOrEqualTo(x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Expression<? extends Y> y) {
        return delegate().lessThan(x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Y y) {
        return delegate().lessThan(x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y) {
        return delegate().lessThanOrEqualTo(x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Y y) {
        return delegate().lessThanOrEqualTo(x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Expression<? extends Y> x, Expression<? extends Y> y) {
        return delegate().between(v, x, y);
    }

    @Override
    public default <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Y x, Y y) {
        return delegate().between(v, x, y);
    }

    @Override
    public default Predicate gt(Expression<? extends Number> x, Expression<? extends Number> y) {
        return delegate().gt(x, y);
    }

    @Override
    public default Predicate gt(Expression<? extends Number> x, Number y) {
        return delegate().gt(x, y);
    }

    @Override
    public default Predicate ge(Expression<? extends Number> x, Expression<? extends Number> y) {
        return delegate().ge(x, y);
    }

    @Override
    public default Predicate ge(Expression<? extends Number> x, Number y) {
        return delegate().ge(x, y);
    }

    @Override
    public default Predicate lt(Expression<? extends Number> x, Expression<? extends Number> y) {
        return delegate().lt(x, y);
    }

    @Override
    public default Predicate lt(Expression<? extends Number> x, Number y) {
        return delegate().lt(x, y);
    }

    @Override
    public default Predicate le(Expression<? extends Number> x, Expression<? extends Number> y) {
        return delegate().le(x, y);
    }

    @Override
    public default Predicate le(Expression<? extends Number> x, Number y) {
        return delegate().le(x, y);
    }

    @Override
    public default <N extends Number> Expression<N> neg(Expression<N> x) {
        return delegate().neg(x);
    }

    @Override
    public default <N extends Number> Expression<N> abs(Expression<N> x) {
        return delegate().abs(x);
    }

    @Override
    public default <N extends Number> Expression<N> sum(Expression<? extends N> x, Expression<? extends N> y) {
        return delegate().sum(x, y);
    }

    @Override
    public default <N extends Number> Expression<N> sum(Expression<? extends N> x, N y) {
        return delegate().sum(x, y);
    }

    @Override
    public default <N extends Number> Expression<N> sum(N x, Expression<? extends N> y) {
        return delegate().sum(x, y);
    }

    @Override
    public default <N extends Number> Expression<N> prod(Expression<? extends N> x, Expression<? extends N> y) {
        return delegate().prod(x, y);
    }

    @Override
    public default <N extends Number> Expression<N> prod(Expression<? extends N> x, N y) {
        return delegate().prod(x, y);
    }

    @Override
    public default <N extends Number> Expression<N> prod(N x, Expression<? extends N> y) {
        return delegate().prod(x, y);
    }

    @Override
    public default <N extends Number> Expression<N> diff(Expression<? extends N> x, Expression<? extends N> y) {
        return delegate().diff(x, y);
    }

    @Override
    public default <N extends Number> Expression<N> diff(Expression<? extends N> x, N y) {
        return delegate().diff(x, y);
    }

    @Override
    public default <N extends Number> Expression<N> diff(N x, Expression<? extends N> y) {
        return delegate().diff(x, y);
    }

    @Override
    public default Expression<Number> quot(Expression<? extends Number> x, Expression<? extends Number> y) {
        return delegate().quot(x, y);
    }

    @Override
    public default Expression<Number> quot(Expression<? extends Number> x, Number y) {
        return delegate().quot(x, y);
    }

    @Override
    public default Expression<Number> quot(Number x, Expression<? extends Number> y) {
        return delegate().quot(x, y);
    }

    @Override
    public default Expression<Integer> mod(Expression<Integer> x, Expression<Integer> y) {
        return delegate().mod(x, y);
    }

    @Override
    public default Expression<Integer> mod(Expression<Integer> x, Integer y) {
        return delegate().mod(x, y);
    }

    @Override
    public default Expression<Integer> mod(Integer x, Expression<Integer> y) {
        return delegate().mod(x, y);
    }

    @Override
    public default Expression<Double> sqrt(Expression<? extends Number> x) {
        return delegate().sqrt(x);
    }

    @Override
    public default Expression<Long> toLong(Expression<? extends Number> number) {
        return delegate().toLong(number);
    }

    @Override
    public default Expression<Integer> toInteger(Expression<? extends Number> number) {
        return delegate().toInteger(number);
    }

    @Override
    public default Expression<Float> toFloat(Expression<? extends Number> number) {
        return delegate().toFloat(number);
    }

    @Override
    public default Expression<Double> toDouble(Expression<? extends Number> number) {
        return delegate().toDouble(number);
    }

    @Override
    public default Expression<BigDecimal> toBigDecimal(Expression<? extends Number> number) {
        return delegate().toBigDecimal(number);
    }

    @Override
    public default Expression<BigInteger> toBigInteger(Expression<? extends Number> number) {
        return delegate().toBigInteger(number);
    }

    @Override
    public default Expression<String> toString(Expression<Character> character) {
        return delegate().toString(character);
    }

    @Override
    public default <T> Expression<T> literal(T value) {
        return delegate().literal(value);
    }

    @Override
    public default <T> Expression<T> nullLiteral(Class<T> resultClass) {
        return delegate().nullLiteral(resultClass);
    }

    @Override
    public default <T> ParameterExpression<T> parameter(Class<T> paramClass) {
        return delegate().parameter(paramClass);
    }

    @Override
    public default <T> ParameterExpression<T> parameter(Class<T> paramClass, String name) {
        return delegate().parameter(paramClass, name);
    }

    @Override
    public default <C extends Collection<?>> Predicate isEmpty(Expression<C> collection) {
        return delegate().isEmpty(collection);
    }

    @Override
    public default <C extends Collection<?>> Predicate isNotEmpty(Expression<C> collection) {
        return delegate().isNotEmpty(collection);
    }

    @Override
    public default <C extends Collection<?>> Expression<Integer> size(Expression<C> collection) {
        return delegate().size(collection);
    }

    @Override
    public default <C extends Collection<?>> Expression<Integer> size(C collection) {
        return delegate().size(collection);
    }

    @Override
    public default <E, C extends Collection<E>> Predicate isMember(Expression<E> elem, Expression<C> collection) {
        return delegate().isMember(elem, collection);
    }

    @Override
    public default <E, C extends Collection<E>> Predicate isMember(E elem, Expression<C> collection) {
        return delegate().isMember(elem, collection);
    }

    @Override
    public default <E, C extends Collection<E>> Predicate isNotMember(Expression<E> elem, Expression<C> collection) {
        return delegate().isNotMember(elem, collection);
    }

    @Override
    public default <E, C extends Collection<E>> Predicate isNotMember(E elem, Expression<C> collection) {
        return delegate().isNotMember(elem, collection);
    }

    @Override
    public default <V, M extends Map<?, V>> Expression<Collection<V>> values(M map) {
        return delegate().values(map);
    }

    @Override
    public default <K, M extends Map<K, ?>> Expression<Set<K>> keys(M map) {
        return delegate().keys(map);
    }

    @Override
    public default Predicate like(Expression<String> x, Expression<String> pattern) {
        return delegate().like(x, pattern);
    }

    @Override
    public default Predicate like(Expression<String> x, String pattern) {
        return delegate().like(x, pattern);
    }

    @Override
    public default Predicate like(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
        return delegate().like(x, pattern, escapeChar);
    }

    @Override
    public default Predicate like(Expression<String> x, Expression<String> pattern, char escapeChar) {
        return delegate().like(x, pattern, escapeChar);
    }

    @Override
    public default Predicate like(Expression<String> x, String pattern, Expression<Character> escapeChar) {
        return delegate().like(x, pattern, escapeChar);
    }

    @Override
    public default Predicate like(Expression<String> x, String pattern, char escapeChar) {
        return delegate().like(x, pattern, escapeChar);
    }

    @Override
    public default Predicate notLike(Expression<String> x, Expression<String> pattern) {
        return delegate().notLike(x, pattern);
    }

    @Override
    public default Predicate notLike(Expression<String> x, String pattern) {
        return delegate().notLike(x, pattern);
    }

    @Override
    public default Predicate notLike(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
        return delegate().notLike(x, pattern, escapeChar);
    }

    @Override
    public default Predicate notLike(Expression<String> x, Expression<String> pattern, char escapeChar) {
        return delegate().notLike(x, pattern, escapeChar);
    }

    @Override
    public default Predicate notLike(Expression<String> x, String pattern, Expression<Character> escapeChar) {
        return delegate().notLike(x, pattern, escapeChar);
    }

    @Override
    public default Predicate notLike(Expression<String> x, String pattern, char escapeChar) {
        return delegate().notLike(x, pattern, escapeChar);
    }

    @Override
    public default Expression<String> concat(Expression<String> x, Expression<String> y) {
        return delegate().concat(x, y);
    }

    @Override
    public default Expression<String> concat(Expression<String> x, String y) {
        return delegate().concat(x, y);
    }

    @Override
    public default Expression<String> concat(String x, Expression<String> y) {
        return delegate().concat(x, y);
    }

    @Override
    public default Expression<String> substring(Expression<String> x, Expression<Integer> from) {
        return delegate().substring(x, from);
    }

    @Override
    public default Expression<String> substring(Expression<String> x, int from) {
        return delegate().substring(x, from);
    }

    @Override
    public default Expression<String> substring(Expression<String> x, Expression<Integer> from, Expression<Integer> len) {
        return delegate().substring(x, from, len);
    }

    @Override
    public default Expression<String> substring(Expression<String> x, int from, int len) {
        return delegate().substring(x, from, len);
    }

    @Override
    public default Expression<String> trim(Expression<String> x) {
        return delegate().trim(x);
    }

    @Override
    public default Expression<String> trim(Trimspec ts, Expression<String> x) {
        return delegate().trim(ts, x);
    }

    @Override
    public default Expression<String> trim(Expression<Character> t, Expression<String> x) {
        return delegate().trim(t, x);
    }

    @Override
    public default Expression<String> trim(Trimspec ts, Expression<Character> t, Expression<String> x) {
        return delegate().trim(ts, t, x);
    }

    @Override
    public default Expression<String> trim(char t, Expression<String> x) {
        return delegate().trim(t, x);
    }

    @Override
    public default Expression<String> trim(Trimspec ts, char t, Expression<String> x) {
        return delegate().trim(ts, t, x);
    }

    @Override
    public default Expression<String> lower(Expression<String> x) {
        return delegate().lower(x);
    }

    @Override
    public default Expression<String> upper(Expression<String> x) {
        return delegate().upper(x);
    }

    @Override
    public default Expression<Integer> length(Expression<String> x) {
        return delegate().length(x);
    }

    @Override
    public default Expression<Integer> locate(Expression<String> x, Expression<String> pattern) {
        return delegate().locate(x, pattern);
    }

    @Override
    public default Expression<Integer> locate(Expression<String> x, String pattern) {
        return delegate().locate(x, pattern);
    }

    @Override
    public default Expression<Integer> locate(Expression<String> x, Expression<String> pattern, Expression<Integer> from) {
        return delegate().locate(x, pattern, from);
    }

    @Override
    public default Expression<Integer> locate(Expression<String> x, String pattern, int from) {
        return delegate().locate(x, pattern, from);
    }

    @Override
    public default Expression<Date> currentDate() {
        return delegate().currentDate();
    }

    @Override
    public default Expression<Timestamp> currentTimestamp() {
        return delegate().currentTimestamp();
    }

    @Override
    public default Expression<Time> currentTime() {
        return delegate().currentTime();
    }

    @Override
    public default <T> In<T> in(Expression<? extends T> expression) {
        return delegate().in(expression);
    }

    @Override
    public default <Y> Expression<Y> coalesce(Expression<? extends Y> x, Expression<? extends Y> y) {
        return delegate().coalesce(x, y);
    }

    @Override
    public default <Y> Expression<Y> coalesce(Expression<? extends Y> x, Y y) {
        return delegate().coalesce(x, y);
    }

    @Override
    public default <Y> Expression<Y> nullif(Expression<Y> x, Expression<?> y) {
        return delegate().nullif(x, y);
    }

    @Override
    public default <Y> Expression<Y> nullif(Expression<Y> x, Y y) {
        return delegate().nullif(x, y);
    }

    @Override
    public default <T> Coalesce<T> coalesce() {
        return delegate().coalesce();
    }

    @Override
    public default <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
        return delegate().selectCase(expression);
    }

    @Override
    public default <R> Case<R> selectCase() {
        return delegate().selectCase();
    }

    @Override
    public default <T> Expression<T> function(String name, Class<T> type, Expression<?>... args) {
        return delegate().function(name, type, args);
    }

    @Override
    public default <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> type) {
        return delegate().treat(join, type);
    }

    @Override
    public default <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type) {
        return delegate().treat(join, type);
    }

    @Override
    public default <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type) {
        return delegate().treat(join, type);
    }

    @Override
    public default <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type) {
        return delegate().treat(join, type);
    }

    @Override
    public default <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type) {
        return delegate().treat(join, type);
    }

    @Override
    public default <X, T extends X> Path<T> treat(Path<X> path, Class<T> type) {
        return delegate().treat(path, type);
    }

    @Override
    public default <X, T extends X> Root<T> treat(Root<X> root, Class<T> type) {
        return delegate().treat(root, type);
    }

}
