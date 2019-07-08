package lt.lb.commons.containers.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.misc.IntRange;

/**
 * Easy formatting table generator with cell merges and optional content.
 *
 * @author laim0nas100
 */
public class CellTable<T> {

    @FunctionalInterface
    public static interface CellRowRenderer<T> {

        public void render(Map<Long, List<Consumer>> formatters, Integer rowIndex, List<CellPrep<T>> cells);
    }

    public enum TableCellMerge {
        NONE, FIRST, PREVIOUS;
    }

    /**
     * Helper classes
     */
    private static class Row<T> {

        List<CellPrep<T>> cells = new ArrayList<>();

        private Row() {
        }

        private void modifyToSize(int size) {
            while (cells.size() < size) {
                cells.add(new CellPrep<>());
            }

            while (cells.size() > size) {
                cells.remove(cells.size() - 1);
            }
        }
    }

    private static abstract class CellFormatIndexCollect<T> {

        protected CellTable<T> table;
        protected Optional<CellFormatBuilder<T>> previous;

        protected CellFormatIndexCollect(Optional<CellFormatBuilder<T>> previous, CellTable<T> table) {
            this.table = table;
            this.previous = previous;
        }

        protected CellFormatBuilder<T> appendOrNew(List<CellPrep<T>> cells) {
            return previous.map(m -> {
                m.cells.addAll(cells);
                return m;
            }).orElseGet(() -> new CellFormatBuilder<>(table, cells));
        }
    }

    public static class CellFormatIndexCollectorRectangle<T> extends CellFormatIndexCollect<T> {

        private Integer startRow;
        private Integer startCol;

        private CellFormatIndexCollectorRectangle(Optional<CellFormatBuilder<T>> previous, CellTable table, Integer row, Integer col) {
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
        public CellFormatBuilder<T> toRightBottomCornerAt(Integer lastRow, Integer lastCol) {
            IntRange.of(startRow, lastRow).assertRangeIsValid();
            IntRange.of(startCol, lastCol).assertRangeIsValid();
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (int r = startRow; r < lastRow + 1; r++) {
                Row<T> row = getRow(table, r);
                F.iterate(row.cells, startCol, lastCol + 1, (i, cell) -> {
                    cells.add(cell);
                });
            }
            return appendOrNew(cells);
        }

    }

    public static class CellFormatIndexCollectorStart<T> extends CellFormatIndexCollect<T> {

        private final Integer index;

        private CellFormatIndexCollectorStart(Optional<CellFormatBuilder<T>> previous, CellTable table, Integer startIndex) {
            super(previous, table);
            this.index = startIndex;
        }

        /**
         * Specific cell
         *
         * @param end
         * @return
         */
        public CellFormatBuilder<T> andColumn(Integer end) {
            return appendOrNew(Arrays.asList(getCellAt(table, index, end)));
        }

        /**
         * All rows from index to specified endIndex.
         *
         * @param endIndex
         * @return
         */
        public CellFormatBuilder<T> includingRowsTo(Integer endIndex) {

            IntRange.of(index, endIndex).assertRangeIsValid();
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (int i = index; i < endIndex; i++) {
                Row<T> row = getRow(table, i);
                cells.addAll(row.cells);
            }
            return appendOrNew(cells);
        }

        /**
         * All columns from index to specified endIndex.
         *
         * @param endIndex
         * @return
         */
        public CellFormatBuilder<T> includingColumnsTo(Integer endIndex) {

            IntRange.of(index, endIndex).assertRangeIsValid();
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (Row<T> row : table.rows) {
                F.iterate(row.cells, index, endIndex + 1, (i, cell) -> {
                    cells.add(cell);
                });
            }
            return appendOrNew(cells);
        }

