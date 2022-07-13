package lt.lb.commons.jpa.querydecor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase1;

/**
 *
 * @author laim0nas100
 */
public interface JpaExpResolve<SOURCE, CURRENT, SOURCE_P extends Path<SOURCE>, CURRENT_P extends Expression<CURRENT>, CTX> {

    public static interface JpaPathResolve<SOURCE, CURRENT, SOURCE_P extends Path<SOURCE>, CURRENT_P extends Path<CURRENT>, CTX> extends JpaExpResolve<SOURCE, CURRENT, SOURCE_P, CURRENT_P, CTX> {

        public default <EE, NRR extends Path<EE>> JpaPathResolve<SOURCE, EE, SOURCE_P, NRR, CTX> thenPath(BiFunction<Phase1<CTX>, ? super CURRENT_P, ? extends NRR> after) {
            Objects.requireNonNull(after);
            return (ph, r) -> after.apply(ph, resolve(ph, r));
        }

        public default <EE> JpaPathResolve<SOURCE, EE, SOURCE_P, Path<EE>, CTX> get(SingularAttribute<? super CURRENT, EE> att) {
            Objects.requireNonNull(att);
            return thenPath((ph, r) -> r.get(att));
        }

        public default <EE, CC extends Collection<EE>> JpaExpResolve<SOURCE, CC, SOURCE_P, Expression<CC>, CTX> get(PluralAttribute<CURRENT, CC, EE> att) {
            Objects.requireNonNull(att);
            return then((ph, r) -> r.get(att));
        }

        public default <K, V, M extends Map<K, V>> JpaExpResolve<SOURCE, M, SOURCE_P, Expression<M>, CTX> get(MapAttribute<CURRENT, K, V> att) {
            Objects.requireNonNull(att);
            return then((ph, r) -> r.get(att));
        }
    }

    public CURRENT_P resolve(Phase1<CTX> phase, SOURCE_P starting);

    public default Function<DecoratorPhases.Phase2<SOURCE, CTX>, Predicate> in(CURRENT... objects) {
        Objects.requireNonNull(objects);
        if(objects.length == 0){
            throw new IllegalArgumentException("Object array using 'in' via JpaExpResolve is empty");
        }
        return f -> resolve(f, (SOURCE_P) f.root()).in(objects);
    }
    
    public default Function<DecoratorPhases.Phase2<SOURCE, CTX>, Predicate> in(Collection<CURRENT> objects) {
        Objects.requireNonNull(objects);
        if(objects.isEmpty()){
            throw new IllegalArgumentException("Object collection using 'in' via JpaExpResolve is empty");
        }
        return f -> resolve(f, (SOURCE_P) f.root()).in(objects);
    }
    
    public default Function<DecoratorPhases.Phase2<SOURCE, CTX>, Predicate> notIn(CURRENT... objects) {
        Objects.requireNonNull(objects);
        if(objects.length == 0){
            throw new IllegalArgumentException("Object array using 'in' via JpaExpResolve is empty");
        }
        return f -> resolve(f, (SOURCE_P) f.root()).in(objects).not();
    }
    
    public default Function<DecoratorPhases.Phase2<SOURCE, CTX>, Predicate> notIn(Collection<CURRENT> objects) {
        Objects.requireNonNull(objects);
        if(objects.isEmpty()){
            throw new IllegalArgumentException("Object collection using 'in' via JpaExpResolve is empty");
        }
        return f -> resolve(f, (SOURCE_P) f.root()).in(objects).not();
    }

    public default <EE, NRR extends Expression<EE>> JpaExpResolve<SOURCE, EE, SOURCE_P, NRR, CTX> then(BiFunction<Phase1<CTX>, ? super CURRENT_P, ? extends NRR> after) {
        Objects.requireNonNull(after);
        return (ph, r) -> after.apply(ph, resolve(ph, r));
    }

