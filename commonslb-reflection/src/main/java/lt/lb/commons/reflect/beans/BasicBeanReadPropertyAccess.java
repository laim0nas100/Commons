package lt.lb.commons.reflect.beans;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.F;

/**
 *
 * @author Lemmin
 */
public class BasicBeanReadPropertyAccess<V, T> implements Supplier<T> {

    protected V object;
    protected Method read;
    protected String readMethodName;

    @Override
    public T get() {
        return F.uncheckedCall(() -> (T) read.invoke(object));
    }

    public BasicBeanReadPropertyAccess(V object, String property) {
        this.object = object;
        Class clazz = object.getClass();
        String cap = NameUtil.capitalize(property);
        String simpleReadName = "get" + cap;

        Optional<Method> firstTry = Stream.of(clazz.getMethods())
                .filter(p -> p.getName().equals(simpleReadName))
                .filter(p -> p.getParameterCount() == 0)
                .findFirst();

        if (!firstTry.isPresent()) {
            readMethodName = "is" + NameUtil.capitalize(property);
            read = Stream.of(clazz.getMethods())
                    .filter(p -> p.getName().equals(readMethodName))
                    .filter(p -> p.getParameterCount() == 0)
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Failed to find read method of name:" + simpleReadName + " or " + readMethodName));
        } else {
            read = firstTry.get();
            readMethodName = simpleReadName;
        }

    }
}
