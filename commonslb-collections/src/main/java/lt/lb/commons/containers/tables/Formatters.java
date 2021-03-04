package lt.lb.commons.containers.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.containers.ForwardingMap;
import lt.lb.commons.iteration.For;
import lt.lb.fastid.FastID;

/**
 *
 * @author laim0nas100
 */
public interface Formatters<Format> extends Map<CellSelectorBase, List<Consumer<Format>>> {

    public static interface ForwardingFormatters<Format> extends Formatters<Format>, ForwardingMap<CellSelectorBase, List<Consumer<Format>>> {

    }

    /**
     * Get applicable decorators for this cell
     *
     * @param <T>
     * @param cell
     * @return
     */
    public default <T> List<Consumer<Format>> getApplicable(CellPrep<T> cell) {
        List<Consumer<Format>> applicable = new ArrayList<>();
        For.entries().iterate(this, (selector, list) -> {
            if (selector.test(cell)) {
                applicable.addAll(list);
            }
        });
        return applicable;
    }

    /**
     * Get applicable decorators for each cell, resolving it in parallel (order
     * remains correct). Performance gains when cell collection is big.
     *
     * @param <T>
     * @param cells
     * @return
     */
    public default <T> Map<FastID, List<Consumer<Format>>> getApplicableThreaded(Collection<CellPrep<T>> cells) {
        ConcurrentHashMap<FastID, List<Consumer<Format>>> map = new ConcurrentHashMap<>();
        this.entrySet().stream().forEach(entry -> {
            CellSelectorBase selector = entry.getKey();
            List<Consumer<Format>> formats = entry.getValue();
            cells.stream().parallel().forEach(cell -> {
                if (selector.test(cell)) {
                    map.compute(cell.id, (id, list) -> {
                        list = F.nullSupp(list, ArrayList::new);
                        list.addAll(formats);
                        return list;
                    });
                }

            });

        });

        return map;
    }

    /**
     * Get applicable decorators for each cell.
     *
     * @param <T>
     * @param cells
     * @return
     */
    public default <T> Map<FastID, List<Consumer<Format>>> getApplicable(Collection<CellPrep<T>> cells) {
        Map<FastID, List<Consumer<Format>>> map = new HashMap<>();
        this.entrySet().stream().forEach(entry -> {
            CellSelectorBase selector = entry.getKey();
            List<Consumer<Format>> formats = entry.getValue();
            cells.stream().forEach(cell -> {
                if (selector.test(cell)) {
                    map.compute(cell.id, (id, list) -> {
                        list = F.nullSupp(list, ArrayList::new);
                        list.addAll(formats);
                        return list;
                    });
                }

            });

        });

        return map;
    }

    public static <FORM> Formatters<FORM> ofMap(Map<CellSelectorBase, List<Consumer<FORM>>> map) {
        Objects.requireNonNull(map);
        return (ForwardingFormatters<FORM>) () -> map;
    }

    public static <FORM> Formatters<FORM> getDefault() {
        return ofMap(new LinkedHashMap<>());
    }

}
