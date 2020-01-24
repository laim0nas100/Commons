package lt.lb.commons.mutmap;

import java.util.List;

/**
 * Partial mapping that can be applied to many objects, or the inner ones
 *
 * @author laim0nas100
 */
public class MutableMapList<From, To> {

    public final From from;
    public final To to;
    private boolean applied = false;

    public MutableMapList(From from, To to, List<MutablePartialMapperAct> list) {
        this.from = from;
        this.to = to;
        this.list = list;
    }

    private final List<MutablePartialMapperAct> list;

    /**
     * Apply mapping to the inner objects.
     * Can only apply once.
     *
     * @return wether applied successfully
     */
    public boolean apply() {
        if (applied) {
            return false;
        }
        applied = apply(from, to);
        return applied;

    }

    /**
     * Apply mapping to external objects.
     * @param from
     * @param to
     * @return 
     */
    public boolean apply(From from, To to) {
        if (list.isEmpty()) {
            return false;
        }
        list.forEach(m -> m.doMapping(from, to));
        return true;
    }
}
