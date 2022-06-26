package lt.lb.commons.jpa.tuple;

import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.Selection;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Abstract;

/**
 *
 * @author laim0nas100
 */
public interface TupleProjection2<R, T0, T1> extends TupleProjection1<R, T0> {

    public <C> Selection<T1> get1_path(Phase3Abstract<R, ?, C> from);

    public default T1 get1() {
        return null;
    }
    
    @Override
    public default <C> List<Selection<?>> getAllSelections(Phase3Abstract<R, ?, C> from) {
        return Arrays.asList(
                get0_path(from),
                get1_path(from)
        );
    }
}
