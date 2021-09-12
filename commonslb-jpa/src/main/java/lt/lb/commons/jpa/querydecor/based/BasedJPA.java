package lt.lb.commons.jpa.querydecor.based;

import java.util.List;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.jpa.querydecor.DecoratorPhases;

/**
 *
 * @author laim0nas100
 */
public abstract class BasedJPA {

    public static abstract class BasedGenericDecorator<ROOT, RES, CTX, M extends BasedGenericDecorator<ROOT, RES, CTX, M>> extends BasedJpaQueryDecor<ROOT, RES, CTX, M> implements ICommonRootQuery<ROOT, CTX, M> {

        public BasedGenericDecorator(BasedGenericDecorator copy) {
            super(copy);
        }

        public BasedGenericDecorator(CTX context, Class<ROOT> rootClass, Class<RES> resultClass, BasedGenericDecorator copy) {
            super(context, rootClass, resultClass, copy);
        }

        protected abstract <NEW_ROOT> BasedGenericDecorator<NEW_ROOT, ?, CTX, ?> construct(CTX context, Class<NEW_ROOT> rootClass, Class<?> resultClass, BasedGenericDecorator copy);

        @Override
        public <NEW_ROOT extends ROOT> BasedGenericDecorator<NEW_ROOT, RES, CTX, ?> usingSubtype(Class<NEW_ROOT> subtype) {
            return (BasedGenericDecorator) construct(getContext(), subtype, getResultClass(), this);
        }

        @Override
        public <RES> BasedGenericDecorator<ROOT, RES, CTX, ?> selecting(Class<RES> resClass, Function<DecoratorPhases.Phase2<ROOT, CTX>, Expression<RES>> func) {
            BasedGenericDecorator<ROOT, RES, CTX, ?> based = (BasedGenericDecorator) construct(getContext(), getRootClass(), resClass, this);
            based.selection = func;
            return based;
        }

        @Override
        public BasedGenericDecorator<ROOT, Tuple, CTX, ?> selectingTuple(Function<DecoratorPhases.Phase2<ROOT, CTX>, List<Selection<?>>> selections) {
            BasedGenericDecorator<ROOT, Tuple, CTX, ?> based = (BasedGenericDecorator) construct(getContext(), getRootClass(), Tuple.class, this);
            based.multiselection = selections;
            return based;
        }

    }

    public static class BasedDelete<ROOT, CTX> extends BasedGenericDecorator<ROOT, Void, CTX, BasedDelete<ROOT, CTX>> implements IDeleteQueryDecor<ROOT, CTX, BasedDelete<ROOT, CTX>> {

        public BasedDelete(BasedGenericDecorator copy) {
            super(copy);
        }

        public BasedDelete(CTX context, Class<ROOT> rootClass, Class resultClass, BasedGenericDecorator copy) {
            super(context, rootClass, resultClass, copy);
        }

        @Override
        protected BasedDelete<ROOT, CTX> me() {
            return new BasedDelete<>(this);
        }

        @Override
        public CriteriaDelete<ROOT> produceQuery(EntityManager em) {
            return produceDeleteQuery(em);
        }

        @Override
        protected <NEW_ROOT> BasedDelete<NEW_ROOT, CTX> construct(CTX context, Class<NEW_ROOT> rootClass, Class<?> resultClass, BasedGenericDecorator copy) {
             return new BasedDelete<>(context, rootClass, resultClass, copy);
        }
    }

    public static class BasedUpdate<ROOT, CTX> extends BasedGenericDecorator<ROOT, Void, CTX, BasedUpdate<ROOT, CTX>> implements IUpdateQueryDecor<ROOT, CTX, BasedUpdate<ROOT, CTX>> {

        public BasedUpdate(BasedGenericDecorator copy) {
            super(copy);
        }

        public BasedUpdate(CTX context, Class<ROOT> rootClass, Class resultClass, BasedGenericDecorator copy) {
            super(context, rootClass, resultClass, copy);
        }

