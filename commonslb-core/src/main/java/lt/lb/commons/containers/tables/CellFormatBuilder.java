package lt.lb.commons.containers.tables;

import lt.lb.commons.containers.tables.CellTable.TableCellMerge;
import java.util.List;
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

    /**
     * Define formatting action
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder addFormat(Consumer<CellPrep<T>> cons) {
        for (CellPrep cell : cells) {
            cell.formatters.add(cons);
        }
        return this;
    }

    /**
     * Do with each selected cell
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
        for (CellPrep cell : cells) {
            cell.formatters.clear();
        }
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
}