    public static <R, CTX> JpaRootResolve<R, CTX> of() {
        return (ph, r) -> r;
    }

    public static <R> JpaRootResolve<R, ?> ofRoot() {
        return of();
    }

    public static <R, CTX> JpaRootResolve<R, CTX> ofRoot(CTX ctx) {
        return (ph, r) -> r;
    }

    public static <R> JpaRootResolve<R, ?> ofRoot(Attribute<R, ?> att) {
        return of();
    }

    public static <R, CTX> JpaRootResolve<R, CTX> ofRoot(CTX ctx, Attribute<R, ?> att) {
        return (ph, r) -> r;
    }

    public static <R, P extends Path<R>, CTX> JpaPathResolve<R, R, P, P, CTX> ofPath() {
        return (ph, r) -> r;
    }

    public static interface JpaFromResolve<SOURCE, CURRENT, SOURCE_P extends From<?, SOURCE>, CURRENT_P extends From<?, CURRENT>, CTX>
            extends JpaPathResolve<SOURCE, CURRENT, SOURCE_P, CURRENT_P, CTX> {

        public default <NEW, NEW_P extends From<CURRENT, NEW>> JpaFromResolve<SOURCE, NEW, SOURCE_P, NEW_P, CTX> thenFrom(BiFunction<Phase1<CTX>, ? super CURRENT_P, ? extends NEW_P> after) {
            Objects.requireNonNull(after);
            return (ph, r) -> after.apply(ph, resolve(ph, r));
        }

        public default <NEW, CTX>
                JpaFromResolve<SOURCE, NEW, SOURCE_P, Join<CURRENT, NEW>, CTX>
                join(SingularAttribute<? super CURRENT, NEW> att, JoinType joinType) {
            Nulls.requireNonNulls(att, joinType);
            return F.cast(thenFrom((ph, r) -> r.join(att, joinType)));
        }

        public default <NEW, CTX>
                JpaFromResolve<SOURCE, NEW, SOURCE_P, CollectionJoin<CURRENT, NEW>, CTX>
                join(CollectionAttribute<? super CURRENT, NEW> att, JoinType joinType) {
            Nulls.requireNonNulls(att, joinType);
            return F.cast(thenFrom((ph, r) -> r.join(att, joinType)));
        }

        public default <NEW, CTX>
                JpaFromResolve<SOURCE, NEW, SOURCE_P, ListJoin<CURRENT, NEW>, CTX>
                join(ListAttribute<? super CURRENT, NEW> att, JoinType joinType) {
            Nulls.requireNonNulls(att, joinType);
            return F.cast(thenFrom((ph, r) -> r.join(att, joinType)));
        }

        public default <NEW, CTX>
                JpaFromResolve<SOURCE, NEW, SOURCE_P, SetJoin<CURRENT, NEW>, CTX>
                join(SetAttribute<? super CURRENT, NEW> att, JoinType joinType) {
            Nulls.requireNonNulls(att, joinType);
            return F.cast(thenFrom((ph, r) -> r.join(att, joinType)));
        }

        public default <NEW, K, NEW_P extends MapJoin<CURRENT, K, NEW>, CTX>
                JpaFromResolve<SOURCE, NEW, SOURCE_P, NEW_P, CTX>
                join(MapAttribute<? super CURRENT, K, NEW> att, JoinType joinType) {
            Nulls.requireNonNulls(att, joinType);
            return F.cast(thenFrom((ph, r) -> r.join(att, joinType)));
        }

    }

    public static interface JpaRootResolve<SOURCE, CTX> extends JpaFromResolve<SOURCE, SOURCE, Root<SOURCE>, Root<SOURCE>, CTX> {

    }

    public static <SOURCE, CURRENT, P extends From<SOURCE, SOURCE>, CTX> JpaFromResolve<SOURCE, CURRENT, P, Join<SOURCE, CURRENT>, CTX> ofJoin(SingularAttribute<? super SOURCE, CURRENT> att, JoinType joinType) {
        Nulls.requireNonNulls(att, joinType);
        return (ph, r) -> r.join(att, joinType);
    }

