package lt.lb.commons.containers.tables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lt.lb.commons.containers.tables.CellSelectorBase.CellSelector;
import lt.lb.commons.containers.tables.CellTable.TableCellMerge;
import lt.lb.commons.misc.IntRange;
import lt.lb.fastid.FastID;

/**
 *
 * @author laim0nas100
 * @param <Format> decorator object type
 * @param <T> cell content type
 */
public class CellFormatBuilder<Format, T> {

    protected CellFormatBuilder(CellTable<Format, T> table, CellSelectorBase selector) {
        this.table = Objects.requireNonNull(table);
        this.selector = Objects.requireNonNull(selector);
    }

    protected CellSelectorBase selector;
    protected final CellTable<Format, T> table;
    protected Formatters<Format> formatters = Formatters.getDefault();

    protected void addPrev(CellSelectorBase prevSelector) {
        Objects.requireNonNull(prevSelector);

        CellSelectorBase base = Objects.requireNonNull(selector);
        if (base.prev == null) {
            base.prev = prevSelector;
        } else {
            if (prevSelector.prev != null) {
                throw new IllegalArgumentException("Prev selector to be relinked allready has a prev");
            }
            prevSelector.prev = base.prev;
            base.prev = prevSelector;

        }
    }

    /**
     * Define formatting action for currently selected cells
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder<Format, T> addFormat(Consumer<Format> cons) {
        formatters.computeIfAbsent(selector, select -> new LinkedList<>()).add(cons);
        return this;
    }

    /**
     * Do with each selected cell
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder<Format, T> forEachCell(Consumer<CellPrep<T>> cons) {
        table.doCells(null, null, cell -> {
            if (selector.test(cell)) {
                cons.accept(cell);
            }
        });
        return this;
    }

    /**
     * Clean all formatters.
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanAllFormat() {
        formatters.clear();
        return this;
    }

    /**
     * Replace format on selected cells. Same as calling
     * {@link CellFormatBuilder#cleanSelectedFormat} followed up by
     * {@link CellFormatBuilder#addFormat(Consumer)}
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder<Format, T> replaceFormat(Consumer<Format> cons) {
        LinkedList<Consumer<Format>> newList = new LinkedList<>();
        newList.add(cons);
        formatters.put(selector, newList);
        return this;
    }

    /**
     * Cleans format in only currently selected cells.
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanSelectedFormat() {
        formatters.remove(selector);
        return this;
    }

    /**
     * Reset horizontal merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanHorizontalMerge() {
        return this.forEachCell(c -> c.horizontalMerge = TableCellMerge.NONE);
    }

    /**
     * Reset vertical merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanVerticalMerge() {
        return this.forEachCell(c -> c.verticalMerge = TableCellMerge.NONE);
    }

    /**
     * Reset diagonal merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanDiagonalMerge() {
        return this.forEachCell(c -> c.diagonalMerge = TableCellMerge.NONE);
    }

    /**
     * Reset diagonal merge property to NONE
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanAllMerge() {
        return this.forEachCell(c -> {
            c.diagonalMerge = TableCellMerge.NONE;
            c.verticalMerge = TableCellMerge.NONE;
            c.horizontalMerge = TableCellMerge.NONE;
        });
    }

    /**
     * Append selection
     *
     * @return
     */
    public CellFormatIndexCollector<Format, T> addToSelection() {
        return table.selectCells(Optional.of(this));
    }

    /**
     * Deselect currently selected cells
     *
     * @return
     */
    public CellFormatBuilder<Format, T> cleanSelection() {
        this.selector = CellSelector.empty();
        return this;
    }

    /**
     * Cleans current selection and starts new. All defined formatters are
     * preserved.
     *
     * @return
     */
    public CellFormatIndexCollector<Format, T> cleanSelectionStart() {
        return this.cleanSelection().addToSelection();
    }

    /**
     * Return all collected formatters
     *
     * @return
     */
    public Formatters<Format> getFormatterMap() {
        return this.formatters;
    }

    /**
     * Ability to collect formatters mid-way.
     *
     * @param cons
     * @return
     */
    public CellFormatBuilder<Format, T> withFormatterMap(Consumer<Formatters<Format>> cons) {
        cons.accept(this.getFormatterMap());
        return this;

    }

    public static abstract class CellFormatIndexCollect<Format, T> {

        protected CellTable<Format, T> table;
        protected Optional<CellFormatBuilder<Format, T>> previous;

        protected CellFormatIndexCollect(Optional<CellFormatBuilder<Format, T>> previous, CellTable<Format, T> table) {
            this.table = table;
            this.previous = previous;
        }

        protected CellFormatBuilder<Format, T> appendOrNew(CellSelectorBase selector) {
            Objects.requireNonNull(selector);
            return previous.map(m -> {
                m.addPrev(selector);
                return m;
            }).orElseGet(() -> new CellFormatBuilder<>(table, selector));
        }
    }

    public static class CellFormatIndexCollector<Format, T> extends CellFormatIndexCollect<Format, T> {

        CellFormatIndexCollector(Optional<CellFormatBuilder<Format, T>> previous, CellTable<Format, T> table) {
            super(previous, table);
        }

        public CellFormatBuilder<Format, T> withFullTable() {
            return this.appendOrNew(CellSelector.full());
        }

        /**
         * Specify startRow indexes or leave empty for full table.
         *
         * @param rows
         * @return
         */
        public CellFormatBuilder<Format, T> withRows(Integer... rows) {
            return this.appendOrNew(CellSelector.rows(rows));
        }

