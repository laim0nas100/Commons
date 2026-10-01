package com.github.laim0nas100.commonslb.datasync.extractors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.laim0nas100.commonslb.containers.values.Value;
import com.github.laim0nas100.commonslb.containers.values.ValueProxy;
import com.github.laim0nas100.commonslb.reflect.unified.ReflMethods;

/**
 *
 * @author laim0nas100
 */
public interface ProxyForm {

    public static class MapProxyForm implements ProxyForm, InvocationHandler {

        private Map<Method, ValueProxy> map = new HashMap<>();

        public MapProxyForm(List<Method> methods) {
            for (Method m : methods) {
                map.put(m, new Value());
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            ValueProxy val = map.getOrDefault(method, null);
            if (val != null) {
                return val;
            } else {
                return method.invoke(proxy, args);
            }

        }

    }

    public static <T extends ProxyForm> T of(Class<T> type) {
        List<Method> toList = ReflMethods.getLocalMethods(type, ValueProxy.class).map(m -> m.method()).toList();
        
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new MapProxyForm(toList));

    }
    
    public static interface TestProxy extends ProxyForm{
        public ValueProxy<String> firstName();
        public ValueProxy<String> lastName();
        public ValueProxy<Integer> age();
    }
    
    
    public static void main(String[] args){
        TestProxy form = ProxyForm.of(TestProxy.class);
        
        
        form.age().set(10);
        form.firstName().set("John");
        form.lastName().set("Roberts");
        
        
        System.out.println(form.age()+" "+form.firstName()+" "+form.lastName());
        
    }

}
