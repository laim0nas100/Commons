package lt.lb.commons.jpa.ids;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lt.lb.commons.iteration.streams.SimpleStream;
import lt.lb.commons.reflect.Refl;
import lt.lb.commons.reflect.fields.IField;
import lt.lb.commons.reflect.fields.IObjectField;
import lt.lb.commons.reflect.fields.ReflFields;
import lt.lb.uncheckedutils.func.UncheckedFunction;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 * @param <I>
 */
public interface IDFactory<I> {

    public static interface IdGetter<O, R> extends UncheckedFunction<O, R> {

        public boolean generated();
    }
    
    
    public default <T> Class<T> classResolve(T object) {
        if (object == null) {
            return null;
        }
        return (Class) object.getClass();
    }

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
        return ofId(null, getId(object));
    }

    /**
     * Implicitly resolve class and ID
     *
     * @param object
     * @return
     */
    public default <T> ID<T, I> of(T object) {
        return ofId(null, getId(object));
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
     * Finds first method or field with {@link Id} annotation, and resolves via
     * getter method.
     *
     * @param cls
     * @return
     */
    public default <T> IdGetter<T, I> idGetter(Class cls) {
        SimpleStream<IField> fields = ReflFields.getFieldsOf(cls);
        Optional<IObjectField> objectField = fields.filter(f -> f.isAnnotationPresent(Id.class)).map(m -> m.asObjectField()).findFirst();
        boolean generatedByField = false;
        boolean generatedByMethod = false;
        final Method method;
        if (objectField.isPresent()) {
            generatedByField = objectField.map(f -> f.isAnnotationPresent(GeneratedValue.class)).orElse(false);
            String name = objectField.get().getName();
            String methodName1 = "get" + name;
            String methodName2 = "is" + name;
            LinkedList<Method> methodsOf = Refl.getMethodsOf(cls, meth -> StringUtils.containsIgnoreCase(meth.getName(), methodName1) || StringUtils.containsIgnoreCase(meth.getName(), methodName2));
            if (methodsOf.isEmpty()) {
                throw new IllegalArgumentException("Failed to resolve a way to get Id form " + cls);
            }
            method = methodsOf.get(0);
        } else {
            Optional<Method> findFirst = Stream.of(cls.getMethods()).filter(me -> me.isAnnotationPresent(Id.class)).findFirst();
            if (!findFirst.isPresent()) {
                throw new IllegalArgumentException("Failed to resolve a way to get Id form " + cls);
            }
            method = findFirst.get();
            generatedByField = method.isAnnotationPresent(GeneratedValue.class);
        }

        final boolean generated = generatedByField || generatedByMethod;

        return new IdGetter<T, I>() {
            @Override
            public boolean generated() {
                return generated;
            }

            @Override
            public I applyUnchecked(T t) throws Throwable {
                return (I) method.invoke(t);
            }
        };
    }

    /**
     * Gets and casts ID from {@link IDFactory#idGetter(java.lang.Class)
     * } method.
     *
     * @param ob
     * @return
     */
    public default <T> I getId(T ob) {
        return idGetter(classResolve(ob)).apply(ob);
    }
}
