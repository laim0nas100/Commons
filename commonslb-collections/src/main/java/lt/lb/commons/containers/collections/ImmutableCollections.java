package lt.lb.commons.containers.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author laim0nas100
 */
public class ImmutableCollections {
    
    public static <T> Set<T> setOf(T...items){
        if(items.length == 0){
            return Collections.emptySet();
        }
        if(items.length == 1){
            Set<T> set = new HashSet<>(1,1);
            set.add(items[0]);
            return Collections.unmodifiableSet(set);
        }
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(items)));
    }
    
    
    public static <T> List<T> listOf(T...items){
        if(items.length == 0){
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(items));
    }
    
}
