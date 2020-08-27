package lt.lb.commons.rows;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import static lt.lb.commons.rows.BasicUpdates.UPDATES_ON_REFRESH;
import static lt.lb.commons.rows.BasicUpdates.UPDATES_ON_RENDER;

/**
 *
 * @author laim0nas100
 */
public interface UpdateAware<U extends Updates, R extends UpdateAware> extends UpdateHolder<U, R, R> {

    public default R initUpdates() {

        for (String type : defaultUpdateNames()) {
            getUpdateMap().put(type, getConfig().createUpdates(type, me()));
        }
        return me();
    }

    public UpdateConfigAware<U, R> getConfig();

    public default Set<String> getBindableTypes() {
        return getUpdateMap().keySet();
    }

    @Override
    public default R withUpdate(String type, int order, Consumer<R> run) {
        return withUpdate(type, new OrderedRunnable(order, () -> run.accept(me())));
    }

    @Override
    public default U ensureUpdate(String type) {
        return getUpdateMap().computeIfAbsent(type, k -> getConfig().createUpdates(type, me()));
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
