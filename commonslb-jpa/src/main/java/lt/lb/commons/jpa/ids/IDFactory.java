package lt.lb.commons.jpa.ids;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.uncheckedutils.func.UncheckedFunction;

/**
 *
 * @author laim0nas100
 */
public interface IDFactory<I> {

    /**
     * Polymorphic cast. Both have to share a common supertype.
     *
     * @param <T>
     * @param <P>
     * @param <O>
     * @param id
     * @return
     */
    public default <T, P extends T, O extends T> ID<P, I> polyCast(ID<O, I> id) {
        return ofId(id.id);
    }

    /**
     * Simple cast.
     *
     * @param <T>
     * @param <P>
     * @param id
     * @return
     */
    public default <T, P> ID<P, I> cast(ID<T, I> id) {
        return ofId(id.id);
    }

    /**
     * Explicitly define class and ID. Base method, override this for different
     * base class objects.
     *
     * @param cls
     * @param id
     * @return
     */
    public default <T> ID<T, I> ofId(Class<T> cls, I id) {
        return new ID<>(id);
    }

    /**
     * Explicitly define class and get ID by calling default id getter method
     * (should be public method)
     *
     * @param cls
     * @param object
     * @return
     */
    public default <T> ID<T, I> of(Class<T> cls, T object) {
        return ofId(null, defaultGetId(object));
    }

    /**
     * Implicitly resolve class and ID
     *
     * @param object
     * @return
     */
    public default <T> ID<T, I> of(T object) {
        return ofId(null, defaultGetId(object));
    }

    /**
     * Don't resolve class and ID. Have to define in a variable.
     *
     * @param id
     * @return
     */
    public default <T> ID<T, I> ofId(I id) {
        return ofId(null, id);
    }

    /**
     * Finds first public method called 'getid' (case-insensitive)
     *
     * @param cls
     * @return
     */
    public default <T> Function<T, I> defaultIdGetter(Class cls) {
        Method method = Stream.of(cls.getMethods()).filter(me -> me.getName().equalsIgnoreCase("getid")).findFirst().get();
        return (UncheckedFunction<T, I>) (T item) -> (I) method.invoke(item);
    }

    /**
     * Gets and casts ID from {@link IDFactory#defaultIdGetter(java.lang.Class) } method.
     *
     * @param ob
     * @return
     */
    public default <T> I defaultGetId(T ob) {
        return defaultIdGetter(ob.getClass()).apply(ob);
    }
}
