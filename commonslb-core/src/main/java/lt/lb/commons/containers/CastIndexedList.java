package lt.lb.commons.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lt.lb.commons.ArrayOp;

/**
 * CastList, but also contains int[] information about where those values were
 * taken from.
 *
 * @author laim0nas
 */
public class CastIndexedList<T> extends CastList<T> {

    public final int[] indexes;

    public CastIndexedList(int[] indexes, List<T> list) {
        super(list);
        this.indexes = indexes;
        
    }

    @Override
    public String toString() {
        return Arrays.toString(indexes) +" "+ super.toString();
    }
    
    public List<Integer> asIndexList(){
        return new ArrayList<>(Arrays.asList(ArrayOp.mapInt(indexes)));
    }
    
    

}
