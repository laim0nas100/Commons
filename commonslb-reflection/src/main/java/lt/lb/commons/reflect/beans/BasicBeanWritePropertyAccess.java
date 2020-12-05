package lt.lb.commons.reflect.beans;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lt.lb.commons.F;

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
        F.unsafeRun(() -> {
            write.invoke(object, v);
        });
    }

    public void set(T v) {
        accept(v);
    }

    public BasicBeanWritePropertyAccess(V object, String property) {
        this.object = object;
        Class clazz = object.getClass();
        //try simple then boolean
        writeMethodName = "set" + NameUtil.capitalize(property);

        write = Stream.of(clazz.getMethods())
                .filter(p -> p.getName().equals(writeMethodName))
                .filter(p -> p.getParameterCount() == 1)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Failed to find write method of name:" + writeMethodName));
    }
}