        @Override
        protected BasedUpdate<ROOT, CTX> me() {
            return new BasedUpdate<>(this);
        }

        @Override
        public CriteriaUpdate<ROOT> produceQuery(EntityManager em) {
            return produceUpdateQuery(em);
        }

        @Override
        protected <NEW_ROOT> BasedUpdate<NEW_ROOT, CTX> construct(CTX context, Class<NEW_ROOT> rootClass, Class<?> resultClass, BasedGenericDecorator copy) {
            return new BasedUpdate<>(context, rootClass, resultClass, copy);
        }
    }

    public static abstract class BasedGenericDecoratorTyped<ROOT, RES, CTX, M extends BasedGenericDecoratorTyped<ROOT, RES, CTX, M>> extends BasedJpaQueryDecor<ROOT, RES, CTX, M> implements IAbstractQueryDecor<ROOT, RES, CTX, M> {

        public BasedGenericDecoratorTyped(BasedGenericDecoratorTyped copy) {
            super(copy);
        }

        public BasedGenericDecoratorTyped(CTX context, Class<ROOT> rootClass, Class<RES> resultClass, BasedGenericDecoratorTyped copy) {
            super(context, rootClass, resultClass, copy);
        }

        protected abstract <NEW_ROOT, NEW_RES> BasedGenericDecoratorTyped<NEW_ROOT, NEW_RES, CTX, ?> construct(CTX context, Class<NEW_ROOT> rootClass, Class<NEW_RES> resultClass, BasedGenericDecoratorTyped copy);

        @Override
        public <NEW_ROOT extends ROOT> BasedGenericDecoratorTyped<NEW_ROOT, RES, CTX, ?> usingSubtype(Class<NEW_ROOT> subtype) {
            return construct(getContext(), subtype, getResultClass(), this);
        }

        @Override
        public <RES> BasedGenericDecoratorTyped<ROOT, RES, CTX, ?> selecting(Class<RES> resClass, Function<DecoratorPhases.Phase2<ROOT, CTX>, Expression<RES>> func) {
            BasedGenericDecoratorTyped<ROOT, RES, CTX, ?> based = construct(getContext(), getRootClass(), resClass, this);
            based.selection = func;
            return based;
        }

        @Override
        public BasedGenericDecoratorTyped<ROOT, Tuple, CTX, ?> selectingTuple(Function<DecoratorPhases.Phase2<ROOT, CTX>, List<Selection<?>>> selections) {
            BasedGenericDecoratorTyped<ROOT, Tuple, CTX, ?> based = construct(getContext(), getRootClass(), Tuple.class, this);
            based.multiselection = selections;
            return based;
        }

        @Override
        public BasedGenericDecoratorTyped<ROOT, Tuple, CTX, ?> selectingTuple(SingularAttribute<? super ROOT, ?>... selections) {
            return (BasedGenericDecoratorTyped) IAbstractQueryDecor.super.selectingTuple(selections);
        }

        @Override
        public BasedGenericDecoratorTyped<ROOT, ROOT, CTX, ?> selectingRoot() {
            return (BasedGenericDecoratorTyped) IAbstractQueryDecor.super.selectingRoot();
        }

        @Override
        public <RES> BasedGenericDecoratorTyped<ROOT, RES, CTX, ?> selecting(SingularAttribute<? super ROOT, RES> att) {
            return (BasedGenericDecoratorTyped) IAbstractQueryDecor.super.selecting(att);
        }

        @Override
        public BasedGenericDecoratorTyped<ROOT, Long, CTX, ?> selectingCount() {
            return (BasedGenericDecoratorTyped) IAbstractQueryDecor.super.selectingCount();
        }

        @Override
        public <RES> BasedGenericDecoratorTyped<ROOT, Long, CTX, ?> selectingCount(SingularAttribute<? super ROOT, RES> att) {
            return (BasedGenericDecoratorTyped) IAbstractQueryDecor.super.selectingCount(att);
        }

