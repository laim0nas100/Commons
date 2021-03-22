package lt.lb.commons.jpa.searchpart;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 * @param <T> expression type
 * @param <M> implementation
 */
public interface SearchPartExpresion<T, M extends SearchPartExpresion<T, M>> extends SearchPart<M> {

    /**
     * Builds predicate regardless if it is enabled or not
     *
     * @param builder
     * @param search
     * @return
     */
    public Predicate buildPredicateImpl(CriteriaBuilder builder, Expression<T> search);

    /**
     * Builds predicate and negates it if necessary
     *
     * @param builder
     * @param search
     * @return
     */
    public default Predicate buildPredicate(CriteriaBuilder builder, Expression<T> search) {
        Predicate predicate = buildPredicateImpl(builder, search);
        return isNegated() ? predicate.not() : predicate;
    }

    /**
     * If enabled builds predicate and feeds it to the consumer
     *
     * @param builder
     * @param search
     * @param predCons
     */
    public default void buildPredicate(CriteriaBuilder builder, Expression<T> search, Consumer<Predicate> predCons) {
        if (isEnabled()) {
            Predicate pred = buildPredicate(builder, search);
            predCons.accept(pred);
        }
    }

    /**
     * If enabled builds predicate and feeds it to the consumer
     *
     * @param builder
     * @param search
     * @param predCons
     */
    public default void buildPredicate(CriteriaBuilder builder, Supplier<Expression<T>> search, Consumer<Predicate> predCons) {
        if (isEnabled()) {
            Predicate pred = buildPredicate(builder, search.get());
            predCons.accept(pred);
        }
    }
}
