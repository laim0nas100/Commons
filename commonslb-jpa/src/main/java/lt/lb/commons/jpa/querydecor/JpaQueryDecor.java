package lt.lb.commons.jpa.querydecor;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.F;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.commons.iteration.streams.SimpleStream;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase2;

/**
 *
 * @{inheritDoc}
 */
public class JpaQueryDecor<T_ROOT, T_RESULT> extends BaseJpaQueryDecor<T_ROOT, T_RESULT, Void, JpaQueryDecor<T_ROOT, T_RESULT>> {

    public static class JpaQueryDecorResultProvider<X> implements JpaQueryResultProvider<X> {

        protected JpaQueryResultProvider<X> original;

        public JpaQueryDecorResultProvider(JpaQueryResultProvider<X> original) {
            this.original = Objects.requireNonNull(original, "Original result provided must be not null");
        }

        @Override
        public Query originalQuery() {
            return original.originalQuery();
        }

        @Override
        public List<X> getResultList() {
            return original.getResultList();
        }

        @Override
        public SimpleStream<X> getResultStream() {
            return new SimpleStream<>(original.getResultStream());
        }

        @Override
        public JpaQueryDecorResultProvider<X> modified(Function<List<X>, List<X>> mapper) {
            return new JpaQueryDecorResultProvider<>(original.modified(mapper));
        }

    }

    protected JpaQueryDecor(Class<T_ROOT> rootClass, Class<T_RESULT> resultClass, JpaQueryDecor copy) {
        super(copy);
        this.rootClass = Objects.requireNonNull(rootClass);
        this.resultClass = Objects.requireNonNull(resultClass);
    }

    protected JpaQueryDecor(JpaQueryDecor copy) {
        this(copy.rootClass, copy.resultClass, copy);
    }

    public static <T> JpaQueryDecor<T, T> of(Class<T> root) {
        JpaQueryDecor<T, T> decor = new JpaQueryDecor<>(root, root, null);
        decor.selection = Phase2::root;
        return decor;
    }

    @Override
    protected JpaQueryDecor<T_ROOT, T_RESULT> me() {
        return new JpaQueryDecor<>(this);
    }

    @Override
    public <NEW_ROOT extends T_ROOT> JpaQueryDecor<NEW_ROOT, T_RESULT> usingSubtype(Class<NEW_ROOT> subtype) {
        return new JpaQueryDecor<>(subtype, this.resultClass, this);
    }

    @Override
    public JpaQueryDecor<T_ROOT, T_ROOT> selectingRoot() {
        return F.cast(super.selectingRoot());
    }

    @Override
    public <RES> JpaQueryDecor<T_ROOT, RES> selecting(Class<RES> resClass, Function<Phase2<T_ROOT, Void>, Expression<RES>> func) {
        JpaQueryDecor<T_ROOT, RES> of = new JpaQueryDecor<>(rootClass, resClass, this);
        of.selection = func;
        return of;
    }

    @Override
    public <RES> JpaQueryDecor<T_ROOT, RES> selecting(SingularAttribute<? super T_ROOT, RES> att) {
        return F.cast(super.selecting(att));
    }

    @Override
    public JpaQueryDecor<T_ROOT, Tuple> selectingTuple(Function<Phase2<T_ROOT, Void>, List<Selection<?>>> selections) {
        JpaQueryDecor<T_ROOT, Tuple> of = new JpaQueryDecor<>(rootClass, Tuple.class, this);
        of.multiselection = selections;
        return of;
    }

    @Override
    public JpaQueryDecor<T_ROOT, Tuple> selectingTuple(Selection<?>... selections) {
        return F.cast(super.selectingTuple(selections));
    }

    @Override
    public JpaQueryDecor<T_ROOT, Tuple> selectingTuple(List<Selection<?>> selections) {
        return F.cast(super.selectingTuple(selections));
    }

    @Override
    public JpaQueryDecor<T_ROOT, Tuple> selectingTuple(SingularAttribute<? super T_ROOT, ?>... selections) {
        return F.cast(super.selectingTuple(selections));
    }

    @Override
    public <RES> JpaQueryDecor<T_ROOT, Long> selectingCountDistinct(SingularAttribute<? super T_ROOT, RES> att) {
        return F.cast(super.selectingCountDistinct(att));
    }

    @Override
    public JpaQueryDecor<T_ROOT, Long> selectingCountDistinct() {
        return F.cast(super.selectingCountDistinct());
    }

    @Override
    public <RES> JpaQueryDecor<T_ROOT, Long> selectingCount(SingularAttribute<? super T_ROOT, RES> att) {
        return F.cast(super.selectingCount(att));
    }

    @Override
    public JpaQueryDecor<T_ROOT, Long> selectingCount() {
        return F.cast(super.selectingCount());
    }

    /**
     * Functor pattern
     *
     * @param <U>
     * @param function
     * @return
     */
    public <U> U chain(Function<JpaQueryDecor<T_ROOT, T_RESULT>, ? extends U> function) {
        Objects.requireNonNull(function);
        return function.apply(this);
    }

    @Override
    public SimpleStream<T_RESULT> buildStream(EntityManager em) {
        return MakeStream.from(super.buildStream(em));
    }

    @Override
    public JpaQueryDecorResultProvider<T_RESULT> buildResult(EntityManager em) {
        return new JpaQueryDecorResultProvider<>(super.buildResult(em));
    }

}
