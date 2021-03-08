package empiric.core;

import java.util.HashMap;
import java.util.Map;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.caching.LazyDependantValue;
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
        LazyDependantValue<Map<String, String>> map = new LazyDependantValue<>(() -> {
            DLog.print("Init map");
            return str;
        });
        LazyDependantValue<String> map1 = map.map(s -> {
            DLog.print("Init map1");
            return s.get("1");
        });
        LazyDependantValue<String> map2 = map1.map(s -> {
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
