package lt.lb.commons.iteration;

import java.util.Iterator;
import com.github.laim0nas100.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface UncheckedIterator<T> extends Iterator<T>{

    @Override
    public default T next(){
        return safeNext().throwIfErrorAsNested().get();
    }
    
    public SafeOpt<T> safeNext();

    @Override
    public boolean hasNext();
    
}
