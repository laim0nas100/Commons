package lt.lb.commons.containers.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.containers.tables.CellFormatBuilder.CellFormatIndexCollector;
import lt.lb.commons.containers.tuples.Pair;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.iteration.For;
import lt.lb.commons.misc.IntRange;
import lt.lb.fastid.FastID;

/**
 * Easy formatting table generator with cell merges and optional content.
 *
 * @author laim0nas100
 * @param <Format>
 * @param <T>
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
    static class Row<T> {

        private final int ri; // index in rows collection.
        private List<CellPrep<T>> cellPrep;
        private final CellTable<?, T> table;

        private Row(int ri, int size, CellTable<?, T> table) {
            cellPrep = new ArrayList<>(size);
            this.ri = ri;
            this.table = table;
        }

        private void modifyToSize(int size, T content) {
            while (getCells().size() < size) {
                getCells().add(new CellPrep<>(ri + table.rowOffset, table.colOffset + getCells().size(), content));
            }

            while (getCells().size() > size) {
                getCells().remove(getCells().size() - 1);
            }
        }

        public int size() {
            if (cellPrep == null) {
                return 0;
            }
            return cellPrep.size();
        }

        private void modifyToSize(int size) {
            this.modifyToSize(size, null);
        }

        public void add(T content) {
            getCells().add(new CellPrep<>(ri + table.rowOffset, table.colOffset + cellPrep.size(), content));
        }

        public void addAll(Collection<T> content) {
            if (content.isEmpty()) {
                return;
            }
            int size = getCells().size();
            for (T c : content) {
                getCells().add(new CellPrep<>(ri + table.rowOffset, table.colOffset + size++, c));
            }

        }

        public List<CellPrep<T>> getCells() {
            if (cellPrep == null) {
                cellPrep = new ArrayList<>();
            }
            return cellPrep;
        }

    }

    static <T> Row<T> getRow(CellTable<?, T> table, Integer row) {
        IntRange.of(table.rowOffset, table.rowOffset + table.rows.size()).assertIndexBoundsExclusive(row);
        return table.rows.get(row - table.rowOffset);
    }

    static <T> CellPrep<T> getCellAt(CellTable<?, T> table, Integer row, Integer col) {
        Row<T> r = getRow(table, row);
        IntRange.of(table.colOffset, table.colOffset + r.getCells().size()).assertIndexBoundsExclusive(col);
        return r.getCells().get(col - table.colOffset);
    }

    List<Row<T>> rows = new ArrayList<>();

    public final int rowOffset;
    public final int colOffset;

    public CellTable() {
        this(0, 0);
    }

    /**
     * Specify cell offset. Have effect on indexing.
     *
     * @param rowOffset
     * @param colOffset
     */
    public CellTable(int rowOffset, int colOffset) {
        this.rowOffset = rowOffset;
        this.colOffset = colOffset;
    }

    /**
     *
     * @param content array of content to add to cells.
     * @return
     */
    public CellTable<Format, T> addRow(T... content) {
        Row<T> row = new Row<>(getRowCount(), content.length, this);
        for (T c : content) {
            row.add(c);
        }
        this.rows.add(row);

        return this;
    }

    /**
     *
     * @param content collection of content to add to cells.
     * @return
     */
    public CellTable<Format, T> addRow(Collection<T> content) {
        Row<T> row = new Row<>(getRowCount(), content.size(), this);
        row.addAll(content);
        this.rows.add(row);
        return this;
    }

    protected <From, To> void copyCellPrep(CellPrep<From> from, CellPrep<To> to, Function<? super From, ? extends To> func) {
        to.content = from.content.map(func);
        to.diagonalMerge = from.diagonalMerge;
        to.horizontalMerge = from.horizontalMerge;
        to.verticalMerge = from.verticalMerge;
    }

    protected <TO> CellRowRenderer<Format, T> getExportRender(CellTable<?, TO> table, Function<? super T, ? extends TO> func) {
        CellRowRenderer<Format, T> renderer = (f, ri, cells) -> {
            Row row = new Row(table.getRowCount(), cells.size(), table);
            table.rows.add(row);
            int size = 0;
            for (CellPrep<T> prep : cells) {
                CellPrep<TO> cp = new CellPrep<>(row.ri, size++);
                copyCellPrep(prep, cp, func);
                row.add(cp);
            }

        };
        return renderer;
    }

    public <TO> void exportTable(IntRange range, CellTable<?, TO> table, Function<? super T, ? extends TO> func) {
        renderRows(range, null, getExportRender(table, func));
    }

    public <TO> void exportTable(CellTable<?, TO> table, Function<? super T, ? extends TO> func) {
        renderRows(null, getExportRender(table, func));
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
        IntRange.of(from, to).assertRangeSizeAtLeast(2);
        For.elements().withInterval(from - rowOffset, to + 1 - rowOffset).iterate(rows, (i, row) -> {
            CellPrep<T> cell = row.getCells().get(column);
            if (cell.verticalMerge != TableCellMerge.NONE) {
                throw new IllegalArgumentException("Overwriting existing vertical merge at " + formatVector(i + rowOffset, column + colOffset) + " clean existing merge first");
            }
            if (cell.diagonalMerge != TableCellMerge.NONE) {
                throw new IllegalArgumentException("Overwriting existing diagonal merge at " + formatVector(i + rowOffset, column + colOffset) + " clean existing merge first");
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
        IntRange.of(from, to).assertRangeSizeAtLeast(2);

        Row<T> r = getRow(this, row);

        for (int i = from + colOffset; i <= to + colOffset; i++) {
            int index = i - colOffset;
            CellPrep<T> cell = r.getCells().get(index);
            if (cell.diagonalMerge != TableCellMerge.NONE) {
                throw new IllegalArgumentException("Overwriting existing diagonal merge at " + formatVector(row, i) + " clean existing merge first");
            }
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

    public CellTable<Format, T> mergeDiagonal(int firstRow, int firstCol, int lastRow, int lastCol) {
        IntRange.of(firstRow, lastRow).assertRangeSizeAtLeast(2);
        IntRange.of(firstCol, lastCol).assertRangeSizeAtLeast(2);
        for (int ri = firstRow + rowOffset; ri <= lastRow + rowOffset; ri++) {
            for (int ci = firstCol + colOffset; ci <= lastCol + colOffset; ci++) {
                SafeOpt<CellPrep<T>> prep = findCell(ri, ci);
                if (prep.isEmpty()) {
                    throw new IllegalArgumentException("Diagonal merge cannot be used with jagged (uneven length) rows or columns within merge space. No cell " + formatVector(ri, ci));
                }
                CellPrep<T> cell = prep.get();
                if (cell.diagonalMerge != TableCellMerge.NONE) {
                    throw new IllegalArgumentException("Overwriting existing diagonal merge at " + formatVector(ri, ci) + " clean existing merge first");
                }
                if (cell.horizontalMerge != TableCellMerge.NONE) {
                    throw new IllegalArgumentException("Overwriting existing horizonal merge at " + formatVector(ri, ci) + " clean existing merge first");
                }
                if (cell.verticalMerge != TableCellMerge.NONE) {
                    throw new IllegalArgumentException("Overwriting existing vertical merge at " + formatVector(ri, ci) + " clean existing merge first");
                }
            }
        }

        CellPrep<T> upLeft = this.findCell(firstRow, firstCol).get();
        CellPrep<T> downRight = this.findCell(lastRow, lastCol).get();

        upLeft.diagonalMerge = TableCellMerge.FIRST;
        downRight.diagonalMerge = TableCellMerge.LAST;

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
        return this.mergeHorizontal(from, to, getLastRowIndex());
    }

    /**
     *
     * Merge empty cells horizontally of the latest row. Typically follows
     * addRow method.
     *
     * @return
     */
    public CellTable<Format, T> mergeLastEmpty() {
        Row<T> row = getRow(this, getLastRowIndex());
        For.elements().findBackwards(row.getCells(), (i, cell) -> {
            return cell.content.isPresent();
        }).map(m -> m.index).ifPresent(from -> {

            this.mergeHorizontal(from, colOffset + row.getCells().size() - 1, getLastRowIndex());
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
        return this.modifySize(getLastRowIndex(), desiredSize, content);
    }

    /**
     * Modify last row with null up to or down to given size.
     *
     * @param desiredSize
     * @return
     */
    public CellTable<Format, T> modifySize(int desiredSize) {
        return this.modifySize(getLastRowIndex(), desiredSize, null);
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

    public void doCells(IntRange rows, IntRange cols, Consumer<CellPrep<T>> cons) {
        CellRowRenderer<Format, T> renderer = (f, ri, list) -> {
            for (CellPrep<T> p : list) {
                if (cols != null) {
                    if (cols.min > p.colIndex) {
                        continue;
                    }
                    if (cols.max < p.colIndex) {
                        break;
                    }
                }
                cons.accept(p);
            }
        };
        if (rows == null) {
            this.renderRows(null, renderer);
        } else {
            this.renderRows(rows, null, renderer);
        }
    }

    /**
     * Stream rows within row index range [min,max] (both limits including) to
     * the renderer. Useful for splicing or paging.
     *
     * @param range
     * @param formatters
     * @param renderer
     */
    public void renderRows(IntRange range, final Formatters<Format> formatters, CellRowRenderer<Format, T> renderer) {
        Objects.requireNonNull(renderer);
        IntRange allowed = IntRange.of(rowOffset, rows.size() + rowOffset);
        allowed.assertIndexBoundsExclusive(range.min, range.max);
        range.assertRangeSizeAtLeast(1);

        for (int ri = range.min; ri <= range.max; ri++) {
            renderer.render(formatters, ri, getRow(this, ri).getCells());
        }
    }

    /**
     * Stream all rows to the renderer.
     *
     * @param formatters
     * @param renderer
     */
    public void renderRows(final Formatters<Format> formatters, CellRowRenderer<Format, T> renderer) {
        Objects.requireNonNull(renderer);
        int ri = 0;
        for (Row<T> row : rows) {
            renderer.render(formatters, ri + rowOffset, row.getCells());
            ri++;
        }
    }

    /**
     * Stream all rows to produce row-local tasks.
     *
     * @param formatters
     * @param renderer
     * @return
     */
    public List<Runnable> renderRowsBatch(final Formatters<Format> formatters, CellRowRenderer<Format, T> renderer) {
        Objects.requireNonNull(renderer);
        int ri = 0;
        ArrayList<Runnable> tasks = new ArrayList<>(rows.size());
        for (Row<T> row : rows) {
            final int i = ri + rowOffset;
            Runnable r = () -> {
                renderer.render(formatters, i, row.getCells());
            };
            tasks.add(r);
            ri++;
        }
        return tasks;
    }

    /**
     * Stream rows within row index range [min,max] (both limits including) to
     * produce row-local tasks. Useful for splicing or paging.
     *
     * @param range
     * @param formatters
     * @param renderer
     * @return
     */
    public List<Runnable> renderRowsBatch(IntRange range, final Formatters<Format> formatters, CellRowRenderer<Format, T> renderer) {
        Objects.requireNonNull(renderer);
        IntRange allowed = IntRange.of(rowOffset, rows.size() + rowOffset);
        allowed.assertIndexBoundsExclusive(range.min, range.max);
        range.assertRangeSizeAtLeast(1);
        ArrayList<Runnable> tasks = new ArrayList<>(range.getDiff() + 1);
        for (int ri = range.min + rowOffset; ri <= range.max + rowOffset; ri++) {
            final Row<T> row = getRow(this, ri);
            final int finalIndex = ri;
            Runnable r = () -> {
                renderer.render(formatters, finalIndex, row.getCells());
            };
            tasks.add(r);
        }
        return tasks;
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
        return getRowCount() - 1 + rowOffset;
    }

    /**
     * Get size of given row index
     *
     * @param ri
     * @return
     */
    public Integer getRowSize(int ri) {
        return getRow(this, ri).size();
    }

    /**
     * Get size of last row
     *
     * @return
     */
    public Integer getLastRowSize() {
        return getRowSize(getLastRowIndex());
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
        for (Row<T> row : this.rows) {
            for (CellPrep<T> cell : row.getCells()) {
                if (Objects.equals(cell.id, id)) {
                    return SafeOpt.of(Tuples.create(new Pair(cell.rowIndex, cell.colIndex), cell));
                }
            }
        }
        return SafeOpt.empty();
    }

    /**
     * Removes every row
     */
    public void clear() {
        rows.clear();
    }

    /**
     * Removes every cell, but keeps rows. Use this to "stream" a big table.
     * Write some rows, export them, clear cells.
     */
    public void clearOnlyCells() {
        for (Row r : rows) {
            if (r.cellPrep != null) {
                r.cellPrep.clear();
                r.cellPrep = null;
            }

        }
    }

    /**
     * Removes every cell in given range, but keeps rows.Use this to "stream" a
     * big table. Write some rows, export them, clear cells.
     *
     * @param range
     */
    public void clearOnlyCells(IntRange range) {
        IntRange allowed = IntRange.of(0, rows.size());
        allowed.assertIndexBoundsExclusive(range.min, range.max);
        range.assertRangeSizeAtLeast(1);

        for (int ri = range.min; ri <= range.max; ri++) {
            Row<T> r = rows.get(ri);
            if (r.cellPrep != null) {
                r.cellPrep.clear();
                r.cellPrep = null;
            }
        }
    }

}
