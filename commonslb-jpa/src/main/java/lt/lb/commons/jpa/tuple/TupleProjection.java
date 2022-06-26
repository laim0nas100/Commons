package lt.lb.commons.jpa.tuple;

import java.util.List;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;
import lt.lb.commons.F;
import lt.lb.commons.jpa.querydecor.DecoratorPhases;
import lt.lb.commons.jpa.querydecor.JpaExpResolve;

/**
 * Middle-ground for Tuple and type-based selection: Tuple with types.
 *
 * @author laim0nas100
 */
public interface TupleProjection<R> {

    public <C> List<Selection<?>> getAllSelections(DecoratorPhases.Phase3Abstract<R, ?, C> from);

    public default Class<? extends TupleProjection> getResultType() {
        return TupleProjectionResult.class;
    }

    public static <R, T0> TupleProjection1<R, T0> of1(
            JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0
    ) {
        return F.cast(new TupleProjectionSpec<>(exp0));
    }

    public static <R, T0, T1>
            TupleProjection2<R, T0, T1> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1
        ));
    }

    public static <R, T0, T1, T2>
            TupleProjection3<R, T0, T1, T2> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2
        ));
    }

    public static <R, T0, T1, T2, T3>
            TupleProjection4<R, T0, T1, T2, T3> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3
        ));
    }

    public static <R, T0, T1, T2, T3, T4>
            TupleProjection5<R, T0, T1, T2, T3, T4> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4
        ));
    }

    public static <R, T0, T1, T2, T3, T4, T5>
            TupleProjection6<R, T0, T1, T2, T3, T4, T5> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5
        ));
    }

    public static <R, T0, T1, T2, T3, T4, T5, T6>
            TupleProjection7<R, T0, T1, T2, T3, T4, T5, T6> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6
        ));
    }

    public static <R, T0, T1, T2, T3, T4, T5, T6, T7>
            TupleProjection8<R, T0, T1, T2, T3, T4, T5, T6, T7> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6,
                    JpaExpResolve<R, T7, ? extends Path<R>, ? extends Expression<T7>, ?> exp7
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6,
                exp7
        ));
    }

    public static <R, T0, T1, T2, T3, T4, T5, T6, T7, T8>
            TupleProjection9<R, T0, T1, T2, T3, T4, T5, T6, T7, T8> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6,
                    JpaExpResolve<R, T7, ? extends Path<R>, ? extends Expression<T7>, ?> exp7,
                    JpaExpResolve<R, T8, ? extends Path<R>, ? extends Expression<T8>, ?> exp8
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6,
                exp7,
                exp8
        ));
    }

    public static <R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>
            TupleProjection10<R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6,
                    JpaExpResolve<R, T7, ? extends Path<R>, ? extends Expression<T7>, ?> exp7,
                    JpaExpResolve<R, T8, ? extends Path<R>, ? extends Expression<T8>, ?> exp8,
                    JpaExpResolve<R, T9, ? extends Path<R>, ? extends Expression<T9>, ?> exp9
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6,
                exp7,
                exp8,
                exp9
        ));
    }

    public static <R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>
            TupleProjection11<R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6,
                    JpaExpResolve<R, T7, ? extends Path<R>, ? extends Expression<T7>, ?> exp7,
                    JpaExpResolve<R, T8, ? extends Path<R>, ? extends Expression<T8>, ?> exp8,
                    JpaExpResolve<R, T9, ? extends Path<R>, ? extends Expression<T9>, ?> exp9,
                    JpaExpResolve<R, T10, ? extends Path<R>, ? extends Expression<T10>, ?> exp10
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6,
                exp7,
                exp8,
                exp9,
                exp10
        ));
    }

    public static <R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>
            TupleProjection12<R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10,T11> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6,
                    JpaExpResolve<R, T7, ? extends Path<R>, ? extends Expression<T7>, ?> exp7,
                    JpaExpResolve<R, T8, ? extends Path<R>, ? extends Expression<T8>, ?> exp8,
                    JpaExpResolve<R, T9, ? extends Path<R>, ? extends Expression<T9>, ?> exp9,
                    JpaExpResolve<R, T10, ? extends Path<R>, ? extends Expression<T10>, ?> exp10,
                    JpaExpResolve<R, T11, ? extends Path<R>, ? extends Expression<T11>, ?> exp11
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6,
                exp7,
                exp8,
                exp9,
                exp10,
                exp11
        ));
    }
            
            public static <R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>
            TupleProjection13<R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9,T10, T11, T12> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6,
                    JpaExpResolve<R, T7, ? extends Path<R>, ? extends Expression<T7>, ?> exp7,
                    JpaExpResolve<R, T8, ? extends Path<R>, ? extends Expression<T8>, ?> exp8,
                    JpaExpResolve<R, T9, ? extends Path<R>, ? extends Expression<T9>, ?> exp9,
                    JpaExpResolve<R, T10, ? extends Path<R>, ? extends Expression<T10>, ?> exp10,
                    JpaExpResolve<R, T11, ? extends Path<R>, ? extends Expression<T11>, ?> exp11,
                    JpaExpResolve<R, T12, ? extends Path<R>, ? extends Expression<T12>, ?> exp12
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6,
                exp7,
                exp8,
                exp9,
                exp10,
                exp11,
                exp12
        ));
    }
            
            
                 public static <R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>
            TupleProjection14<R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9,T10,T11, T12, T13> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6,
                    JpaExpResolve<R, T7, ? extends Path<R>, ? extends Expression<T7>, ?> exp7,
                    JpaExpResolve<R, T8, ? extends Path<R>, ? extends Expression<T8>, ?> exp8,
                    JpaExpResolve<R, T9, ? extends Path<R>, ? extends Expression<T9>, ?> exp9,
                    JpaExpResolve<R, T10, ? extends Path<R>, ? extends Expression<T10>, ?> exp10,
                    JpaExpResolve<R, T11, ? extends Path<R>, ? extends Expression<T11>, ?> exp11,
                    JpaExpResolve<R, T12, ? extends Path<R>, ? extends Expression<T12>, ?> exp12,
                    JpaExpResolve<R, T13, ? extends Path<R>, ? extends Expression<T13>, ?> exp13
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6,
                exp7,
                exp8,
                exp9,
                exp10,
                exp11,
                exp12,
                exp13
        ));
    }
            
                    public static <R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
            TupleProjection15<R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10 ,T11, T12, T13, T14> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6,
                    JpaExpResolve<R, T7, ? extends Path<R>, ? extends Expression<T7>, ?> exp7,
                    JpaExpResolve<R, T8, ? extends Path<R>, ? extends Expression<T8>, ?> exp8,
                    JpaExpResolve<R, T9, ? extends Path<R>, ? extends Expression<T9>, ?> exp9,
                    JpaExpResolve<R, T10, ? extends Path<R>, ? extends Expression<T10>, ?> exp10,
                    JpaExpResolve<R, T11, ? extends Path<R>, ? extends Expression<T11>, ?> exp11,
                    JpaExpResolve<R, T12, ? extends Path<R>, ? extends Expression<T12>, ?> exp12,
                    JpaExpResolve<R, T13, ? extends Path<R>, ? extends Expression<T13>, ?> exp13,
                    JpaExpResolve<R, T14, ? extends Path<R>, ? extends Expression<T14>, ?> exp14
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6,
                exp7,
                exp8,
                exp9,
                exp10,
                exp11,
                exp12,
                exp13,
                exp14
        ));
    }
            
                        public static <R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
            TupleProjection16<R, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> of(
                    JpaExpResolve<R, T0, ? extends Path<R>, ? extends Expression<T0>, ?> exp0,
                    JpaExpResolve<R, T1, ? extends Path<R>, ? extends Expression<T1>, ?> exp1,
                    JpaExpResolve<R, T2, ? extends Path<R>, ? extends Expression<T2>, ?> exp2,
                    JpaExpResolve<R, T3, ? extends Path<R>, ? extends Expression<T3>, ?> exp3,
                    JpaExpResolve<R, T4, ? extends Path<R>, ? extends Expression<T4>, ?> exp4,
                    JpaExpResolve<R, T5, ? extends Path<R>, ? extends Expression<T5>, ?> exp5,
                    JpaExpResolve<R, T6, ? extends Path<R>, ? extends Expression<T6>, ?> exp6,
                    JpaExpResolve<R, T7, ? extends Path<R>, ? extends Expression<T7>, ?> exp7,
                    JpaExpResolve<R, T8, ? extends Path<R>, ? extends Expression<T8>, ?> exp8,
                    JpaExpResolve<R, T9, ? extends Path<R>, ? extends Expression<T9>, ?> exp9,
                    JpaExpResolve<R, T10, ? extends Path<R>, ? extends Expression<T10>, ?> exp10,
                    JpaExpResolve<R, T11, ? extends Path<R>, ? extends Expression<T11>, ?> exp11,
                    JpaExpResolve<R, T12, ? extends Path<R>, ? extends Expression<T12>, ?> exp12,
                    JpaExpResolve<R, T13, ? extends Path<R>, ? extends Expression<T13>, ?> exp13,
                    JpaExpResolve<R, T14, ? extends Path<R>, ? extends Expression<T14>, ?> exp14,
                    JpaExpResolve<R, T15, ? extends Path<R>, ? extends Expression<T15>, ?> exp15
            ) {
        return F.cast(new TupleProjectionSpec<>(
                exp0,
                exp1,
                exp2,
                exp3,
                exp4,
                exp5,
                exp6,
                exp7,
                exp8,
                exp9,
                exp10,
                exp11,
                exp12,
                exp13,
                exp14,
                exp15
        ));
    }

}
