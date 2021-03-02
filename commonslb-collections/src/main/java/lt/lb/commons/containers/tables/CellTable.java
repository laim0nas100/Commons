package lt.lb.commons.containers.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lt.lb.commons.Lazy;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.containers.tuples.Pair;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.iteration.For;
import lt.lb.commons.misc.IntRange;
import lt.lb.fastid.FastID;

/**
 * Easy formatting table generator with cell merges and optional content.
 *
 * @author laim0nas100
 */
public class CellTable<Format, T> {

    @FunctionalInterface
    public static interface CellRowRenderer<Format, T> {

        public void render(Formatters<Format> formatters, Integer rowIndex, List<CellPrep<T>> cells);
    }

    public enum TableCellMerge {
        NONE, FIRST, MIDDLE, LAST;
    }

    /**
     * Helper classes
     */
    private static class Row<T> {

        private List<CellPrep<T>> cellPrep = new ArrayList<>();
        private Lazy<Map<FastID, Integer>> ids = new Lazy<>(() -> computeIds());

        private AtomicBoolean update = new AtomicBoolean(false);

        private Row() {
        }

        private void modifyToSize(int size, T content) {
            boolean toSet = false;
            while (cellPrep.size() < size) {
                cellPrep.add(new CellPrep<>(content));
                toSet = true;

            }

            while (cellPrep.size() > size) {
                cellPrep.remove(cellPrep.size() - 1);
                toSet = true;
            }
            if (toSet) {
                update.compareAndSet(false, true);
            }
        }

        private void modifyToSize(int size) {
            this.modifyToSize(size, null);
        }

        public void add(T content) {
            cellPrep.add(new CellPrep<>(content));
            update.compareAndSet(false, true);
        }

        public List<CellPrep<T>> getCells() {
            return cellPrep;
        }

        private Map<FastID, Integer> computeIds() {
            HashMap<FastID, Integer> map = new HashMap<>();
            int index = 0;
            for (CellPrep<T> cell : cellPrep) {
                map.put(cell.id, index++);
            }

            return map;
        }

        private Map<FastID, Integer> getIds() {
            while (update.compareAndSet(true, false)) {
                ids = new Lazy<>(() -> computeIds());
            }

            return ids.get();
        }
    }

    private static abstract class CellFormatIndexCollect<Format, T> {

        protected CellTable<Format, T> table;
        protected Optional<CellFormatBuilder<Format, T>> previous;

        protected CellFormatIndexCollect(Optional<CellFormatBuilder<Format, T>> previous, CellTable<Format, T> table) {
            this.table = table;
            this.previous = previous;
        }

        protected CellFormatBuilder<Format, T> appendOrNew(List<CellPrep<T>> cells) {
            return previous.map(m -> {
                m.cells.addAll(cells);
                return m;
            }).orElseGet(() -> new CellFormatBuilder<>(table, cells));
        }
    }

    public static class CellFormatIndexCollectorRectangle<Format, T> extends CellFormatIndexCollect<Format, T> {

        private Integer startRow;
        private Integer startCol;

        private CellFormatIndexCollectorRectangle(Optional<CellFormatBuilder<Format, T>> previous, CellTable<Format, T> table, Integer row, Integer col) {
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
        public CellFormatBuilder<Format, T> toRightBottomCornerAt(Integer lastRow, Integer lastCol) {
            IntRange.of(startRow, lastRow).assertRangeIsValid();
            IntRange.of(startCol, lastCol).assertRangeIsValid();
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (int r = startRow; r < lastRow + 1; r++) {
                Row<T> row = getRow(table, r);
                For.elements().withInterval(startCol, lastCol + 1)
                        .iterate(row.getCells(), (i, cell) -> {
                            cells.add(cell);
                        });
            }
            return appendOrNew(cells);
        }

    }

    public static class CellFormatIndexCollectorStart<Format, T> extends CellFormatIndexCollect<Format, T> {

        private final Integer index;

        private CellFormatIndexCollectorStart(Optional<CellFormatBuilder<Format, T>> previous, CellTable table, Integer startIndex) {
            super(previous, table);
            this.index = startIndex;
        }

        /**
         * Specific cell
         *
         * @param end
         * @return
         */
        public CellFormatBuilder<Format, T> andColumn(Integer end) {
            return appendOrNew(Arrays.asList(getCellAt(table, index, end)));
        }