        @Override
        public BasedGenericDecoratorTyped<ROOT, Long, CTX, ?> selectingCountDistinct() {
            return (BasedGenericDecoratorTyped) IAbstractQueryDecor.super.selectingCountDistinct();
        }

        @Override
        public <RES> BasedGenericDecoratorTyped<ROOT, Long, CTX, ?> selectingCountDistinct(SingularAttribute<? super ROOT, RES> att) {
            return (BasedGenericDecoratorTyped) IAbstractQueryDecor.super.selectingCountDistinct(att);
        }

    }

    public static class BasedCriteria<ROOT, RES, CTX> extends BasedGenericDecoratorTyped<ROOT, RES, CTX, BasedCriteria<ROOT, RES, CTX>> implements ICriteriaQueryDecor<ROOT, RES, CTX, BasedCriteria<ROOT, RES, CTX>> {

        public BasedCriteria(BasedGenericDecoratorTyped copy) {
            super(copy);
        }

        public BasedCriteria(CTX context, Class<ROOT> rootClass, Class<RES> resultClass, BasedGenericDecoratorTyped copy) {
            super(context, rootClass, resultClass, copy);
        }

        @Override
        protected BasedCriteria<ROOT, RES, CTX> me() {
            return new BasedCriteria<>(this);
        }

        @Override
        public CriteriaQuery<RES> produceQuery(EntityManager em) {
            return produceCriteriaQuery(em);
        }

        @Override
        public TypedQuery<RES> build(EntityManager em) {
            return buildCriteriaQuery(em);
        }

        @Override
        protected <NEW_ROOT, NEW_RES> BasedCriteria<NEW_ROOT, NEW_RES, CTX> construct(CTX context, Class<NEW_ROOT> rootClass, Class<NEW_RES> resultClass, BasedGenericDecoratorTyped copy) {
            return new BasedCriteria<>(context, rootClass, resultClass, copy);
        }
    }

    public static class BasedSubCriteria<ROOT, RES, CTX> extends BasedGenericDecoratorTyped<ROOT, RES, CTX, BasedSubCriteria<ROOT, RES, CTX>> implements ISubqueryDecor<ROOT, RES, CTX, BasedSubCriteria<ROOT, RES, CTX>> {

        public BasedSubCriteria(BasedGenericDecoratorTyped copy) {
            super(copy);
        }

        public BasedSubCriteria(CTX context, Class<ROOT> rootClass, Class<RES> resultClass, BasedGenericDecoratorTyped copy) {
            super(context, rootClass, resultClass, copy);
        }

        @Override
        protected BasedSubCriteria<ROOT, RES, CTX> me() {
            return new BasedSubCriteria<>(this);
        }

        @Override
        protected <NEW_ROOT, NEW_RES> BasedSubCriteria<NEW_ROOT, NEW_RES, CTX> construct(CTX context, Class<NEW_ROOT> rootClass, Class<NEW_RES> resultClass, BasedGenericDecoratorTyped copy) {
            return new BasedSubCriteria<>(context, rootClass, resultClass, copy);
        }

    }

    public static <T, C> BasedDelete<T, C> deleteQuery(Class<T> cls, C ctx) {
        BasedDelete<T, C> based = new BasedDelete<>(ctx, cls, Void.class, null);
        return based;
    }

    public static <T, C> BasedUpdate<T, C> updateQuery(Class<T> cls, C ctx) {
        BasedUpdate<T, C> based = new BasedUpdate<>(ctx, cls, Void.class, null);
        return based;
    }

    public static <T, C> BasedCriteria<T, T, C> criteriaQuery(Class<T> cls, C ctx) {
        BasedCriteria<T, T, C> based = new BasedCriteria<>(ctx, cls, cls, null);
        return based;
    }

    public static <T, C> BasedSubCriteria<T, T, C> criteriaSubQuery(Class<T> cls, C ctx) {
        BasedSubCriteria<T, T, C> based = new BasedSubCriteria<>(ctx, cls, cls, null);
        return based;
    }

}
