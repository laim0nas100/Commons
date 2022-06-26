package lt.lb.commons.jpa.tuple;

import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.Selection;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Abstract;

/**
 *
 * @author laim0nas100
 */
public interface TupleProjection9<R, T0, T1, T2, T3, T4, T5, T6, T7, T8> extends TupleProjection8<R, T0, T1, T2, T3, T4, T5, T6, T7> {

    public <C> Selection<T8> get8_path(Phase3Abstract<R, ?, C> from);

    public default T8 get8() {
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
                get6_path(from),
                get7_path(from),
                get8_path(from)
        );
    }
}