        /**
         * All rows from index to specified endIndex.
         *
         * @param endIndex
         * @return
         */
        public CellFormatBuilder<Format, T> includingRowsTo(Integer endIndex) {

            IntRange.of(index, endIndex).assertRangeIsValid();
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (int i = index; i < endIndex; i++) {
                Row<T> row = getRow(table, i);
                cells.addAll(row.getCells());
            }
            return appendOrNew(cells);
        }

        /**
         * All columns from index to specified endIndex.
         *
         * @param endIndex
         * @return
         */
        public CellFormatBuilder<Format, T> includingColumnsTo(Integer endIndex) {

            IntRange.of(index, endIndex).assertRangeIsValid();
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (Row<T> row : table.rows) {
                For.elements().withInterval(index, endIndex + 1)
                        .iterate(row.getCells(), (i, cell) -> {
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
        public CellFormatBuilder<Format, T> includingColumnsInRow(Integer... cols) {
            Row<T> row = table.rows.get(index);
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (Integer c : cols) {
                cells.add(row.getCells().get(c));
            }
            return appendOrNew(cells);
        }

        /**
         * Collect rows in previously defined column
         *
         * @param rows
         * @return
         */
        public CellFormatBuilder<Format, T> includingRowsInColumn(Integer... rows) {
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (Integer ri : rows) {
                cells.add(getCellAt(table, ri, index));

            }
            return appendOrNew(cells);
        }

    }

    public static class CellFormatIndexCollector<Format, T> extends CellFormatIndexCollect<Format, T> {

        private CellFormatIndexCollector(Optional<CellFormatBuilder<Format, T>> previous, CellTable<Format, T> table) {
            super(previous, table);
        }

        public CellFormatBuilder<Format, T> withFullTable() {
            LinkedList<CellPrep<T>> cells = new LinkedList<>();
            for (Row<T> row : table.rows) {
                cells.addAll(row.getCells());
            }
            return this.appendOrNew(cells);
        }

        /**
         * Specify row indexes or leave empty for full table.
         *
         * @param rows
         * @return
         */
        public CellFormatBuilder<Format, T> withRows(Integer... rows) {
            if (rows.length == 0) {
                return this.withFullTable();
            }
            LinkedList<CellPrep<T>> cells = new LinkedList<>();

            for (Integer ri : rows) {
                Row<T> row = getRow(table, ri);
                cells.addAll(row.getCells());
            }
            return this.appendOrNew(cells);
        }

        /**
         * Specify column indexes or leave empty for full table.
         *
         * @param columns
         * @return
         */
        public CellFormatBuilder<Format, T> withColumns(Integer... columns) {
            if (columns.length == 0) {
                return this.withFullTable();
            }
            LinkedList<CellPrep<T>> cells = new LinkedList<>();

            for (Row<T> row : table.rows) {
                for (Integer ci : columns) {
                    if (ci < 0 || ci >= row.getCells().size()) {
                        throw new IllegalArgumentException("Invalid column index " + ci + " where row size" + row.getCells().size());
                    }
                    cells.add(row.getCells().get(ci));
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
        public CellFormatIndexCollectorStart<Format, T> withIndex(Integer index) {
            return new CellFormatIndexCollectorStart<>(previous, table, index);
        }

        /**
         * Select concrete cell
         *
         * @param ri row index
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
         * Specify explicit cell ids
         *
         * @param collection
         * @return
         */
        public CellFormatBuilder<Format, T> withCellIds(Collection<FastID> collection) {
            List<CellPrep<T>> collect = collection.stream()
                    .distinct()
                    .map(m -> this.table.findCell(m))
                    .filter(f -> f.isPresent())
                    .map(m -> m.get())
                    .collect(Collectors.toList());
            return this.appendOrNew(collect);

        }

    }

    private static <T> Row<T> getRow(CellTable<?, T> table, Integer row) {
        if (row < 0 || row >= table.rows.size()) {
            throw new IndexOutOfBoundsException(row + " index when rows are from 0 to " + table.rows.size());
        }

        return table.rows.get(row);
    }

    private static <T> CellPrep<T> getCellAt(CellTable<?, T> table, Integer row, Integer col) {
        Row<T> r = getRow(table, row);
        IntRange.of(0, r.getCells().size()).assertIndexBoundsExclusive(col);
        return r.getCells().get(col);
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
    public CellTable<Format, T> addRow(T... content) {
        Row<T> row = new Row<>();
        for (T c : content) {
            row.add(c);
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
    public CellTable<Format, T> setRowContent(Integer ri, T... content) {
        Row<T> row = getRow(this, ri);
        row.getCells().forEach(c -> c.content = Optional.empty());
        row.modifyToSize(content.length);
        For.elements().iterate(row.getCells(), (i, cell) -> {
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
    public CellTable<Format, T> setCellContent(Integer ri, Integer ci, T content) {
        getCellAt(this, ri, ci).content = Optional.ofNullable(content);
        return this;
    }

    /**
     *
     * @param ri row index
     * @param content new content to add
     * @return
     */
    public CellTable<Format, T> appendRowContent(Integer ri, T... content) {
        Row<T> row = getRow(this, ri);
        for (T c : content) {
            row.add(c);
        }
        return this;

    }

    /**
     * Merge cells vertically.Cells must be defined beforehand.
     *
     * @param from starting index (First)
     * @param to ending index (Last)
     * @param column column index
     * @return
     */
    public CellTable<Format, T> mergeVertical(int from, int to, int column) {
        IntRange.of(from, to).assertRangeSizeAtLeast(1);
        For.elements().withInterval(from, to + 1).iterate(rows, (i, row) -> {
            CellPrep<T> cell = row.getCells().get(column);
            if (cell.verticalMerge != TableCellMerge.NONE) {
                throw new IllegalArgumentException("Overwriting existing vertical merge at " + formatVector(i, column) + " clean existing merge first");
            }
            if (i == from) {
                cell.verticalMerge = TableCellMerge.FIRST;
            } else if (i == to) {
                cell.verticalMerge = TableCellMerge.LAST;
            } else {
                cell.verticalMerge = TableCellMerge.MIDDLE;
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
     * @return
     */
    public CellTable<Format, T> mergeHorizontal(int from, int to, int row) {
        IntRange.of(from, to).assertRangeSizeAtLeast(1);

        Row<T> r = rows.get(row);

        for (int i = from; i <= to; i++) {
            CellPrep<T> cell = r.getCells().get(i);
            if (cell.horizontalMerge != TableCellMerge.NONE) {
                throw new IllegalArgumentException("Overwriting existing horizonal merge at " + formatVector(row, i) + " clean existing merge first");
            }
            if (i == from) {
                cell.horizontalMerge = TableCellMerge.FIRST;
            } else if (i == to) {
                cell.horizontalMerge = TableCellMerge.LAST;
            } else {
                cell.horizontalMerge = TableCellMerge.MIDDLE;
            }
        }
        return this;
    }

    /**
     * Merge cells horizontally of the latest row. Typically follows addRow
     * method.
     *
     * @param from column start index
     * @param to column end index (inclusive)
     * @return
     */
    public CellTable<Format, T> merge(int from, int to) {
        return this.mergeHorizontal(from, to, rows.size() - 1);
    }

    /**
     *
     * Merge empty cells horizontally of the latest row. Typically follows
     * addRow method.
     *
     * @return
     */
    public CellTable<Format, T> mergeLastEmpty() {
        Row<T> row = rows.get(this.getLastRowIndex());
        For.elements().findBackwards(row.getCells(), (i, cell) -> {
            return cell.content.isPresent();
        }).map(m -> m.index).ifPresent(from -> {
            this.mergeHorizontal(from, row.getCells().size() - 1, rows.size() - 1);
        });
        return this;
    }

    /**
     * Modify last row size to give size and merge last empty rows.
     *
     * @param size
     * @return
     */
    public CellTable<Format, T> mergeLastInsertEmpty(int size) {
        return modifySize(size).mergeLastEmpty();
    }

    /**
     * Modify row with given content up to or down to given size.
     *
     * @param rowIndex
     * @param desiredSize
     * @param content
     * @return
     */
    public CellTable<Format, T> modifySize(int rowIndex, int desiredSize, T content) {
        Row<T> lastRow = rows.get(rowIndex);
        lastRow.modifyToSize(desiredSize, content);
        return this;
    }

    /**
     * Modify last row with given content up to or down to given size.
     *
     * @param desiredSize
     * @param content
     * @return
     */
    public CellTable<Format, T> modifySize(int desiredSize, T content) {
        return this.modifySize(rows.size() - 1, desiredSize, content);
    }

    /**
     * Modify last row with null up to or down to given size.
     *
     * @param desiredSize
     * @return
     */
    public CellTable<Format, T> modifySize(int desiredSize) {
        return this.modifySize(rows.size() - 1, desiredSize, null);
    }

    /**
     * Cell selector
     *
     * @return
     */
    public CellFormatIndexCollector<Format, T> selectCells() {
        return selectCells(Optional.empty());
    }

    CellFormatIndexCollector<Format, T> selectCells(Optional<CellFormatBuilder<Format, T>> prevBuilder) {
        return new CellFormatIndexCollector<>(prevBuilder, this);
    }

    /**
     * Merge tables sharing references.
     *
     * @param other
     * @return
     */
    public CellTable<Format, T> appendTable(CellTable<Format, T> other) {
        this.rows.addAll(other.rows);
        return this;
    }

    /**
     * Stream all rows to the renderer.
     *
     * @param formatters
     * @param renderer
     */
    public void renderRows(Formatters<Format> formatters, CellRowRenderer<Format, T> renderer) {
        Objects.requireNonNull(renderer);
        int ri = 0;
        for (Row<T> row : rows) {
            renderer.render(formatters, ri, row.getCells());
            ri++;
        }
    }

    /**
     * Stream all rows to the renderer.
     *
     * @param exe
     * @param formatters
     * @param renderer
     * @return
     */
    public List<Future> renderRows(Executor exe, Formatters<Format> formatters, CellRowRenderer<Format, T> renderer) {
        Objects.requireNonNull(renderer);
        Objects.requireNonNull(exe);
        int ri = 0;
        ArrayList<Future> tasks = new ArrayList<>(rows.size());
        for (Row<T> row : rows) {
            final int i = ri;
            Runnable r = () -> {
                renderer.render(formatters, i, row.getCells());
            };
            FutureTask task = new FutureTask(r, null);
            exe.execute(task);
            tasks.add(task);
            ri++;
        }
        return tasks;
    }

    /**
     * Render without any implementation-dependant formatting map.
     *
     * @param renderer
     */
    public void renderRows(BiConsumer<Integer, List<CellPrep<T>>> renderer) {
        renderRows(null, (map, ri, cells) -> renderer.accept(ri, cells));
    }

    /**
     * Render without any implementation-dependant formatting map.
     *
     * @param exe
     * @param renderer
     * @return
     */
    public List<Future> renderRows(Executor exe, BiConsumer<Integer, List<CellPrep<T>>> renderer) {
        return renderRows(exe, null, (map, ri, cells) -> renderer.accept(ri, cells));
    }

    /**
     *
     * @return amount of rows
     */
    public Integer getRowCount() {
        return rows.size();
    }

    /**
     *
     * @return index of last row
     */
    public Integer getLastRowIndex() {
        return getRowCount() - 1;
    }

    /**
     * Find cell coordinates by id.
     *
     * @param id
     * @return
     */
    public SafeOpt<Pair<Integer>> findCellLocation(FastID id) {
        return findCellById(id).map(m -> m.g1);
    }

    /**
     * Find cell by id.
     *
     * @param id
     * @return
     */
    public SafeOpt<CellPrep<T>> findCell(FastID id) {
        return findCellById(id).map(m -> m.g2);
    }

    /**
     * Find cell by coordinates.
     *
     * @param ri
     * @param ci
     * @return
     */
    public SafeOpt<CellPrep<T>> findCell(int ri, int ci) {
        return SafeOpt.of(this).map(m -> getCellAt(m, ri, ci));
    }

    private SafeOpt<Tuple<Pair<Integer>, CellPrep<T>>> findCellById(FastID id) {
        IntegerValue ri = new IntegerValue(-1);
        for (Row<T> row : this.rows) {
            ri.incrementAndGet();
            if (row.getIds().containsKey(id)) {
                int col = row.getIds().get(id);
                CellPrep<T> cell = row.getCells().get(col);
                return SafeOpt.of(Tuples.create(new Pair(ri.get(), col), cell));
            }
        }
        return SafeOpt.empty();
    }

}
