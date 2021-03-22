package lt.lb.commons.jpa.searchpart;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 */
public class ComposedSearchExpressionPart<T> extends BaseSearchExpressionPart<T, ComposedSearchExpressionPart<T>> {

    protected List<SearchPartExpresion<T, ?>> composed = new ArrayList<>();
    protected ComposedSearchExpressionPartEnum gateType = ComposedSearchExpressionPartEnum.AND;

    public enum ComposedSearchExpressionPartEnum {
        AND, OR, XOR
    }

    public ComposedSearchExpressionPart() {
    }

    public ComposedSearchExpressionPart(ComposedSearchExpressionPart<T> copy) {
        super(copy);

    }

    public List<SearchPartExpresion<T, ?>> getComposed() {
        return composed;
    }

    public void setComposed(List<SearchPartExpresion<T, ?>> composed) {
        this.composed = composed;
    }

    public ComposedSearchExpressionPartEnum getGateType() {
        return gateType;
    }

    public void setGateType(ComposedSearchExpressionPartEnum gateType) {
        this.gateType = gateType;
    }

    @Override
    public ComposedSearchExpressionPart<T> clone() {
        return new ComposedSearchExpressionPart<>(this);
    }

    @Override
    public Predicate buildPredicateImpl(CriteriaBuilder builder, Expression<T> search) {
        Predicate[] toArray = composed.stream().filter(comp -> comp.isEnabled()).map(comp -> {
            return comp.buildPredicateImpl(builder, search);
        }).toArray(s -> new Predicate[s]);
        boolean empty = toArray.length == 0;
        switch (getGateType()) {
            case AND: {
                if (empty) {
                    return builder.conjunction();
                }
                return builder.and(toArray);
            }
            case OR: {
                if (empty) {
                    return builder.disjunction();
                }
                return builder.or(toArray);
            }

            case XOR: {
                if (empty) {
                    return builder.disjunction();
                }
                return builder.notEqual(builder.or(toArray), builder.and(toArray));
            }
            default:
                throw new IllegalArgumentException("Unrecognized gate" + getGateType());

        }

    }

}
