package lt.lb.commons.containers.tables;

import java.util.HashMap;
import java.util.LinkedList;
import lt.lb.commons.containers.tables.CellTable.TableCellMerge;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author laim0nas100
 */
public class CellFormatBuilder<T> {

    protected CellFormatBuilder(List<CellPrep<T>> prep) {
        cells = prep;
    }
    protected List<CellPrep<T>> cells;
    protected Map<Long, List<Consumer>> formatters = new HashMap<>();

    /**
     * Define formatting action
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder addFormat(Consumer cons) {
        for (CellPrep cell : cells) {
            formatters.computeIfAbsent(cell.id, id -> new LinkedList<>()).add(cons);
        }
        return this;
    }

    /**
     * Do with each selected cell
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder forEachCell(Consumer<CellPrep<T>> cons) {
        cells.forEach(cons);
        return this;
    }

    /**
     * Clean all formatters
     *
     * @return
     */
    public CellFormatBuilder cleanFormat() {
        formatters.clear();
        return this;
    }

    /**
     * Reset horizontal merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder cleanHorizontalMerge() {
        return this.forEachCell(c -> c.horizontalMerge = TableCellMerge.NONE);
    }

    /**
     * Reset vertical merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder cleanVerticalMerge() {
        return this.forEachCell(c -> c.verticalMerge = TableCellMerge.NONE);
    }

    public Map<Long, List<Consumer>> getFormatterMap() {
        return this.formatters;
    }
}
