package empiric.core;

import java.util.HashMap;
import java.util.Map;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.caching.lazy.LazyProxy;
import lt.lb.commons.containers.caching.lazy.LazyValue;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class LazyValueTest {
    
//    @Test
    public void test() {
        HashMap<String, String> str = new HashMap<>();
        str.put("1", "one");
        str.put("2", "two");
        DLog.print("hi");
        LazyProxy<Map<String, String>> map = new LazyValue<>(() -> {
            DLog.print("Init map");
            return str;
        });
        LazyProxy<String> map1 = map.map(s -> {
            DLog.print("Init map1");
            return s.get("1");
        });
        LazyProxy<String> map2 = map1.map(s -> {
            DLog.print("Init map2");
            return s + "__";
        });
        DLog.print(map2.get());
        
        str.replace("1", "ONE");
        DLog.print(map2.get());
        map.invalidate();
        DLog.print("After invalidate");
        DLog.print( map2.get());
        
        DLog.close();
    }
    
}
