/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.rows.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.rows.OrderedRunnable;
import lt.lb.commons.rows.SyncDrow;
import lt.lb.commons.rows.SyncDrowConf;
import lt.lb.commons.rows.UpdateHolder;
import lt.lb.commons.rows.Updates;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public abstract class BaseSyncDrowConfig<R extends SyncDrow, C, N, L, U extends Updates, Conf extends BaseSyncDrowConfig> implements SyncDrowConf<R, C, N, L, U>, UpdateHolder<U, Conf, R> {

    protected Map<String, U> updateMap = new HashMap<>();

    protected List<UnresolvedConsumer<R>> unresolved = new ArrayList<>();

    @Override
    public Map<String, U> getUpdateMap() {
        return updateMap;
    }

    @Override
    public void doUpdates(U updates, R object) {
        if (updates.active) {
            updates.triggerUpdate(Java.getNanoTime());
        }
    }

    protected R confupdatesFor;

    @Override
    public void configureUpdates(Map<String, U> updates, R object) {
        object.initUpdates();
        confupdatesFor = object;
        F.iterate(updateMap, (type, up) -> {
            List<OrderedRunnable> updateListeners = up.getUpdateListeners();
            for (OrderedRunnable run : updateListeners) {
                object.withUpdate(type, run);
            }

        });
        for (UnresolvedConsumer<R> consumer : unresolved) {
            object.withUpdate(consumer.type, new OrderedRunnable(consumer.order, () -> consumer.cons.accept(object)));
        }
        confupdatesFor = null;
    }

    @Override
    public U ensureUpdate(String type) {
        return getUpdateMap().computeIfAbsent(type, k -> createUpdates(type, confupdatesFor));
    }

    @Override
    public Conf withUpdate(String type, int order, Consumer<R> run) {
        unresolved.add(new UnresolvedConsumer<>(type, order, run));
        return me();
    }

    public static class UnresolvedConsumer<R> {

        public Consumer<R> cons;
        public String type;
        public int order;

        public UnresolvedConsumer(String type, int order, Consumer<R> cons) {
            this.cons = cons;
            this.type = type;
            this.order = order;
        }

    }

}
