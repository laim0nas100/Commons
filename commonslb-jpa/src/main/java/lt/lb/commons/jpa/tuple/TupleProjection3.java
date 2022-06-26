package lt.lb.commons.jpa.tuple;

import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.Selection;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Abstract;

/**
 *
 * @author laim0nas100
 */
public interface TupleProjection3<R, T0, T1, T2> extends TupleProjection2<R,  T0, T1> {

    public <C> Selection<T2> get2_path(Phase3Abstract<R, ?, C> from);

    public default T2 get2(){
        return null;
    }
    
    @Override
    public default <C> List<Selection<?>> getAllSelections(Phase3Abstract<R, ?, C> from) {
        return Arrays.asList(
                get0_path(from),
                get1_path(from),
                get2_path(from)
        );
    }
}
