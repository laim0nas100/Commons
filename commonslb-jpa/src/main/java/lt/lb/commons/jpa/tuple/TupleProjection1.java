package lt.lb.commons.jpa.tuple;

import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.Selection;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Abstract;

/**
 *
 * @author laim0nas100
 */
public interface TupleProjection1<R, T0> extends TupleProjection<R> {

    public <C> Selection<T0> get0_path(Phase3Abstract<R, ?, C> from);
    
    public List<?> getList();

    public default T0 get0() {
        return null;
    }

    @Override
    public default <C> List<Selection<?>> getAllSelections(Phase3Abstract<R, ?, C> from) {
        return Arrays.asList(
                get0_path(from)
        );
    }
}
