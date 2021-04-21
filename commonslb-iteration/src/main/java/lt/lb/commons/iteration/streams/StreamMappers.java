package lt.lb.commons.iteration.streams;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.Ins;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.Equator;

/**
 * Collection of functors used for StreamMapper objects
 *
 * @author laim0nas100
 */
public abstract class StreamMappers {

    /**
     * Applies filter functor
     *
     * @param <T>
     * @param <Z>
     * @param object
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> filterEquals(Z object) {
        return filterPredicate(ob -> Objects.equals(ob, object));
    }

    /**
     * Applies filter functor
     *
     * @param <T>
     * @param <Z>
     * @param object
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> filterNotEquals(Z object) {
        return filterPredicate(ob -> !Objects.equals(ob, object));
    }

    /**
     * Adds filtering of null elements
     *
     * @param <T>
     * @param <Z>
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> filterNoNulls() {
        return filterPredicate(s -> s != null);
    }

    /**
     * Adds filtering by given predicate
     *
     * @param <T>
     * @param <Z>
     * @param predicate
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> filterPredicate(Predicate<? super Z> predicate) {
        Objects.requireNonNull(predicate);
        return s -> s.filter(predicate);
    }

    /**
     * Adds filtering by given predicate which was negated
     *
     * @param <T>
     * @param <Z>
     * @param predicate
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> filterNotPredicate(Predicate<? super Z> predicate) {
        Objects.requireNonNull(predicate);
        Predicate<? super Z> negate = predicate.negate();
        return s -> s.filter(negate);
    }

    /**
     * Combines filter and map operation to select only specified type
     *
     * @param <T>
     * @param <Z>
     * @param <R>
     * @param cls
     * @return
     */
    public static <T, Z, R> Function<StreamMapper<T, Z>, StreamMapper<T, R>> select(Class<R> cls) {
        return s -> s.filter(cls::isInstance).map(m -> (R) m);
    }

