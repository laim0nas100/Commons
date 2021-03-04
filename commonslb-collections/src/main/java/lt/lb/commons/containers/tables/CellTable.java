package lt.lb.commons.containers.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import lt.lb.commons.SafeOpt;
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

        private final int ri;
        private List<CellPrep<T>> cellPrep;

        private Row(int ri) {
            this.ri = ri;
            cellPrep = new ArrayList<>();
        }

        private Row(int ri, int size) {
            this.ri = ri;
            cellPrep = new ArrayList<>(size);
        }

        private void modifyToSize(int size, T content) {
            while (cellPrep.size() < size) {
                cellPrep.add(new CellPrep<>(ri, cellPrep.size(), content));
            }

            while (cellPrep.size() > size) {
                cellPrep.remove(cellPrep.size() - 1);
            }
        }

        private void modifyToSize(int size) {
            this.modifyToSize(size, null);
        }

        public void add(T content) {
            cellPrep.add(new CellPrep<>(ri, cellPrep.size(), content));
        }

        public void addAll(Collection<T> content) {
            if (content.isEmpty()) {
                return;
            }
            int size = cellPrep.size();
            for (T c : content) {
                cellPrep.add(new CellPrep<>(ri, size++, c));
            }

        }

        public List<CellPrep<T>> getCells() {
            return cellPrep;
        }

    }

    static <T> Row<T> getRow(CellTable<?, T> table, Integer row) {
        IntRange.of(0, table.rows.size()).assertIndexBoundsExclusive(row);
        return table.rows.get(row);
    }

    static <T> CellPrep<T> getCellAt(CellTable<?, T> table, Integer row, Integer col) {
        Row<T> r = getRow(table, row);
        IntRange.of(0, r.getCells().size()).assertIndexBoundsExclusive(col);
        return r.getCells().get(col);
    }

    List<Row<T>> rows = new ArrayList<>();

    public CellTable() {
    }

    /**
     *
     * @param content array of content to add to cells.
     * @return
     */
    public CellTable<Format, T> addRow(T... content) {
        Row<T> row = new Row<>(getRowCount(), content.length);
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
        Row<T> row = new Row<>(getRowCount(), content.size());
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
            Row row = new Row(table.getRowCount(), cells.size());
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
        For.elements().withInterval(from, to + 1).iterate(rows, (i, row) -> {
            CellPrep<T> cell = row.getCells().get(column);
            if (cell.verticalMerge != TableCellMerge.NONE) {
                throw new IllegalArgumentException("Overwriting existing vertical merge at " + formatVector(i, column) + " clean existing merge first");
            }
            if (cell.diagonalMerge != TableCellMerge.NONE) {
                throw new IllegalArgumentException("Overwriting existing diagonal merge at " + formatVector(i, column) + " clean existing merge first");
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

        Row<T> r = rows.get(row);

        for (int i = from; i <= to; i++) {
            CellPrep<T> cell = r.getCells().get(i);
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
        for (int ri = firstRow; ri <= lastRow; ri++) {
            for (int ci = firstCol; ci <= lastCol; ci++) {
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
        IntRange allowed = IntRange.of(0, rows.size());
        allowed.assertIndexBoundsExclusive(range.min, range.max);
        range.assertRangeSizeAtLeast(1);

        for (int ri = range.min; ri <= range.max; ri++) {
            renderer.render(formatters, ri, rows.get(ri).getCells());
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
            renderer.render(formatters, ri, row.getCells());
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
            final int i = ri;
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
        IntRange allowed = IntRange.of(0, rows.size());
        allowed.assertIndexBoundsExclusive(range.min, range.max);
        range.assertRangeSizeAtLeast(1);
        ArrayList<Runnable> tasks = new ArrayList<>(range.getDiff() + 1);
        for (int ri = range.min; ri <= range.max; ri++) {
            final Row<T> row = rows.get(ri);
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
        for (Row<T> row : this.rows) {
            for (CellPrep<T> cell : row.getCells()) {
                if (Objects.equals(cell.id, id)) {
                    return SafeOpt.of(Tuples.create(new Pair(cell.rowIndex, cell.colIndex), cell));
                }
            }
        }
        return SafeOpt.empty();
    }

}
