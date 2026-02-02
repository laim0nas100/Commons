package lt.lb.commons.refmodel.maps;

import java.util.List;
import lt.lb.commons.F;
import lt.lb.commons.refmodel.Ref;
import lt.lb.commons.refmodel.RefCompiler;
import lt.lb.commons.refmodel.RefList;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class ListRef<T extends Ref> extends ObjectRef<T> implements RefList {

    /**
     * {@inheritDoc }
     */
    @Override
    public T at(int index) {

        try {
            Ref continuation = getMemberContinuation().clone();
            //only care about cloning the string fields, they will change in compile continuation, the rest can be the same

            RefCompiler.compileContinuation(continuation, index);
            return (T) continuation;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Reads a List value at resolved path
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public <T> SafeOpt<List> readList(MapProvider provider) {
        return readCast(provider);
    }

    /**
     * Reads a List value at measures it's size
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public SafeOpt<Integer> size(MapProvider provider) {
        return readList(provider).map(m -> m.size());
    }

    /**
     * Reads a List value at measures clears it
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public SafeOpt<Integer> clear(MapProvider provider) {
        return readList(provider).map(m -> {
            int size = m.size();
            m.clear();
            return size;
        });
    }

}