    public static <SOURCE, CURRENT, CTX> JpaFromResolve<SOURCE, CURRENT, From<SOURCE, SOURCE>, CollectionJoin<SOURCE, CURRENT>, CTX> ofJoin(CollectionAttribute<? super SOURCE, CURRENT> att, JoinType joinType) {
        Nulls.requireNonNulls(att, joinType);
        return (ph, r) -> r.join(att, joinType);
    }

    public static <SOURCE, CURRENT, CTX> JpaFromResolve<SOURCE, CURRENT, From<SOURCE, SOURCE>, ListJoin<SOURCE, CURRENT>, CTX> ofJoin(ListAttribute<? super SOURCE, CURRENT> att, JoinType joinType) {
        Nulls.requireNonNulls(att, joinType);
        return (ph, r) -> r.join(att, joinType);
    }

    public static <SOURCE, CURRENT, CTX> JpaFromResolve<SOURCE, CURRENT, From<SOURCE, SOURCE>, SetJoin<SOURCE, CURRENT>, CTX> ofJoin(SetAttribute<? super SOURCE, CURRENT> att, JoinType joinType) {
        Nulls.requireNonNulls(att, joinType);
        return (ph, r) -> r.join(att, joinType);
    }

    public static <SOURCE, K, CURRENT, CTX> JpaFromResolve<SOURCE, CURRENT, From<SOURCE, SOURCE>, MapJoin<SOURCE, K, CURRENT>, CTX> ofJoin(MapAttribute<? super SOURCE, K, CURRENT> att, JoinType joinType) {
        Nulls.requireNonNulls(att, joinType);
        return (ph, r) -> r.join(att, joinType);
    }

    public static <SOURCE, CURRENT, P extends Path<SOURCE>, CTX> JpaPathResolve<SOURCE, CURRENT, P, Path<CURRENT>, CTX> of(SingularAttribute<? super SOURCE, CURRENT> att) {
        Nulls.requireNonNulls(att);
        return (ph, r) -> r.get(att);
    }

    public static <R, R1, R2, P extends Path<R>, CTX> JpaPathResolve<R, R2, P, Path<R2>, CTX> of(SingularAttribute<? super R, R1> att1, SingularAttribute<? super R1, R2> att2) {
        Nulls.requireNonNulls(att1, att2);
        return (ph, r) -> {
            Path<R1> get1 = r.get(att1);
            Path<R2> get2 = get1.get(att2);
            return get2;
        };
    }

    public static <R, R1, R2, R3, P extends Path<R>, CTX> JpaPathResolve<R, R3, P, Path<R3>, CTX> of(SingularAttribute<? super R, R1> att1, SingularAttribute<? super R1, R2> att2, SingularAttribute<? super R2, R3> att3) {
        Nulls.requireNonNulls(att1, att2, att3);
        return (ph, r) -> {
            Path<R1> get1 = r.get(att1);
            Path<R2> get2 = get1.get(att2);
            Path<R3> get3 = get2.get(att3);
            return get3;
        };
    }

    public static <R, R1, R2, R3, R4, P extends Path<R>, CTX> JpaPathResolve<R, R4, P, Path<R4>, CTX> of(
            SingularAttribute<? super R, R1> att1,
            SingularAttribute<? super R1, R2> att2,
            SingularAttribute<? super R2, R3> att3,
            SingularAttribute<? super R3, R4> att4
    ) {
        Nulls.requireNonNulls(att1, att2, att3, att4);
        return (ph, r) -> {
            Path<R1> get1 = r.get(att1);
            Path<R2> get2 = get1.get(att2);
            Path<R3> get3 = get2.get(att3);
            Path<R4> get4 = get3.get(att4);
            return get4;
        };
    }

}
