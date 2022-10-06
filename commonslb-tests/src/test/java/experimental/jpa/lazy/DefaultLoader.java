package experimental.jpa.lazy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.jpa.EntityFacade;
import lt.lb.commons.jpa.EntityManagerAware;
import lt.lb.commons.jpa.ids.GenericIDFactory;
import lt.lb.commons.jpa.ids.IDFactory;
import experimental.jpa.lazy.LazyLoadContext;
import experimental.jpa.lazy.LazyLoadResult;
import experimental.jpa.lazy.LazyLoadType;
import experimental.jpa.lazy.LazyLoaderIds;
import experimental.jpa.lazy.LazySearcher;
import lt.lb.commons.jpa.querydecor.JpaQueryDecor;

/**
 *
 * @author laim0nas100
 */
public class DefaultLoader<ID, T> implements LazyLoaderIds<ID, T> {

    protected IDFactory<ID> factory;
    protected EntityManagerAware emAware;
    protected Class<T> entityClass;
    protected SingularAttribute<? super T, ID> idPath;
    protected Function<LazyLoadContext, LazyLoadResult<ID>> searcher;

    public DefaultLoader(EntityManagerAware emAware, IDFactory<ID> idFac, Class<T> entityClass, SingularAttribute<? super T, ID> idPath, Function<LazyLoadContext, LazyLoadResult<ID>> searcher) {
        this.emAware = emAware;
        this.factory = idFac;
        this.idPath = idPath;
        this.entityClass = entityClass;
        this.searcher = searcher;
    }

    @Override
    public LazyLoadResult<ID> loadIds(LazyLoadContext ctx) {
        if (ctx.isLoadList() || ctx.isOptimal()) {
            return searcher.apply(ctx);
        } else {
            throw new IllegalArgumentException("Supported load types are:" + Arrays.asList(LazyLoadType.IDS, LazyLoadType.OPTIMAL) + " and recieved:" + ctx.getType());
        }
// return getFacade().search(getSearch(), getSortOrder(), LazyLoadContext.list(Long.valueOf(position), getSortOrder().getPageSize()));
    }

    public SingularAttribute<? super T, ID> getIdPath() {
        return idPath;
    }

    @Override
    public List<T> lazyLoad(LazyLoadResult<ID> ids) {
        LazyLoadType type = ids.getType();
        switch (type) {
            case IDS:
            case GROUPED:
            case GROUPED_COUNT:
                return getList(ids.getIds(), getIdPath());
            default:
                throw new IllegalArgumentException("Unsupported load type " + type);
        }
    }

    protected Class<T> getEntityClass() {
        return this.entityClass;
    }

    protected List<T> getList(List<ID> ids, SingularAttribute<? super T, ID> idPath) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter ids is required");
        }
        if (idPath == null) {
            throw new IllegalArgumentException("Parameter idPath is required");
        }
        if (ids.isEmpty()) {
            return ImmutableCollections.listOf();
        }

        return JpaQueryDecor.of(getEntityClass())
                .withPred(idPath, (cb, id) -> id.in(ids))
                .setDistinct(true)
                .withResultModification(list -> orderByPrimaryKey(list, ids))
                .buildList(emAware.getEntityManager());
    }

    protected List<T> orderByPrimaryKey(List<T> original, Collection<ID> primaryKeys) {

        Map<ID, T> map = new HashMap<>(original.size());
        IDFactory.IdGetter<T, ID> idGetter = factory.idGetter(getEntityClass());
        for (T item : original) {
            map.put(idGetter.apply(item), item);
        }

        List<T> ordered = new ArrayList<>(primaryKeys.size());
        for (ID id : primaryKeys) {
            if (map.containsKey(id)) {
                ordered.add(map.get(id));
            }
        }
        return ordered;
    }

}
