package lt.lb.commons.jpa.searchpart;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 */
public class NullCheckSeachPart extends BaseSearchPart<NullCheckSeachPart> implements SearchPartExpresion<Object, NullCheckSeachPart> {

    protected boolean needsNull;

    public NullCheckSeachPart(boolean enabled, boolean needsNull) {
        super(enabled);
        this.needsNull = needsNull;

    }

    public NullCheckSeachPart(boolean needsNull) {
        super();
        this.needsNull = needsNull;
    }

    public NullCheckSeachPart() {
        super();
    }

    protected NullCheckSeachPart(NullCheckSeachPart copy) {
        super(copy);
        this.needsNull = copy.needsNull;
    }

    public boolean isNeedsNull() {
        return needsNull;
    }

    public void setNeedsNull(boolean needsNull) {
        this.needsNull = needsNull;
    }

    @Override
    public Predicate buildPredicateImpl(CriteriaBuilder builder, Expression search) {
        if (isNeedsNull()) {
            return builder.isNull(search);
        } else {
            return builder.isNotNull(search);
        }
    }

    @Override
    public NullCheckSeachPart clone() {
        return new NullCheckSeachPart(this);
    }
}
