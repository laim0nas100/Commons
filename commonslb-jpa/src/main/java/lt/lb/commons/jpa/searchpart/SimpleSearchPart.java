package lt.lb.commons.jpa.searchpart;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class SimpleSearchPart<T> extends BaseSearchPart<SimpleSearchPart<T>> implements SearchPartValue<T, SimpleSearchPart<T>>, ValueProxy<T> {

    protected T value;

    public SimpleSearchPart() {
        super();
    }

    public SimpleSearchPart(T val) {
        super();
        this.value = val;
    }

    protected SimpleSearchPart(SimpleSearchPart<T> copy) {
        super(copy);
        this.value = copy.value;
    }

    @Override
    public Predicate buildPredicateImpl(CriteriaBuilder builder, Expression<T> search) {
        return builder.equal(search, get());
    }

    @Override
    public SimpleSearchPart<T> clone() {
        return new SimpleSearchPart<>(this);
    }

    @Override
    public boolean isEnabled() {
        return isNotNull() && super.isEnabled();
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T v) {
        this.value = v;
    }

}
