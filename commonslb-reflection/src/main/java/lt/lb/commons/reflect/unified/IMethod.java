package lt.lb.commons.reflect.unified;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public interface IMethod<S, T> extends IMember, IAnnotatedElement, IMethodModifierAware {

    public Method method();

    @Override
    public default void setAccessible(boolean flag) {
        method().setAccessible(flag);
    }

    @Override
    public default Class<S> getDeclaringClass() {
        return (Class<S>) method().getDeclaringClass();
    }

    @Override
    public default String getName() {
        return method().getName();
    }

    @Override
    public default int getModifiers() {
        return method().getModifiers();
    }

    @Override
    public default boolean isSynthetic() {
        return method().isSynthetic();
    }

    public default boolean isBridge() {
        return method().isBridge();
    }

    @Override
    public default boolean isDefault() {
        return method().isDefault();
    }

    public default Object getDefaultValue() {
        return method().getDefaultValue();
    }

    public default Annotation[][] getParameterAnnotations() {
        return method().getParameterAnnotations();
    }

    public default AnnotatedType getAnnotatedReturnType() {
        return method().getAnnotatedReturnType();
    }

    public default AnnotatedType getAnnotatedReceiverType() {
        return method().getAnnotatedReceiverType();
    }

    public default AnnotatedType[] getAnnotatedParameterTypes() {
        return method().getAnnotatedParameterTypes();
    }

    public default AnnotatedType[] getAnnotatedExceptionTypes() {
        return method().getAnnotatedExceptionTypes();
    }
    
    @Override
    public default boolean isVarArgs(){
        return method().isVarArgs();
    }

    @Override
    public default <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return method().getAnnotation(annotationClass);
    }

    @Override
    public default Annotation[] getAnnotations() {
        return method().getAnnotations();
    }

    @Override
    public default Annotation[] getDeclaredAnnotations() {
        return method().getDeclaredAnnotations();
    }

    public default TypeVariable<Method>[] getTypeParameters() {
        return method().getTypeParameters();
    }

    public default Class<T> getReturnType() {
        return (Class<T>) method().getReturnType();
    }

    public default Type getGenericReturnType() {
        return method().getGenericReturnType();
    }

    public default int getParameterCount() {
        return method().getParameterCount();
    }

    public default Type[] getGenericParameterTypes() {
        return method().getGenericParameterTypes();
    }

    public default Class<?>[] getExceptionTypes() {
        return method().getExceptionTypes();
    }

    public default Type[] getGenericExceptionTypes() {
        return method().getGenericExceptionTypes();
    }
    
    //custom methods
    
    
    /**
     * instanceof operator
     * @param cls
     * @return 
     */
    public default boolean isReturnTypeOf(Class cls){
        Objects.requireNonNull(cls);
        return cls.isAssignableFrom(getReturnType());
    }
    
    public default boolean isReturnTypeExactly(Class cls){
        Objects.requireNonNull(cls);
        return Objects.equals(cls, getReturnType());
    }
    
    public default boolean isVoid(){
        return isReturnTypeOf(Void.TYPE);
    }

}
