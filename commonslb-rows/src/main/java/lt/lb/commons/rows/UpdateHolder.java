package lt.lb.commons.rows;

import java.util.Map;
import java.util.function.Consumer;
import lt.lb.commons.ArrayOp;
import static lt.lb.commons.rows.BasicUpdates.UPDATES_ON_DISABLE;
import static lt.lb.commons.rows.BasicUpdates.UPDATES_ON_DISPLAY;
import static lt.lb.commons.rows.BasicUpdates.UPDATES_ON_REFRESH;
import static lt.lb.commons.rows.BasicUpdates.UPDATES_ON_RENDER;
import static lt.lb.commons.rows.BasicUpdates.UPDATES_ON_VISIBLE;

/**
 *
 * @author laim0nas100
 */
public interface UpdateHolder<U extends Updates, P extends UpdateHolder, R extends UpdateAware> {

    public P me();

    public Map<String, U> getUpdateMap();

    public default String[] defaultUpdateNames() {
        return ArrayOp.asArray(UPDATES_ON_DISPLAY,
                UPDATES_ON_REFRESH,
                UPDATES_ON_RENDER,
                UPDATES_ON_VISIBLE,
                UPDATES_ON_DISABLE
        );
    }

    public default P withUpdates(U update) {
        P me = me();
        getUpdateMap().put(update.type, update);
        return me;
    }

    public default P withUpdate(String type, int order, Runnable run) {
        return withUpdate(type, new OrderedRunnable(order, run));
    }

    public U ensureUpdate(String type);

    public default P withUpdate(String type, OrderedRunnable update) {
        ensureUpdate(type).addUpdate(update);
        return me();
    }

    public P withUpdate(String type, int order, Consumer<R> run);

    public default P withUpdateRefresh(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_REFRESH, order, run);
    }

    public default P withUpdateRefresh(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_REFRESH, 0, run);
    }

    public default P withUpdateRender(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_RENDER, order, run);
    }

    public default P withUpdateRender(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_RENDER, 0, run);
    }

    public default P withUpdateVisible(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_VISIBLE, order, run);
    }

    public default P withUpdateVisible(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_VISIBLE, 0, run);
    }

    public default P withUpdateDisable(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_DISABLE, order, run);
    }

    public default P withUpdateDisable(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_DISABLE, 0, run);
    }

    public default P withUpdateDisplay(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_DISPLAY, order, run);
    }

    public default P withUpdateDisplay(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_DISPLAY, 0, run);
    }
}
