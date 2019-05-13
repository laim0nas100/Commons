
package core;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.iteration.Iter.IterNoStop;
import lt.lb.commons.iteration.ReadOnlyIterator;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class IteratorTest {


    @Test
    public void test() throws Exception{
        Integer[] arr = ArrayOp.asArray(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15);
        List<Integer> asList = Arrays.asList(arr);
        Log.main().stackTrace = false;
        IterNoStop it = (i,item)->{
            
            if(i==0){
                Log.print("####");
            }
            Log.print(i,"Item="+item);
            
        };
        F.iterate(asList, it);
        
        F.iterate(ReadOnlyIterator.of(asList), it);
        
        F.iterate(asList.stream(), it);
        
        F.iterate(arr, it);
        
        F.iterate(ReadOnlyIterator.of(arr), it);
        
        
        
        
        Log.await(1, TimeUnit.HOURS);
    }
    
    
}
