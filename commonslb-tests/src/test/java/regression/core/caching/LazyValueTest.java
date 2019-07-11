package regression.core.caching;

import java.util.HashMap;
import java.util.Map;
import lt.lb.commons.containers.caching.LazyDependantValue;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class LazyValueTest {
    @Test
    public void test() {
        HashMap<String, String> str = new HashMap<>();
        str.put("1", "one");
        str.put("2", "two");
        LazyDependantValue<Map<String, String>> map = new LazyDependantValue<>(str);
        LazyDependantValue<String> map1 = map.map(s -> {
            return s.get("1");
        });
        LazyDependantValue<String> map2 = map1.map(s -> {
            return s + "__";
        });
        assertThat(map2.get()).isEqualTo("one__");
        
        str.replace("1", "ONE");
        assertThat(map2.get()).isEqualTo("one__"); // no update yet
        map.invalidate();
        //after invalidate
        assertThat(map2.get()).isEqualTo("ONE__");
        
    }
}