        /**
         * Specify column indexes or leave empty for full table.
         *
         * @param columns
         * @return
         */
        public CellFormatBuilder<Format, T> withColumns(Integer... columns) {
            return this.appendOrNew(CellSelector.columns(columns));
//            if (columns.length == 0) {
//                return this.withFullTable();
//            }
//            LinkedList<CellPrep<T>> cells = new LinkedList<>();
//
//            for (CellTable.Row<T> row : table.rows) {
//                for (Integer ci : columns) {
//                    if (ci < 0 || ci >= row.getCells().size()) {
//                        throw new IllegalArgumentException("Invalid column index " + ci + " where row size" + row.getCells().size());
//                    }
//                    cells.add(row.getCells().get(ci));
//                }
//            }
//            return this.appendOrNew(cells);
        }

        /**
         * Linear builder, with starting index.
         *
         * @param index
         * @return
         */
        public CellFormatIndexCollectorStart<Format, T> withIndex(Integer index) {
            return new CellFormatIndexCollectorStart<>(previous, table, index);
        }

        /**
         * Select concrete cell
         *
         * @param ri startRow index
         * @param ci column index
         * @return
         */
        public CellFormatBuilder<Format, T> withRowAndCol(Integer ri, Integer ci) {
            return withIndex(ri).andColumn(ci);
        }

        /**
         * Start selection rectangle.
         *
         * @param leftTopRow
         * @param leftTopColumn
         * @return
         */
        public CellFormatIndexCollectorRectangle<Format, T> withRectangleStartingAt(Integer leftTopRow, Integer leftTopColumn) {
            return new CellFormatIndexCollectorRectangle<>(previous, table, leftTopRow, leftTopColumn);
        }

        /**
         * Specify explicit cell ids to include
         *
         * @param collection
         * @return
         */
        public CellFormatBuilder<Format, T> withCellIds(Set<FastID> collection) {
            Objects.requireNonNull(collection);
            Set<CellPrep> cellSet = new HashSet<>();
            table.doCells(null, null, cell -> {
                if (collection.contains(cell.id)) {
                    cellSet.add(cell);
                }
            });
            return this.appendOrNew(CellSelector.cellsInclude(cellSet));

        }

        /**
         * Specify explicit cell ids to exclude
         *
         * @param collection
         * @return
         */
        public CellFormatBuilder<Format, T> withoutCellIds(Set<FastID> collection) {
            Objects.requireNonNull(collection);
            Set<CellPrep> cellSet = new HashSet<>();
            table.doCells(null, null, cell -> {
                if (collection.contains(cell.id)) {
                    cellSet.add(cell);
                }
            });
            return this.appendOrNew(CellSelector.cellsExclude(cellSet));

        }

    }

    public static class CellFormatIndexCollectorRectangle<Format, T> extends CellFormatIndexCollect<Format, T> {

        protected int startRow;
        protected int startCol;

        CellFormatIndexCollectorRectangle(Optional<CellFormatBuilder<Format, T>> previous, CellTable<Format, T> table, int row, int col) {
            super(previous, table);
            this.startRow = row;
            this.startCol = col;
        }

        /**
         * Finish selection rectangle
         *
         * @param lastRow
         * @param lastCol
         * @return
         */
        public CellFormatBuilder<Format, T> toRightBottomCornerAt(int lastRow, int lastCol) {
            IntRange.of(startRow, lastRow).assertRangeIsValid();
            IntRange.of(startCol, lastCol).assertRangeIsValid();
            return appendOrNew(CellSelector.diagonal(startRow, startCol, lastRow, lastCol));
        }

    }

    public static class CellFormatIndexCollectorStart<Format, T> extends CellFormatIndexCollect<Format, T> {

        private final int index;

        CellFormatIndexCollectorStart(Optional<CellFormatBuilder<Format, T>> previous, CellTable table, int startIndex) {
            super(previous, table);
            this.index = startIndex;
        }

        /**
         * Specific cell
         *
         * @param end
         * @return
         */
        public CellFormatBuilder<Format, T> andColumn(int end) {
            return appendOrNew(CellSelector.cellAt(index, end));
        }

        /**
         * All rows from index to specified endIndex (inclusive).
         *
         * @param endIndex
         * @return
         */
        public CellFormatBuilder<Format, T> includingRowsTo(int endIndex) {

            IntRange of = IntRange.of(index, endIndex).assertRangeIsValid();
            List<Integer> rows = new ArrayList<>(of.getDiff());
            for (int i = index; i <= endIndex; i++) {
                rows.add(i);
            }
            return appendOrNew(CellSelector.rows(rows.toArray(new Integer[of.getDiff()])));
        }

        /**
         * All columns from index to specified endIndex (inclusive).
         *
         * @param endIndex
         * @return
         */
        public CellFormatBuilder<Format, T> includingColumnsTo(int endIndex) {

            IntRange of = IntRange.of(index, endIndex).assertRangeIsValid();
            List<Integer> cols = new ArrayList<>(of.getDiff());
            for (int i = index; i <= endIndex; i++) {
                cols.add(i);
            }
            return appendOrNew(CellSelector.columns(cols.toArray(new Integer[of.getDiff()])));
        }

        /**
         * Collect columns in previously defined row
         *
         * @param cols
         * @return
         */
        public CellFormatBuilder<Format, T> includingColumnsInRow(Integer... cols) {
            CellSelector select = CellSelector.columns(cols);
            select.mergeAND(CellSelector.rows(index));
            return appendOrNew(select);
        }

        /**
         * Collect rows in previously defined column
         *
         * @param rows
         * @return
         */
        public CellFormatBuilder<Format, T> includingRowsInColumn(Integer... rows) {
            CellSelector select = CellSelector.rows(rows);
            select.mergeAND(CellSelector.columns(index));
            return appendOrNew(select);
        }

    }

}
