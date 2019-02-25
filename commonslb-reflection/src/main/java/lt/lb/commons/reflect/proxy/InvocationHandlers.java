package lt.lb.commons.reflect.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author Laimonas Beniušis
 */
public class InvocationHandlers {

    public static InvocationHandler ofRunnable(Runnable run) {
        return (Object proxy, Method method, Object[] args) -> {
            run.run();
            return null;
        };
    }

    public static InvocationHandler ofMethodListener(Consumer<Method> cons) {
        return (Object proxy, Method method, Object[] args) -> {
            cons.accept(method);
            return null;
        };
    }

    public static InvocationHandler ofArgsListener(Consumer<Object[]> cons) {
        return (Object proxy, Method method, Object[] args) -> {
            cons.accept(args);
            return null;
        };
    }

    public static <T> InvocationHandler ofDelegate(Function<Object[], T> cons) {
        return (Object proxy, Method method, Object[] args) -> {
            return cons.apply(args);
        };
    }
}
