package lt.lb.commons.refmodel.maps;

import java.util.List;
import lt.lb.commons.refmodel.Ref;
import lt.lb.commons.refmodel.RefCompiler;
import lt.lb.commons.refmodel.RefList;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class ListRef<T extends Ref> extends ObjectRef<T> implements RefList {

    @Override
    public T at(int index) {

        try {
            Ref memberContinuation = getMemberContinuation().clone();

            RefCompiler.compileContinuation(memberContinuation, index);
            return (T) memberContinuation;
        } catch (Exception ex) {
            return null;
        }
    }

    public int size(MapProvider provider) {
        SafeOpt<List> map = readCast(provider);
        return map.map(m -> m.size()).orElse(0);
    }

    public int clear(MapProvider provider) {
        SafeOpt<List> list = readCast(provider);
        if (list.isPresent()) {
            List get = list.get();
            int size = get.size();
            get.clear();
            return size;
        }
        return 0;
    }

}
