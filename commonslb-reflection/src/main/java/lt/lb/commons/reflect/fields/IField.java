package lt.lb.commons.reflect.fields;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 * @param <S> source class type
 * @param <T> field type
 */
public interface IField<S, T> extends Member, AnnotatedElement, IFieldModifierAware {

    public Field rawField();

    public static <S, T> IField<S, T> ofField(final Field field) {
        Objects.requireNonNull(field);
        return () -> field;
    }

    @Override
    default String getName() {
        return rawField().getName();
    }

    @Override
    public default Class<S> getDeclaringClass() {
        return (Class<S>) rawField().getDeclaringClass();
    }

    @Override
    public default int getModifiers() {
        return rawField().getModifiers();
    }

    @Override
    public default boolean isSynthetic() {
        return rawField().isSynthetic();
    }

    @Override
    public default <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return rawField().getAnnotation(annotationClass);
    }

    @Override
    public default Annotation[] getAnnotations() {
        return rawField().getAnnotations();
    }

    @Override
    public default Annotation[] getDeclaredAnnotations() {
        return rawField().getDeclaredAnnotations();
    }

    public default IStaticField<S, T> asStaticField() {
        return this::rawField;
    }

    public default IObjectField<S, T> asObjectField() {
        return this::rawField;
    }

}