        /**
         * Collect columns in previously defined row
         *
         * @param cols
         * @return
         */
        public CellFormatBuilder<T> includingColumnsInRow(Integer... cols) {
            Row<T> row = table.rows.get(index);
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (Integer c : cols) {
                cells.add(row.cells.get(c));
            }
            return appendOrNew(cells);
        }

        /**
         * Collect rows in previously defined column
         *
         * @param rows
         * @return
         */
        public CellFormatBuilder<T> includingRowsInColumn(Integer... rows) {
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (Integer ri : rows) {
                cells.add(getCellAt(table, ri, index));

            }
            return appendOrNew(cells);
        }

    }

    public static class CellFormatIndexCollector<T> extends CellFormatIndexCollect<T> {

        private CellFormatIndexCollector(Optional<CellFormatBuilder<T>> previous, CellTable<T> table) {
            super(previous, table);
        }

        public CellFormatBuilder<T> withFullTable() {
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (Row<T> row : table.rows) {
                cells.addAll(row.cells);
            }
            return this.appendOrNew(cells);
        }

        /**
         * Specify row indexes or leave empty for full table.
         *
         * @param rows
         * @return
         */
        public CellFormatBuilder<T> withRows(Integer... rows) {
            if (rows.length == 0) {
                return this.withFullTable();
            }
            LinkedList<CellPrep<T>> cells = new LinkedList<>();

            for (Integer ri : rows) {
                Row<T> row = getRow(table, ri);
                cells.addAll(row.cells);
            }
            return this.appendOrNew(cells);
        }

        /**
         * Specify column indexes or leave empty for full table.
         *
         * @param columns
         * @return
         */
        public CellFormatBuilder<T> withColumns(Integer... columns) {
            if (columns.length == 0) {
                return this.withFullTable();
            }
            LinkedList<CellPrep<T>> cells = new LinkedList<>();

            for (Row<T> row : table.rows) {
                for (Integer ci : columns) {
                    if (ci < 0 || ci >= row.cells.size()) {
                        throw new IllegalArgumentException("Invalid column index " + ci + " where row size" + row.cells.size());
                    }
                    cells.add(row.cells.get(ci));
                }
            }
            return this.appendOrNew(cells);
        }

        /**
         * Linear builder, with starting index.
         *
         * @param index
         * @return
         */
        public CellFormatIndexCollectorStart<T> withIndex(Integer index) {
            return new CellFormatIndexCollectorStart<T>(previous, table, index);
        }

        /**
         * Select concrete cell
         *
         * @param ri row index
         * @param ci column index
         * @return
         */
        public CellFormatBuilder<T> withRowAndCol(Integer ri, Integer ci) {

            return withIndex(ri).andColumn(ci);
        }

        /**
         * Start selection rectangle.
         *
         * @param leftTopRow
         * @param leftTopColumn
         * @return
         */
        public CellFormatIndexCollectorRectangle<T> withRectangleStartingAt(Integer leftTopRow, Integer leftTopColumn) {
            return new CellFormatIndexCollectorRectangle<T>(previous, table, leftTopRow, leftTopColumn);
        }

    }

    private static <T> Row<T> getRow(CellTable<T> table, Integer row) {
        if (row < 0 || row >= table.rows.size()) {
            throw new IndexOutOfBoundsException(row + " index when rows are from 0 to " + table.rows.size());
        }

        return table.rows.get(row);
    }

    private static <T> CellPrep<T> getCellAt(CellTable<T> table, Integer row, Integer col) {
        Row<T> r = getRow(table, row);
        IntRange.of(0, r.cells.size()).assertIndexBoundsExclusive(col);
        return r.cells.get(col);
    }

    /**
     * DocTable class definition
     */
    private List<Row<T>> rows = new ArrayList<>();

    public CellTable() {
    }

    /**
     *
     * @param content array of content to add to cells.
     * @return
     */
    public CellTable addRow(T... content) {
        Row<T> row = new Row<>();
        for (T c : content) {
            row.cells.add(new CellPrep<>(c));
        }
        this.rows.add(row);

        return this;
    }

