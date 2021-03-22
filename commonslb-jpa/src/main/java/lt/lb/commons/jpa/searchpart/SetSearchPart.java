package lt.lb.commons.jpa.searchpart;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 *
 * Empty set means test if expression is null (null values are not usually
 * possible in any other set)
 * @param <T>
 */
public class SetSearchPart<T> extends BaseSearchPart<SetSearchPart<T>> implements SearchPartExpresion<T, SetSearchPart<T>> {

    protected Set<T> set;

    public SetSearchPart() {
        super();
        this.set = new HashSet<>();
    }

    public SetSearchPart(Collection<T> val) {
        super();
        this.set = new HashSet<>(Objects.requireNonNull(val));
    }

    protected SetSearchPart(SetSearchPart<T> copy) {
        super(copy);
        this.set = new HashSet<>(copy.set);
    }

    @Override
    public SetSearchPart<T> clone() throws CloneNotSupportedException {
        return new SetSearchPart<>(this);
    }

    @Override
    public Predicate buildPredicateImpl(CriteriaBuilder builder, Expression<T> search) {
        if (!set.isEmpty()) {
            return search.in(set);
        } else {
            return search.isNull();
        }
    }

}