    /**
     * Filter operation to select only specified types. If array is empty, does
     * nothing.
     *
     * @param <T>
     * @param <Z>
     * @param cls
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> selectTypes(Class... cls) {
        return st -> {
            if (cls.length == 0) {
                return st;
            }
            return st.filter(s -> Ins.ofNullable(s).instanceOfAny(cls));
        };
    }

    /**
     * Replace every null instance with some default value
     *
     * @param <T>
     * @param <Z>
     * @param nullCase default value
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> nullWrap(Supplier<? extends Z> nullCase) {
        Objects.requireNonNull(nullCase, "nullCase is null");
        return st -> st.map(s -> s == null ? nullCase.get() : s);
    }

    /**
     * Applies distinct functor based on custom equator
     *
     * @param <T>
     * @param <Z>
     * @param eq
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> distinct(Equator<Z> eq) {
        Objects.requireNonNull(eq, "Equator is null");
        return st -> st.map(s -> new Equator.EqualityProxy<>(s, eq)).distinct().map(m -> m.getValue());
    }

    /**
     * Applies concat functor with given iterable appending the stream
     *
     * @param <T>
     * @param <Z>
     * @param it iterable
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> concat(Iterable<Z> it) {
        Objects.requireNonNull(it, "Iterable is null");
        return st -> st.then(s -> Stream.concat(s, StreamMapper.fromIterable(it)));
    }
    
    /**
     * Applies concat functor with given stream appending the stream
     *
     * @param <T>
     * @param <Z>
     * @param stream 
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> concat(Stream<Z> stream) {
        Objects.requireNonNull(stream, "Iterable is null");
        return st -> st.then(s -> Stream.concat(s, stream));
    }

    /**
     * Applies concat functor with given iterable prepending the stream
     *
     * @param <T>
     * @param <Z>
     * @param it iterable
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> concatFirst(Iterable<Z> it) {
        Objects.requireNonNull(it, "Iterable is null");
        return st -> st.then(s -> Stream.concat(StreamMapper.fromIterable(it), s));
    }
    
    /**
     * Applies concat functor with given stream prepending the stream
     *
     * @param <T>
     * @param <Z>
     * @param stream 
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> concatFirst(Stream<Z> stream) {
        Objects.requireNonNull(stream, "Iterable is null");
        return st -> st.then(s -> Stream.concat(stream,s));
    }

    /**
     * Applies concat functor with given array appending the stream
     *
     * @param <T>
     * @param <Z>
     * @param it iterable
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> concat(Z... it) {
        Objects.requireNonNull(it, "Iterable is null");
        return st -> st.then(s -> Stream.concat(s, StreamMapper.fromArray(it)));
    }

    /**
     * Applies concat functor with given array prepending the stream
     *
     * @param <T>
     * @param <Z>
     * @param it iterable
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> concatFirst(Z... it) {
        Objects.requireNonNull(it, "Iterable is null");
        return st -> st.then(s -> Stream.concat(StreamMapper.fromArray(it), s));
    }

    /**
     * Applies functor that adds a filter that all values in a stream must be
     * inside given collection
     *
     * @param <T>
     * @param <Z>
     * @param target
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> filterIn(Collection<? extends Z> target) {
        Objects.requireNonNull(target, "Collection is null");
        return filterPredicate(s -> target.contains(s));
    }

    /**
     * Applies functor that adds a filter that all values in a stream must not
     * be inside given collection
     *
     * @param <T>
     * @param <Z>
     * @param target
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> filterNotIn(Collection<? extends Z> target) {
        Objects.requireNonNull(target, "Collection is null");
        return filterPredicate(s -> !target.contains(s));
    }

    /**
     * Applies functor that adds a filter that all values in a stream must be
     * inside given collection with compliance to the given equator
     *
     * @param <T>
     * @param <Z>
     * @param target
     * @param eq
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> filterIn(Collection<? extends Z> target, Equator<Z> eq) {

        Objects.requireNonNull(target, "Collection is null");
        Objects.requireNonNull(eq, "Equator is null");

        return doWithEqualityProxyIterable(target, eq, (set, st) -> {
            Set<Equator.EqualityProxy<Z>> collect = set.collect(Collectors.toSet());
            return st.filter(item -> collect.contains(item));
        });

    }

    /**
     * Applies functor that adds a filter that all values in a stream must NOT
     * be inside given collection with compliance to the given equator
     *
     * @param <T>
     * @param <Z>
     * @param target
     * @param eq
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> filterNotIn(Collection<? extends Z> target, Equator<Z> eq) {

        Objects.requireNonNull(target, "Collection is null");
        Objects.requireNonNull(eq, "Equator is null");

        return doWithEqualityProxyIterable(target, eq, (set, st) -> {
            Set<Equator.EqualityProxy<Z>> collect = set.collect(Collectors.toSet());
            return st.filter(item -> !collect.contains(item));
        });

    }

    /**
     * Applies functor that creates an equality proxy and do something with
     * proxied stream and iterable
     *
     * @param <T>
     * @param <Z>
     * @param target
     * @param eq
     * @param decorator
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> doWithEqualityProxyIterable(
            Iterable<? extends Z> target,
            Equator<Z> eq,
            BiFunction<Stream<Equator.EqualityProxy<Z>>, StreamMapper<T, Equator.EqualityProxy<Z>>, StreamMapper<T, Equator.EqualityProxy<Z>>> decorator
    ) {

        Objects.requireNonNull(target, "Collection is null");
        Objects.requireNonNull(eq, "Equator is null");
        Objects.requireNonNull(decorator, "decorator is null");
        Function<Z, Equator.EqualityProxy<Z>> toProxy = s -> new Equator.EqualityProxy<>(s, eq);
        Function<Equator.EqualityProxy<Z>, Z> fromProxy = s -> s.getValue();

        Function<StreamMapper<T, Equator.EqualityProxy<Z>>, StreamMapper<T, Equator.EqualityProxy<Z>>> dec = st -> {
            Stream<Equator.EqualityProxy<Z>> proxyStream = StreamMapper.fromIterable(target)
                    .map(s -> new Equator.EqualityProxy<>(s, eq));
            return decorator.apply(proxyStream, st);
        };

        return doWithProxy(toProxy, fromProxy, dec);
    }

    /**
     * Applies a functor that creates a proxy type and do something with it and
     * then return to the original type
     *
     * @param <T>
     * @param <Z>
     * @param <P>
     * @param toProxy
     * @param fromProxy
     * @param decorator
     * @return
     */
    public static <T, Z, P> Function<StreamMapper<T, Z>, StreamMapper<T, Z>> doWithProxy(
            Function<Z, P> toProxy,
            Function<P, Z> fromProxy,
            Function<StreamMapper<T, P>, StreamMapper<T, P>> decorator
    ) {

        Objects.requireNonNull(toProxy, "toProxy is null");
        Objects.requireNonNull(fromProxy, "froProxy is null");
        Objects.requireNonNull(decorator, "decorator is null");

        return st -> st.map(toProxy).apply(decorator).map(fromProxy);

    }

    /**
     * Applies functor that adds a filter that all values in a stream must be
     * present
     *
     * @param <T>
     * @param <Z>
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, Optional<Z>>, StreamMapper<T, Z>> filterPresentOptional() {
        return st -> st.filter(Optional::isPresent).map(Optional::get);
    }

    /**
     * Applies functor that adds a filter that all values in a stream must be
     * present
     *
     * @param <T>
     * @param <Z>
     * @return
     */
    public static <T, Z> Function<StreamMapper<T, SafeOpt<Z>>, StreamMapper<T, Z>> filterPresentSafeOpt() {
        return st -> st.filter(SafeOpt::isPresent).map(SafeOpt::get);
    }

}
