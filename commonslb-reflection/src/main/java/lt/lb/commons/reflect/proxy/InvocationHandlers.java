package lt.lb.commons.reflect.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author laim0nas100
 */
public class InvocationHandlers {
    
    public static InvocationHandler ofRunnable(Runnable run) {
        return ofRunnable(t -> true, run);
    }
    
    public static InvocationHandler ofRunnable(Predicate<Method> pred, Runnable run) {
        return (Object proxy, Method method, Object[] args) -> {
            if (pred.test(method)) {
                run.run();
            }
            return null;
        };
    }
    
    public static InvocationHandler ofMethodListener(Consumer<Method> cons) {
        return (Object proxy, Method method, Object[] args) -> {
            cons.accept(method);
            return null;
        };
    }
    
    public static InvocationHandler ofArgsListener(Predicate<Method> pred, Consumer<Object[]> cons) {
        return (Object proxy, Method method, Object[] args) -> {
            if (pred.test(method)) {
                cons.accept(args);
            }
            return null;
        };
    }
    
    public static InvocationHandler ofArgsListener(Consumer<Object[]> cons) {
        return ofArgsListener(t -> true, cons);
    }
    
    public static <T> InvocationHandler ofDelegate(Function<Object[], T> cons) {
        return ofDelegate(t -> true, cons);
    }
    
    public static <T> InvocationHandler ofDelegate(Predicate<Method> pred, Function<Object[], T> cons) {
        return (Object proxy, Method method, Object[] args) -> {
            if (pred.test(method)) {
                return cons.apply(args);
            }
            return null;
            
        };
    }
}
