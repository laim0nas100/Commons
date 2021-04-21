/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.refletiontests;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.DLog;
import org.junit.Test;
import lt.lb.uncheckedutils.Checked;
/**
 *
 * @author laim0nas100
 */
public class ProxyTest {

    
    public static class DelegetingLoggingHandler <T> implements InvocationHandler{

        private T real;
        
        public DelegetingLoggingHandler(T real){
            this.real = real;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Checked.checkedRun(()->{
                DLog.print("Invoked method:", method.getName(), "At object:", real, "with params:", Arrays.asList(args));
            });
            
            return method.invoke(real, args);
            
        }
        
    }
    public static class DynamicInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Checked.checkedRun(()->{
                DLog.print("Invoked method:", method.getName(), "At object:", proxy, "with params:", Arrays.asList(args));
            });
            DLog.print("INSIDE");
            
            return null;
        }

    }

//    @Test
    public void test() {
        Map<String, String> proxyMap = F.cast(Proxy.newProxyInstance(DynamicInvocationHandler.class.getClassLoader(), ArrayOp.asArray(Map.class), new DelegetingLoggingHandler(new HashMap<String,String>())));
        proxyMap.put("key1", "Value1");

        DLog.print(proxyMap.get("key1"));
        
        Checked.uncheckedRun(() -> DLog.await(1, TimeUnit.HOURS));

    }

}
