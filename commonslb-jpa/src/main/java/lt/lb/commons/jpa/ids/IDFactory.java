package lt.lb.commons.jpa.ids;

import java.util.Optional;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lt.lb.commons.iteration.streams.SimpleStream;
import lt.lb.commons.reflect.beans.NameUtil;
import lt.lb.commons.reflect.unified.IObjectField;
import lt.lb.commons.reflect.unified.IObjectMethod;
import lt.lb.commons.reflect.unified.ReflFields;
import lt.lb.commons.reflect.unified.ReflMethods;
import lt.lb.uncheckedutils.func.UncheckedFunction;

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
    public default <T> IdGetter<T, I> idGetter(Class<T> cls) {
        SimpleStream<IObjectField<T, I>> fields = ReflFields.getLocalFields(cls);
        Optional<IObjectField<T,I>> objectFieldOpt = fields.filter(f -> f.isAnnotationPresent(Id.class)).findFirst();
        boolean generatedByField = false;
        boolean generatedByMethod = false;
        final IObjectMethod<T, I> method;
        if (objectFieldOpt.isPresent()) {
            IObjectField<T,I> objectField = objectFieldOpt.get();
            generatedByField = objectField.isAnnotationPresent(GeneratedValue.class);

            String methodName = "get" + NameUtil.capitalize(objectField.getName()); // should not be of type boolean
            Optional<IObjectMethod<T, I>> uniqueMethod = ReflMethods.getGetterMethodsOfType(cls, objectField.getType())
                    .filter(
                            meth -> meth.nameIs(methodName)
                    ).toUniqueOrEmpty();
            if (uniqueMethod.isPresent()) {
                method = uniqueMethod.get();
            } else {
                throw new IllegalArgumentException("Failed to resolve a way to get Id form " + cls);
            }

        } else {
            SimpleStream<IObjectMethod<T, I>> getterMethods = ReflMethods.getGetterMethodsTyped(cls);
            Optional<IObjectMethod<T, I>> findFirst = getterMethods.filter(me -> me.isAnnotationPresent(Id.class)).findFirst();
            if (!findFirst.isPresent()) {
                throw new IllegalArgumentException("Failed to resolve a way to get Id form " + cls);
            }
            method = findFirst.get();
            generatedByMethod = method.isAnnotationPresent(GeneratedValue.class);
        }

        final boolean generated = generatedByField || generatedByMethod;

        return new IdGetter<T, I>() {
            @Override
            public boolean generated() {
                return generated;
            }

            @Override
            public I applyUnchecked(T t) throws Throwable {
                return method.invoke(t);
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
