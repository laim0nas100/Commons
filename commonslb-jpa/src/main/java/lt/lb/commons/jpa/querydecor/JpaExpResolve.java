package lt.lb.commons.jpa.querydecor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author laim0nas100
 */
public interface JpaExpResolve<S, C, R extends Path<S>, RR extends Expression<C>> {

    public static interface JpaPathResolve<S, C, R extends Path<S>, RR extends Path<C>> extends JpaExpResolve<S, C, R, RR> {

        public default <EE> JpaPathResolve<S, EE, R, Path<EE>> then(SingularAttribute<? super C, EE> att) {
            Objects.requireNonNull(att);
            return thenPath(s -> s.get(att));
        }

        public default <EE, CC extends Collection<EE>> JpaExpResolve<S, CC, R, Expression<CC>> then(PluralAttribute<C, CC, EE> att) {
            Objects.requireNonNull(att);
            return then(s -> s.get(att));
        }

        public default <K, V, M extends Map<K, V>> JpaExpResolve<S, M, R, Expression<M>> then(MapAttribute<C, K, V> att) {
            Objects.requireNonNull(att);
            return then(s -> s.get(att));
        }
    }

    public static interface JpaFromResolve<S, C, FR, R extends Path<S>, RR extends From<FR, C>> extends JpaPathResolve<S, C, R, RR> {

    }

    public RR resolve(R starting);

    public default <EE, NRR extends Expression<EE>> JpaExpResolve<S, EE, R, NRR> then(Function<? super RR, ? extends NRR> after) {
        Objects.requireNonNull(after);
        return r -> after.apply(resolve(r));
    }

    public default <EE, NRR extends Path<EE>> JpaPathResolve<S, EE, R, NRR> thenPath(Function<? super RR, ? extends NRR> after) {
        Objects.requireNonNull(after);
        return r -> after.apply(resolve(r));
    }

    public default <EE, J, NRR extends From<J, EE>> JpaFromResolve<S, EE, J, R, NRR> thenFrom(Function<? super RR, ? extends NRR> after) {
        Objects.requireNonNull(after);
        return r -> after.apply(resolve(r));
    }

    public static <R, P extends From<R,R>> JpaFromResolve<R, R, R, P, P> of() {
        return r -> r;
    }
    
    public static <R, P extends Path<R>> JpaPathResolve<R, R, P, P> ofPath() {
        return r -> r;
    }

    public static <R, RR, P extends Path<R>> JpaPathResolve<R, RR, P, Path<RR>> of(SingularAttribute<? super R, RR> att) {
        Objects.requireNonNull(att);
        return (P starting) -> starting.get(att);
    }
    
    public static <R, RR, RRR, P extends Path<R>> JpaPathResolve<R, RRR, P, Path<RRR>> of(SingularAttribute<? super R, RR> att1, SingularAttribute<? super RR, RRR> att2) {
        Objects.requireNonNull(att1);
        Objects.requireNonNull(att2);
        return (P starting) -> {
            Path<RR> get1 = starting.get(att1);
            Path<RRR> get2 = get1.get(att2);
            return get2;
        };
    }
    
    public static <R, RR, RRR, RRRR, P extends Path<R>> JpaPathResolve<R, RRRR, P, Path<RRRR>> of(SingularAttribute<? super R, RR> att1, SingularAttribute<? super RR, RRR> att2, SingularAttribute<? super RRR, RRRR> att3) {
        Objects.requireNonNull(att1);
        Objects.requireNonNull(att2);
        Objects.requireNonNull(att3);
        return (P starting) -> {
            Path<RR> get1 = starting.get(att1);
            Path<RRR> get2 = get1.get(att2);
            Path<RRRR> get3 = get2.get(att3);
            return get3;
        };
    }
    
    

}
