package lt.lb.commons.func;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lt.lb.commons.Ins;
import lt.lb.commons.interfaces.Equator;

/**
 * Collection of functors used for StreamMapper objects
 *
 * @author laim0nas100
 */
public abstract class StreamMappers {
    
    /**
     * Converts iterable to Stream. If null, return empty stream;
     *
     * @param <T>
     * @param iterable
     * @return
     */
    public static <T> Stream<T> fromIterable(Iterable<T> iterable) {
        return Optional.ofNullable(iterable)
                .map(s -> s.spliterator())
                .map(s -> StreamSupport.stream(s, false)).orElse(Stream.empty());
    }

    /**
     * Converts iterator to Stream. If null, return empty stream;
     *
     * @param <T>
     * @param iterator
     * @return
     */
    public static <T> Stream<T> fromIterator(Iterator<T> iterator) {
        return Optional.ofNullable(iterator)
                .map(s -> Spliterators.spliteratorUnknownSize(s, 0))
                .map(s -> StreamSupport.stream(s, false)).orElse(Stream.empty());
    }

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
        return s -> s.filter(predicate);
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
    public static <T, Z, R> Function<StreamMapper<T, Z>, StreamMapper<T, R>> select(Class<Z> cls) {
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
        Objects.requireNonNull(nullCase, "nullCase is null"); //probably not what you want instead of a null is another null
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
}
