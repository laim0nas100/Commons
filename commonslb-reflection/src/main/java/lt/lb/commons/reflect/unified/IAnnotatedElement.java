package lt.lb.commons.reflect.unified;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 *
 * @author laim0nas100
 */
public interface IAnnotatedElement extends AnnotatedElement {

    public AnnotatedElement annotatedElement();

    @Override
    public default <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return annotatedElement().getAnnotation(annotationClass);
    }

    @Override
    public default Annotation[] getAnnotations() {
        return annotatedElement().getAnnotations();
    }

    @Override
    public default Annotation[] getDeclaredAnnotations() {
        return annotatedElement().getDeclaredAnnotations();
    }

    public default boolean hasAnnotations() {
        return getAnnotations().length > 0;
    }
}
