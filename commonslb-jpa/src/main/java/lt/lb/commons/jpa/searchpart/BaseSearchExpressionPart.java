package lt.lb.commons.jpa.searchpart;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 * @param <T>
 * @param <M> implementation
 */
public abstract class BaseSearchExpressionPart<T, M extends BaseSearchExpressionPart<T, M>> extends BaseSearchPart<M> implements SearchPartExpresion<T, M> {

    protected BaseSearchExpressionPart() {
    }

    protected BaseSearchExpressionPart(boolean enabled) {
        super(enabled);
    }

    protected BaseSearchExpressionPart(BaseSearchPart<M> copy) {
        super(copy);
    }
    
    @Override
    public abstract Predicate buildPredicateImpl(CriteriaBuilder builder, Expression<T> search);
}
