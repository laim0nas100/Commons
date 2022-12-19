package lt.lb.commons.reflect.unified;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 * @param <S> source class type
 * @param <T> field type
 */
public interface IField<S, T> extends IMember, IAnnotatedElement, IFieldModifierAware, IAccessible {

    public Field field();

    @Override
    public default AnnotatedElement annotatedElement() {
        return field();
    }

    @Override
    public default void setAccessible(boolean flag) {
        field().setAccessible(flag);
    }

    @Override
    default String getName() {
        return field().getName();
    }

    @Override
    public default Class<?> getDeclaringClass() {
        return field().getDeclaringClass();
    }

    @Override
    public default int getModifiers() {
        return field().getModifiers();
    }

    @Override
    public default boolean isSynthetic() {
        return field().isSynthetic();
    }

    public default boolean isEnumConstant() {
        return field().isEnumConstant();
    }

    public default Class<T> getType() {
        return (Class) field().getType();
    }

    public default Type getGenericType() {
        return field().getGenericType();
    }

    public default AnnotatedType getAnnotatedType() {
        return field().getAnnotatedType();
    }

    //custom methods
    /**
     * instanceof operator
     *
     * @param cls
     * @return
     */
    public default boolean isTypeOf(Class cls) {
        Objects.requireNonNull(cls);
        return cls.isAssignableFrom(getType());
    }

    public default boolean isTypeExactly(Class cls) {
        Objects.requireNonNull(cls);
        return Objects.equals(cls, getType());
    }

}
