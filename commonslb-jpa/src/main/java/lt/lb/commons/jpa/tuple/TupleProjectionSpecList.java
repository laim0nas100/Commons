package lt.lb.commons.jpa.tuple;

import java.util.List;
import javax.persistence.criteria.Selection;
import lt.lb.commons.F;
import lt.lb.commons.jpa.querydecor.DecoratorPhases;

/**
 *
 * @author laim0nas100
 */
public interface TupleProjectionSpecList<R, T> extends TupleProjection16<R, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T> {

    public default <C> Selection<T> get(DecoratorPhases.Phase3Abstract<R, ?, C> from, int i) {
        List<Selection<?>> list = getAllSelections(from);
        if (list.size() <= i || i < 0) {
            throw new IndexOutOfBoundsException("Selection count is:" + list.size() + " currently requested:" + i + 1);
        }
        return F.cast(getAllSelections(from).get(i));

    }

    @Override
    public <C> List<Selection<?>> getAllSelections(DecoratorPhases.Phase3Abstract<R, ?, C> from);

    @Override
    public default <C> Selection<T> get15_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 15);
    }

    @Override
    public default <C> Selection<T> get14_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 14);
    }

    @Override
    public default <C> Selection<T> get13_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 13);
    }

    @Override
    public default <C> Selection<T> get12_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 12);
    }

    @Override
    public default <C> Selection<T> get11_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 11);
    }

    @Override
    public default <C> Selection<T> get10_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 10);
    }

    @Override
    public default <C> Selection<T> get9_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 9);
    }

    @Override
    public default <C> Selection<T> get8_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 8);
    }

    @Override
    public default <C> Selection<T> get7_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 7);
    }

    @Override
    public default <C> Selection<T> get6_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 6);
    }

    @Override
    public default <C> Selection<T> get5_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 5);
    }

    @Override
    public default <C> Selection<T> get4_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 4);
    }

    @Override
    public default <C> Selection<T> get3_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 3);
    }

    @Override
    public default <C> Selection<T> get2_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 2);
    }

    @Override
    public default <C> Selection<T> get1_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 1);
    }

    @Override
    public default <C> Selection<T> get0_path(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        return get(from, 0);
    }
}
