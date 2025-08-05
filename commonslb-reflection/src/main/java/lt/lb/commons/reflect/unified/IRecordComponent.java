package lt.lb.commons.reflect.unified;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Java 8 compilation compatable RecordComponent extension using reflection
 *
 * @author laim0nas100
 */
public interface IRecordComponent extends IAnnotatedElement, INamed {

    public Method getAccessor();

    public AnnotatedType getAnnotatedType();

    public Class<?> getType();

    public String getGenericSignature();

    public Type getGenericType();

    public Class getDeclaringRecord();

}
