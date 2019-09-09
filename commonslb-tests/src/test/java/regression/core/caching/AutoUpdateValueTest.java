package regression.core.caching;

import lt.lb.commons.containters.caching.AutoUpdateValue;
import lt.lb.commons.threads.FastExecutor;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class AutoUpdateValueTest {
    @Test
    public void test1() throws Exception{
        AutoUpdateValue<String> val = new AutoUpdateValue<>(null, () -> {
            Thread.sleep(200);
            return "Sleepy";
        }, new FastExecutor(1), false);
        
        
        assertThat(val.get()).isNull();
        assertThat(val.get(true)).isEqualTo("Sleepy");
        assertThat(val.get()).isEqualTo("Sleepy");
        
        
    }
    
    @Test
    public void test2() throws Exception{
        AutoUpdateValue<String> val = new AutoUpdateValue<>(null, () -> {
            Thread.sleep(200);
            return "Sleepy";
        }, new FastExecutor(1), true);
        
        
        assertThat(val.get()).isNotNull();
        assertThat(val.get()).isEqualTo("Sleepy");
        assertThat(val.get(true)).isEqualTo("Sleepy");
        assertThat(val.get()).isEqualTo("Sleepy");
        
        
    }
}
