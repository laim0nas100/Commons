package lt.lb.commons.jpa.searchpart;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class ComparableSearchIntervalPart<T extends Comparable<T>> extends BaseSearchExpressionPart<T, ComparableSearchIntervalPart<T>> {

    protected T min;
    protected T max;

    protected boolean includingMin;
    protected boolean includingMax;

    public ComparableSearchIntervalPart() {
    }

    public ComparableSearchIntervalPart(T min, T max) {
        super();
        this.min = min;
        this.max = max;
    }

    protected ComparableSearchIntervalPart(ComparableSearchIntervalPart<T> copy) {
        super(copy);
        this.min = copy.min;
        this.max = copy.max;
    }

    @Override
    public ComparableSearchIntervalPart<T> clone() throws CloneNotSupportedException {
        return new ComparableSearchIntervalPart<>(this);
    }

    protected Predicate buildMaxPredicate(CriteriaBuilder builder, Expression<T> search) {
        return includingMax ? builder.lessThanOrEqualTo(search, max) : builder.lessThanOrEqualTo(search, max);
    }

    protected Predicate buildMinPredicate(CriteriaBuilder builder, Expression<T> search) {
        return includingMin ? builder.greaterThanOrEqualTo(search, min) : builder.greaterThan(search, min);
    }

    @Override
    public Predicate buildPredicateImpl(CriteriaBuilder builder, Expression<T> search) {
        if (min != null && max != null) {
            return builder.and(buildMaxPredicate(builder, search), buildMinPredicate(builder, search));

        }
        if (max != null) {
            return buildMaxPredicate(builder, search);
        }
        if (min != null) {
            return buildMinPredicate(builder, search);
        }
        throw new IllegalStateException("Not min nor max are set");
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && (min != null || max != null);
    }

    public T getMin() {
        return min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return max;
    }

    public void setMax(T max) {
        this.max = max;
    }

    public boolean isIncludingMin() {
        return includingMin;
    }

    public void setIncludingMin(boolean includingMin) {
        this.includingMin = includingMin;
    }

    public boolean isIncludingMax() {
        return includingMax;
    }

    public void setIncludingMax(boolean includingMax) {
        this.includingMax = includingMax;
    }

}
