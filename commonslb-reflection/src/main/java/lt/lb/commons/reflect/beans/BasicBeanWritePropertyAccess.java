package lt.lb.commons.reflect.beans;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lt.lb.commons.reflect.Refl;

/**
 *
 * @author laim0nas100
 */
public class BasicBeanWritePropertyAccess<V, T> implements Consumer<T> {

    protected V object;
    protected Method write;
    protected String writeMethodName;

    @Override
    public void accept(T v) {
        Refl.invokeMethod(write, object, v);
    }

    public void set(T v) {
        accept(v);
    }

    public BasicBeanWritePropertyAccess(V object, String property) {
        this.object = object;
        Class clazz = object.getClass();
        writeMethodName = "set" + NameUtil.capitalize(property);
        Method[] methods = Stream.of(clazz.getMethods())
                .filter(p -> p.getParameterCount() == 1)
                .toArray(s -> new Method[s]);

        boolean found = false;
        for (Method me : methods) {
            if (writeMethodName.equals(me.getName())) {
                found = true;
                write = me;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Failed to find write method of name:" + writeMethodName);
        }
    }
}
