package lt.lb.commons.jpa.tuple;

import java.util.List;
import javax.persistence.criteria.Selection;
import lt.lb.commons.jpa.querydecor.DecoratorPhases;

/**
 *
 * @author laim0nas100
 */
public interface TupleProjectionResultList<R, T> extends TupleProjectionSpecList<R, T> {

    @Override
    public default <C> List<Selection<?>> getAllSelections(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        throw new UnsupportedOperationException(getClass() + " does not support path resolution");
    }

    @Override
    public List<T> getList();

    @Override
    public default T get15() {
        return (T) getList().get(15);
    }

    @Override
    public default T get14() {
        return (T) getList().get(14);
    }

    @Override
    public default T get13() {
        return (T) getList().get(13);
    }

    @Override
    public default T get12() {
        return (T) getList().get(12);
    }

    @Override
    public default T get11() {
        return (T) getList().get(11);
    }

    @Override
    public default T get10() {
        return (T) getList().get(10);
    }

    @Override
    public default T get9() {
        return (T) getList().get(9);
    }

    @Override
    public default T get8() {
        return (T) getList().get(8);
    }

    @Override
    public default T get7() {
        return (T) getList().get(7);
    }

    @Override
    public default T get6() {
        return (T) getList().get(6);
    }

    @Override
    public default T get5() {
        return (T) getList().get(5);
    }

    @Override
    public default T get4() {
        return (T) getList().get(4);
    }

    @Override
    public default T get3() {
        return (T) getList().get(3);
    }

    @Override
    public default T get2() {
        return (T) getList().get(2);
    }

    @Override
    public default T get1() {
        return (T) getList().get(1);
    }

    @Override
    public default T get0() {
        return (T) getList().get(0);
    }

}
