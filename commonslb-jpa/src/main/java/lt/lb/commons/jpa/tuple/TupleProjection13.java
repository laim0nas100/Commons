package lt.lb.commons.jpa.tuple;

import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.Selection;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Abstract;

/**
 *
 * @author laim0nas100
 */
public interface TupleProjection13<R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> extends TupleProjection12<R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> {

    public <C> Selection<T12> get12_path(Phase3Abstract<R, ?, C> from);

    public default T12 get12() {
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
                get8_path(from),
                get9_path(from),
                get10_path(from),
                get11_path(from),
                get12_path(from)
        );
    }
}
