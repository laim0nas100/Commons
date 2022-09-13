package lt.lb.commons.jpa.querydecor;

import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author laim0nas100
 */
public abstract class JpaDecorHelp {

    public static <ROOT, CTX, T> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> equal(SingularAttribute<? super ROOT, T> att, T value) {
        return equal(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX, T> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> equal(JpaExpResolve<? super ROOT, ? extends T, ? super Path<ROOT>, ? extends Expression<T>, CTX> path, T value) {
        Objects.requireNonNull(path);
        return p -> p.cb().equal(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX, T> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> notEqual(JpaExpResolve<? super ROOT, ? extends T, ? super Path<ROOT>, ? extends Expression<T>, CTX> path, T value) {
        Objects.requireNonNull(path);
        return p -> p.cb().notEqual(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX, T> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> notEqual(SingularAttribute<? super ROOT, T> att, T value) {
        return notEqual(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> isTrue(JpaExpResolve<? super ROOT, Boolean, ? super Path<ROOT>, ? extends Expression<Boolean>, CTX> path) {
        Objects.requireNonNull(path);
        return p -> p.cb().isTrue(path.apply(p, p.root()));
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> isTrue(SingularAttribute<? super ROOT, Boolean> att) {
        return isTrue(JpaExpResolve.of(att));
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> isFalse(JpaExpResolve<? super ROOT, Boolean, ? super Path<ROOT>, ? extends Expression<Boolean>, CTX> path) {
        Objects.requireNonNull(path);
        return p -> p.cb().isFalse(path.apply(p, p.root()));
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> isFalse(SingularAttribute<? super ROOT, Boolean> att) {
        return isFalse(JpaExpResolve.of(att));
    }

    public static <ROOT, CTX, T> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> isNull(SingularAttribute<? super ROOT, T> att) {
        return isNull(JpaExpResolve.of(att));
    }

    public static <ROOT, CTX, T> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> isNull(JpaExpResolve<? super ROOT, ? extends T, ? super Path<ROOT>, ? extends Expression<T>, CTX> path) {
        Objects.requireNonNull(path);
        return p -> p.cb().isNull(path.apply(p, p.root()));
    }

    public static <ROOT, CTX, T> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> isNotNull(SingularAttribute<? super ROOT, T> att) {
        return isNotNull(JpaExpResolve.of(att));
    }

    public static <ROOT, CTX, T> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> isNotNull(JpaExpResolve<? super ROOT, ? extends T, ? super Path<ROOT>, ? extends Expression<T>, CTX> path) {
        Objects.requireNonNull(path);
        return p -> p.cb().isNotNull(path.apply(p, p.root()));
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> greaterThan(SingularAttribute<? super ROOT, T> att, T value) {
        return greaterThan(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> greaterThan(JpaExpResolve<? super ROOT, ? extends T, ? super Path<ROOT>, ? extends Expression<T>, CTX> path, T value) {
        Objects.requireNonNull(path);
        return p -> p.cb().greaterThan(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> greaterThanOrEqualTo(SingularAttribute<? super ROOT, T> att, T value) {
        return greaterThanOrEqualTo(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> greaterThanOrEqualTo(JpaExpResolve<? super ROOT, ? extends T, ? super Path<ROOT>, ? extends Expression<T>, CTX> path, T value) {
        Objects.requireNonNull(path);
        return p -> p.cb().greaterThanOrEqualTo(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> lessThan(SingularAttribute<? super ROOT, T> att, T value) {
        return lessThan(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> lessThan(JpaExpResolve<? super ROOT, ? extends T, ? super Path<ROOT>, ? extends Expression<T>, CTX> path, T value) {
        Objects.requireNonNull(path);
        return p -> p.cb().lessThan(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> lessThanOrEqualTo(SingularAttribute<? super ROOT, T> att, T value) {
        return lessThanOrEqualTo(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> lessThanOrEqualTo(JpaExpResolve<? super ROOT, ? extends T, ? super Path<ROOT>, ? extends Expression<T>, CTX> path, T value) {
        Objects.requireNonNull(path);
        return p -> p.cb().lessThanOrEqualTo(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> between(SingularAttribute<? super ROOT, T> att, T first, T second) {
        return between(JpaExpResolve.of(att), first, second);
    }

    public static <ROOT, CTX, T extends Comparable<? super T>> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> between(JpaExpResolve<? super ROOT, ? extends T, ? super Path<ROOT>, ? extends Expression<T>, CTX> path, T first, T second) {
        Objects.requireNonNull(path);
        return p -> p.cb().between(path.apply(p, p.root()), first, second);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> gt(SingularAttribute<? super ROOT, ? extends Number> att, Number value) {
        return gt(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> gt(JpaExpResolve<? super ROOT, ? extends Number, ? super Path<ROOT>, ? extends Expression<? extends Number>, CTX> path, Number value) {
        Objects.requireNonNull(path);
        return p -> p.cb().gt(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> ge(SingularAttribute<? super ROOT, ? extends Number> att, Number value) {
        return ge(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> ge(JpaExpResolve<? super ROOT, ? extends Number, ? super Path<ROOT>, ? extends Expression<? extends Number>, CTX> path, Number value) {
        Objects.requireNonNull(path);
        return p -> p.cb().ge(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> lt(SingularAttribute<? super ROOT, ? extends Number> att, Number value) {
        return lt(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> lt(JpaExpResolve<? super ROOT, ? extends Number, ? super Path<ROOT>, ? extends Expression<? extends Number>, CTX> path, Number value) {
        Objects.requireNonNull(path);
        return p -> p.cb().lt(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> le(SingularAttribute<? super ROOT, ? extends Number> att, Number value) {
        return le(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> le(JpaExpResolve<? super ROOT, ? extends Number, ? super Path<ROOT>, ? extends Expression<? extends Number>, CTX> path, Number value) {
        Objects.requireNonNull(path);
        return p -> p.cb().le(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> like(SingularAttribute<? super ROOT, String> att, String value) {
        return like(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> like(JpaExpResolve<? super ROOT, String, ? super Path<ROOT>, ? extends Expression<String>, CTX> path, String value) {
        Objects.requireNonNull(path);
        return p -> p.cb().like(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> like(SingularAttribute<? super ROOT, String> att, String value, char escapeChar) {
        return like(JpaExpResolve.of(att), value, escapeChar);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> like(JpaExpResolve<? super ROOT, String, ? super Path<ROOT>, ? extends Expression<String>, CTX> path, String value, char escapeChar) {
        Objects.requireNonNull(path);
        return p -> p.cb().like(path.apply(p, p.root()), value, escapeChar);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> notLike(SingularAttribute<? super ROOT, String> att, String value) {
        return notLike(JpaExpResolve.of(att), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> notLike(JpaExpResolve<? super ROOT, String, ? super Path<ROOT>, ? extends Expression<String>, CTX> path, String value) {
        Objects.requireNonNull(path);
        return p -> p.cb().notLike(path.apply(p, p.root()), value);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> notLike(SingularAttribute<? super ROOT, String> att, String value, char escapeChar) {
        return notLike(JpaExpResolve.of(att), value, escapeChar);
    }

    public static <ROOT, CTX> Function<DecoratorPhases.Phase2<ROOT, CTX>, Predicate> notLike(JpaExpResolve<? super ROOT, String, ? super Path<ROOT>, ? extends Expression<String>, CTX> path, String value, char escapeChar) {
        Objects.requireNonNull(path);
        return p -> p.cb().notLike(path.apply(p, p.root()), value, escapeChar);
    }

}
