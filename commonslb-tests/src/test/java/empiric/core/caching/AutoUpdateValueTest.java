/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core.caching;

import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.caching.AutoUpdateValue;
import lt.lb.commons.threads.executors.FastExecutor;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class AutoUpdateValueTest {

//    @Test
    public void test1() throws Exception{
        AutoUpdateValue<String> val = new AutoUpdateValue<>(null, () -> {
            Thread.sleep(1000);
            return "Sleepy";
        }, new FastExecutor(1), false);
        
        
        DLog.print(val.get());
        DLog.print(val.get(true));
        String get = val.get();
        DLog.print(get);
        DLog.await(1, TimeUnit.DAYS);
        
        
    }
}
