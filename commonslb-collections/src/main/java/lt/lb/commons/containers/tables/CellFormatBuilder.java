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
import lt.lb.fastid.FastID;
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
    protected Map<FastID, List<Consumer>> formatters = new HashMap<>();

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
     * Cleans format in only currently selected cells.
     *
     * @return
     */
    public CellFormatBuilder<T> cleanSelectedFormat() {
        return forEachCell(cell -> {
            formatters.remove(cell.id);
        });
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
     *
     * @return
     */
    public CellFormatIndexCollector<T> addToSelection() {
        return table.selectCells(Optional.of(this));
    }

    /**
     * Deselect currently selected cells
     *
     * @return
     */
    public CellFormatBuilder<T> cleanSelection() {
        this.cells.clear();
        return this;
    }

    /**
     * Cleans current selection and starts new. All defined formatters are
     * preserved.
     *
     * @return
     */
    public CellFormatIndexCollector<T> cleanSelectionStart() {
        return this.cleanSelection().addToSelection();
    }

    /**
     * Return all collected formatters
     *
     * @return
     */
    public Map<FastID, List<Consumer>> getFormatterMap() {
        return this.formatters;
    }

    /**
     * Ability to collect formatters mid-way.
     * @param cons
     * @return 
     */
    public CellFormatBuilder<T> withFormatterMap(Consumer<? super Map<FastID, ? extends List<Consumer>>> cons) {
        cons.accept(this.getFormatterMap());
        return this;

    }
}