    /**
     * Replace row with new content. Row size can change. Existing formatting
     * remains.
     *
     * @param ri row index
     * @param content
     * @return
     */
    public CellTable setRowContent(Integer ri, T... content) {
        Row<T> row = getRow(this, ri);
        row.cells.forEach(c -> c.content = Optional.empty());
        row.modifyToSize(content.length);
        F.iterate(row.cells, (i, cell) -> {
            cell.content = Optional.ofNullable(content[i]);
        });
        return this;
    }

    /**
     *
     * @param ri row index
     * @param ci column index
     * @param content
     * @return
     */
    public CellTable setCellContent(Integer ri, Integer ci, T content) {
        getCellAt(this, ri, ci).content = Optional.ofNullable(content);
        return this;
    }

    /**
     *
     * @param ri row index
     * @param content new content to add
     * @return
     */
    public CellTable appendRowContent(Integer ri, T... content) {
        Row<T> row = getRow(this, ri);
        for (T c : content) {
            row.cells.add(new CellPrep<>(c));
        }
        return this;

    }

    /**
     * Merge cells vertically. Cells must be defined beforehand.
     *
     * @param from starting index (First)
     * @param to ending index (Last)
     * @param column column index
     */
    public CellTable mergeVertical(int from, int to, int column) {
        IntRange.of(from, to).assertRangeSizeAtLeast(1);
        F.iterate(rows, from, to, (i, row) -> {
            CellPrep<T> cell = row.cells.get(column);
            if (cell.verticalMerge != TableCellMerge.NONE) {
                throw new IllegalArgumentException("Overwriting existing vertical merge at " + formatVector(i, column) + " clean existing merge first");
            }
            if (i == from) {
                cell.verticalMerge = TableCellMerge.FIRST;
            } else {
                cell.verticalMerge = TableCellMerge.PREVIOUS;
            }
        });
        return this;
    }

    private static String formatVector(int x, int y) {
        return "[" + x + ", " + y + "]";
    }

    /**
     * Merge cells horizontally.
     *
     * @param from column start index
     * @param to column end index (inclusive)
     * @param row row index
     */
    public CellTable mergeHorizontal(int from, int to, int row) {
        IntRange.of(from, to).assertRangeSizeAtLeast(1);

        Row<T> r = rows.get(row);

        for (int i = from; i <= to; i++) {
            CellPrep<T> cell = r.cells.get(i);
            if (cell.horizontalMerge != TableCellMerge.NONE) {
                throw new IllegalArgumentException("Overwriting existing horizonal merge at " + formatVector(row, i) + " clean existing merge first");
            }
        }
        return this;
    }

    /**
     * Cell selector
     *
     * @return
     */
    public CellFormatIndexCollector<T> selectCells() {
        return selectCells(Optional.empty());
    }

    CellFormatIndexCollector<T> selectCells(Optional<CellFormatBuilder<T>> prevBuilder) {
        return new CellFormatIndexCollector<>(prevBuilder, this);
    }

    /**
     * Merge tables sharing references.
     *
     * @param other
     * @return
     */
    public CellTable appendTable(CellTable<T> other) {
        this.rows.addAll(other.rows);
        return this;
    }

    /**
     * Stream all rows to the renderer.
     *
     * @param formatters
     * @param renderer
     */
    public void renderRows(Map<Long, List<Consumer>> formatters, CellRowRenderer<T> renderer) {
        int ri = 0;
        for (Row<T> row : rows) {
            renderer.render(formatters, ri, row.cells);
            ri++;
        }
    }

    /**
     * Render without any implementation-dependant formatting map.
     *
     * @param renderer
     */
    public void renderRows(BiConsumer<Integer, List<CellPrep<T>>> renderer) {

        this.renderRows(new HashMap<>(), (map, ri, cells) -> renderer.accept(ri, cells));
    }

}
