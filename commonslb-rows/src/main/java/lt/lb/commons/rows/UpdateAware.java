package lt.lb.commons.rows;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
public interface UpdateAware<U extends Updates, R extends UpdateAware> {

    public default String[] defaultUpdateNames() {
        return ArrayOp.asArray(UPDATES_ON_DISPLAY,
                UPDATES_ON_REFRESH,
                UPDATES_ON_RENDER,
                UPDATES_ON_VISIBLE,
                UPDATES_ON_DISABLE
        );
    }

    public default R initUpdates() {

        for (String type : defaultUpdateNames()) {
            getUpdateMap().put(type, getConfig().createUpdates(type, me()));
        }
        return me();
    }
    
    public default Set<String> getBindableTypes(){
        return getUpdateMap().keySet();
    }

    public R me();

    public Map<String, U> getUpdateMap();

    public UpdateConfigAware<U, R> getConfig();

    public default R withUpdates(U update) {
        R me = me();
        getUpdateMap().put(update.type, update);
        return me;
    }

    public default R withUpdate(String type, OrderedRunnable update) {
        R me = me();
        getUpdateMap().computeIfAbsent(type, t -> getConfig().createUpdates(type, me)).addUpdate(update);
        return me;
    }

    public default R withUpdate(String type, int order, Runnable run) {
        return withUpdate(type, new OrderedRunnable(order, run));
    }

    public default R withUpdate(String type, int order, Consumer<R> run) {
        return withUpdate(type, order, () -> run.accept(me()));
    }

    public default R withUpdateRefresh(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_REFRESH, order, run);
    }

    public default R withUpdateRefresh(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_REFRESH, 0, run);
    }

    public default R withUpdateRender(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_RENDER, order, run);
    }

    public default R withUpdateRender(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_RENDER, 0, run);
    }

    public default R withUpdateVisible(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_VISIBLE, order, run);
    }

    public default R withUpdateVisible(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_VISIBLE, 0, run);
    }

    public default R withUpdateDisable(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_DISABLE, order, run);
    }

    public default R withUpdateDisable(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_DISABLE, 0, run);
    }

    public default R withUpdateDisplay(int order, Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_DISPLAY, order, run);
    }

    public default R withUpdateDisplay(Consumer<R> run) {
        return withUpdate(BasicUpdates.UPDATES_ON_DISPLAY, 0, run);
    }

    public default R update(String type) {
        R me = me();
        U get = getUpdateMap().getOrDefault(type, null);

        Objects.requireNonNull(get, "Updates of type " + type + " was not registered in this context");

        getConfig().doUpdates(get, me);
        return me;
    }

    public default R update() {
        return update(UPDATES_ON_REFRESH);
    }
    
    public default R render() {
        return update(UPDATES_ON_RENDER);
    }

    public default R bindBindableUpdatesFrom(R source) {
        R me = me();
        source.bindBindableUpdates(me);
        return me;
    }

    public default R bindUpdatesFrom(String type, R source) {
        R me = me();
        source.bindUpdates(type, me);
        return me;
    }

    public default R bindUpdates(String type, R dest) {
        U u = getUpdateMap().getOrDefault(type, null);
        U de = (U) dest.getUpdateMap().getOrDefault(type, null);
        if (u == null || de == null) {
            return me();//failed to bind
        }
        u = (U) u.bindPropogate(de);
        getUpdateMap().put(type, u);
        return me();
    }

    public default R unbindUpdates(String type, R dest) {
        U u = getUpdateMap().getOrDefault(type, null);
        U de = (U) dest.getUpdateMap().getOrDefault(type, null);

        if (u == null || de == null) {
            return me();//failed to unbind
        }
        u = (U) u.unbind(de);
        getUpdateMap().put(type, u);
        return me();
    }

    public default R bindBindableUpdates(R dest) {
        for (String type : getBindableTypes()) {
            bindUpdates(type, dest);
        }
        return me();
    }

    public default R unbindBindableUpdates(R dest) {
        for (String type : getBindableTypes()) {
            unbindUpdates(type, dest);
        }
        return me();
    }
}
