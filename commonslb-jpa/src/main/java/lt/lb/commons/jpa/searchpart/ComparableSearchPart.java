package lt.lb.commons.jpa.searchpart;

import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 */
public class ComparableSearchPart<T extends Comparable<T>> extends SimpleSearchPart<T> {

    public enum ComparableSearchPartEnum {
        BEFORE,
        AFTER,
        EXACT;
    }

    protected ComparableSearchPartEnum searchType = ComparableSearchPartEnum.EXACT;
    protected boolean including;

    public ComparableSearchPartEnum getSearchType() {
        return searchType;
    }

    public void setSearchType(ComparableSearchPartEnum searchType) {
        this.searchType = searchType;
    }

    public boolean isIncluding() {
        return including;
    }

    public void setIncluding(boolean including) {
        this.including = including;
    }

    public ComparableSearchPart() {
    }

    public ComparableSearchPart(T val) {
        super(val);
    }

    protected ComparableSearchPart(ComparableSearchPart<T> copy) {
        super(copy);
        this.searchType = copy.searchType;
    }

    @Override
    public ComparableSearchPart<T> clone() {
        return new ComparableSearchPart<>(this);
    }

    @Override
    public Predicate buildPredicateImpl(CriteriaBuilder builder, Expression<T> search) {
        return comparePredicate(builder, search, searchType, including, get());
    }

    public static <E extends Comparable<E>> Predicate comparePredicate(CriteriaBuilder builder, Expression<E> search, ComparableSearchPartEnum searchType, boolean including, E val) {
        Objects.requireNonNull(builder);
        Objects.requireNonNull(search);
        Objects.requireNonNull(searchType);
        Objects.requireNonNull(val);

        switch (searchType) {
            case EXACT: {
                return builder.equal(search, val);
            }
            case BEFORE: {
                if (including) {
                    return builder.lessThanOrEqualTo(search, val);
                } else {
                    return builder.lessThan(search, val);
                }

            }
            case AFTER: {
                if (including) {
                    return builder.greaterThanOrEqualTo(search, val);
                } else {
                    return builder.greaterThan(search, val);
                }

            }
            default:
                throw new IllegalArgumentException("Failed to match search type:" + searchType);

        }
    }

}
