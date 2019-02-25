package lt.lb.commons.reflect.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lt.lb.commons.ArrayOp;

/**
 *
 * @author Laimonas Beniušis
 * add method listeners
 */
public class ProxyListenerBuilder implements Cloneable{

    private InvocationHandler getObjectInvocationHandler(Object obj) {
        InvocationHandler nullInvocationHandler = getNullInvocationHandler();
        return (Object proxy, Method method, Object[] args) -> {
            nullInvocationHandler.invoke(proxy, method, args);
            return method.invoke(obj, args);
        };
    }

    private InvocationHandler getNullInvocationHandler() {
        ProxyListenerBuilder me = this;
        return (Object proxy, Method method, Object[] args) -> {
            invokeList(me.globalHandlers, proxy, method, args);
            invokeList(me.invHandlers.getOrDefault(method, null), proxy, method, args);
            invokeList(me.stringHandlers.getOrDefault(method.getName(), null), proxy, method, args);
            return null;
        };
    }

    private static void invokeList(List<InvocationHandler> list, Object proxy, Method method, Object[] args) throws Throwable {
        if (list == null) {
            return;
        }
        for (InvocationHandler hand : list) {
            hand.invoke(proxy, method, args);
        }
    }

    private final ClassLoader cLoader;
    private final LinkedList<InvocationHandler> globalHandlers = new LinkedList<>();
    private final Map<Method, List<InvocationHandler>> invHandlers = new HashMap<>();
    private final Map<String, List<InvocationHandler>> stringHandlers = new HashMap<>();

    public ProxyListenerBuilder(ClassLoader cLoader) {
        this.cLoader = cLoader;
    }

    public ProxyListenerBuilder() {
        this(ClassLoader.getSystemClassLoader());
    }

    @Override
    public ProxyListenerBuilder clone() {
        ProxyListenerBuilder copy = new ProxyListenerBuilder(cLoader);
        copy.globalHandlers.addAll(globalHandlers);
        copy.invHandlers.putAll(invHandlers);
        copy.stringHandlers.putAll(stringHandlers);
        return copy;

    }

    public ProxyListenerBuilder addGlobalHandler(InvocationHandler... hand) {
        ProxyListenerBuilder clone = this.clone();
        for (InvocationHandler handler : hand) {
            clone.globalHandlers.add(handler);
        }
        return clone;
    }

    public ProxyListenerBuilder addInvocationHandler(Method me, InvocationHandler... hand) {
        if (hand.length == 0) {
            return this;
        }
        ProxyListenerBuilder clone = this.clone();
        for (InvocationHandler handler : hand) {
            clone.invHandlers.computeIfAbsent(me, k -> new LinkedList<>()).add(handler);
        }
        return clone;
    }

    public ProxyListenerBuilder addNameInvocationHandler(Method me, InvocationHandler... hand) {
        if (hand.length == 0) {
            return this;
        }
        ProxyListenerBuilder clone = this.clone();
        for (InvocationHandler handler : hand) {
            clone.stringHandlers.computeIfAbsent(me.getName(), k -> new LinkedList<>()).add(handler);
        }
        return clone;
    }

    public <T> T ofObject(T obj) {
        return (T) Proxy.newProxyInstance(cLoader, ArrayOp.asArray(obj.getClass()), this.getObjectInvocationHandler(obj));
    }

    public <T> T ofSupply(Supplier<T> supp) {
        return ofObject(supp.get());
    }

    public <T> T ofInterfaces(T obj, Class... interfaces) {
        return (T) Proxy.newProxyInstance(cLoader, ArrayOp.asArray(interfaces), this.getObjectInvocationHandler(obj));
    }

}
