/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core.caching;

import java.util.concurrent.TimeUnit;
import com.github.laim0nas100.commonslb.DLog;
import com.github.laim0nas100.commonslb.caching.AutoUpdateValue;
import com.github.laim0nas100.commonslb.threads.executors.FastExecutor;
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
        DLog.await(1, TimeUnit.MINUTES);
        
        
    }
}
