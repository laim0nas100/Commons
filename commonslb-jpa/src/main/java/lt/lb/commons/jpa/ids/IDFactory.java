package lt.lb.commons.jpa.ids;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import lt.lb.commons.F;

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
     * Finds first method called 'getid' (case-insensisitive)
     *
     * @param cls
     * @return
     */
    public default Method defaultNameIdGetter(Class cls) {
        return Stream.of(cls.getMethods()).filter(me -> me.getName().equalsIgnoreCase("getid")).findFirst().get();
    }

    /**
     * Gets and casts ID from defaultNameIdGetter method.
     *
     * @param ob
     * @return
     */
    public default I defaultGetId(Object ob) {
        return F.uncheckedCall(() -> (I) defaultNameIdGetter(ob.getClass()).invoke(ob));
    }
}
