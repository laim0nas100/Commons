package lt.lb.commons.jpa.tuple;

import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.Selection;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Abstract;

/**
 *
 * @author laim0nas100
 */
public interface TupleProjection7<R, T0, T1, T2, T3, T4, T5, T6> extends TupleProjection6<R, T0, T1, T2, T3, T4, T5> {

    public <C> Selection<T6> get6_path(Phase3Abstract<R, ?, C> from);

    public default T6 get6() {
        return null;
    }
    
    @Override
    public default <C> List<Selection<?>> getAllSelections(Phase3Abstract<R, ?, C> from) {
        return Arrays.asList(
                get0_path(from),
                get1_path(from),
                get2_path(from),
                get3_path(from),
                get4_path(from),
                get5_path(from),
                get6_path(from)
        );
    }
}
