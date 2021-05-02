package lt.lb.commons.iteration;

import java.util.Iterator;
import lt.lb.uncheckedutils.SafeOpt;

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
