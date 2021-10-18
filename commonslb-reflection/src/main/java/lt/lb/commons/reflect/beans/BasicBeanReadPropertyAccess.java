package lt.lb.commons.reflect.beans;

import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.reflect.Refl;

/**
 *
 * @author laim0nas100
 */
public class BasicBeanReadPropertyAccess<V, T> implements Supplier<T> {

    protected V object;
    protected Method read;
    protected String readMethodName;

    @Override
    public T get() {
        return Refl.invokeMethod(read, object);
    }

    public BasicBeanReadPropertyAccess(V object, String property) {
        this.object = object;
        Class clazz = object.getClass();
        String cap = NameUtil.capitalize(property);
        String simpleReadName = "get" + cap;
        Method[] methods = Stream.of(clazz.getMethods())
                .filter(p -> p.getParameterCount() == 0)
                .toArray(s -> new Method[s]);

        boolean found = false;
        for (Method me : methods) {
            if (simpleReadName.equals(me.getName())) {
                found = true;
                readMethodName = simpleReadName;
                read = me;
                break;
            }
        }
        if (!found) {
            simpleReadName = "is" + cap;
            for (Method me : methods) {
                if (simpleReadName.equals(me.getName())) {
                    found = true;
                    readMethodName = simpleReadName;
                    read = me;
                    break;
                }
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Failed to find read method of name:" + simpleReadName + " or " + readMethodName);
        }
    }
}
