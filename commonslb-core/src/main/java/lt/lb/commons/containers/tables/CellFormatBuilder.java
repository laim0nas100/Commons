package lt.lb.commons.containers.tables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import lt.lb.commons.containers.tables.CellTable.TableCellMerge;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lt.lb.commons.containers.tables.CellTable.CellFormatIndexCollector;

/**
 *
 * @author laim0nas100
 */
public class CellFormatBuilder<T> {

    protected CellFormatBuilder(CellTable<T> table, List<CellPrep<T>> prep) {
        cells = new HashSet<>();
        cells.addAll(prep);
        this.table = table;
    }
    protected CellTable<T> table;
    protected Set<CellPrep<T>> cells;
    protected Map<Long, List<Consumer>> formatters = new HashMap<>();

    /**
     * Define formatting action for currently selected cells
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder<T> addFormat(Consumer cons) {
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
    public CellFormatBuilder<T> forEachCell(Consumer<CellPrep<T>> cons) {
        cells.forEach(cons);
        return this;
    }

    /**
     * Clean all formatters
     *
     * @return
     */
    public CellFormatBuilder<T> cleanFormat() {
        formatters.clear();
        return this;
    }

    /**
     * Reset horizontal merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder<T> cleanHorizontalMerge() {
        return this.forEachCell(c -> c.horizontalMerge = TableCellMerge.NONE);
    }

    /**
     * Reset vertical merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder<T> cleanVerticalMerge() {
        return this.forEachCell(c -> c.verticalMerge = TableCellMerge.NONE);
    }

    /**
     * Append selection
     * @return 
     */
    public CellFormatIndexCollector<T> addToSelection() {
        return table.selectCells(Optional.of(this));
    }

    /**
     * Return all collected fomatters
     * @return 
     */
    public Map<Long, List<Consumer>> getFormatterMap() {
        return this.formatters;
    }
}
