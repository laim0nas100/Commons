/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core.caching;

import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.containers.caching.AutoUpdateValue;
import lt.lb.commons.threads.FastExecutor;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Laimonas Beniušis
 */
public class AutoUpdateValueTest {
    @Test
    public void test1() throws Exception{
        AutoUpdateValue<String> val = new AutoUpdateValue<>(null, () -> {
            Thread.sleep(1000);
            return "Sleepy";
        }, new FastExecutor(1), false);
        
        
        assertThat(val.get()).isNull();
        assertThat(val.get(true)).isEqualTo("Sleepy");
        assertThat(val.get()).isEqualTo("Sleepy");
        
        
    }
    
    @Test
    public void test2() throws Exception{
        AutoUpdateValue<String> val = new AutoUpdateValue<>(null, () -> {
            Thread.sleep(1000);
            return "Sleepy";
        }, new FastExecutor(1), true);
        
        
        assertThat(val.get()).isNotNull();
        assertThat(val.get()).isEqualTo("Sleepy");
        assertThat(val.get(true)).isEqualTo("Sleepy");
        assertThat(val.get()).isEqualTo("Sleepy");
        
        
    }
}
