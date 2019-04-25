package core;

import java.util.HashMap;
import java.util.Map;
import lt.lb.commons.Log;
import lt.lb.commons.containers.LazyDependantValue;
import org.junit.Test;

/**
 *
 * @author Laimonas Beniušis
 */
public class LazyValueTest {
    
    @Test
    public void test() {
        HashMap<String, String> str = new HashMap<>();
        str.put("1", "one");
        str.put("2", "two");
        Log.print("hi");
        LazyDependantValue<Map<String, String>> map = new LazyDependantValue<>(() -> {
            Log.print("Init map");
            return str;
        });
        LazyDependantValue<String> map1 = map.map(s -> {
            Log.print("Init map1");
            return s.get("1");
        });
        LazyDependantValue<String> map2 = map1.map(s -> {
            Log.print("Init map2");
            return s + "__";
        });
        Log.print(map2.get());
        
        str.replace("1", "ONE");
        Log.print(map2.get());
        map.invalidate();
        Log.print("After invalidate");
        Log.print( map2.get());
        
        Log.close();
    }
    
}
