package regression.core.caching;

import com.github.laim0nas100.commonslb.caching.AutoUpdateValue;
import com.github.laim0nas100.commonslb.threads.executors.FastExecutor;
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
