package lt.lb.commons.containers.tables;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.misc.IntRange;

/**
 *
 * @author laim0nas100
 * @param <Format>
 * @param <T>
 */
public class StreamedWrite<Format, T> {

    public static <Format, T> void stream(
            final int pageSize,
            CellTable<Format, T> table,
            Stream<? extends Collection<T>> rowStream,
            BiFunction<IntRange, CellTable<Format, T>, Formatters<Format>> formattersProvider,
            CellTable.CellRowRenderer<Format, T> renderer) {
        if (pageSize < 10) {
            throw new IllegalArgumentException("Page size lower than " + 10);
        }
        Objects.requireNonNull(table, "Table is null");
        Objects.requireNonNull(rowStream, "Row stream is null");
        Objects.requireNonNull(formattersProvider, "Formatters provider is null");
        Objects.requireNonNull(renderer, "CellRowRenderer is null");
        IntegerValue page = new IntegerValue(table.getRowCount());
        rowStream.forEachOrdered(row -> {
            table.addRow(row);
            if (page.incrementAndGet() >= pageSize) {
                flush(page, table, formattersProvider, renderer);
            }

        });
        if (page.get() > 0) { // has unflushed
            flush(page, table, formattersProvider, renderer);
        }
    }

    private static <Format, T> void flush(IntegerValue page, CellTable<Format, T> table, BiFunction<IntRange, CellTable<Format, T>, Formatters<Format>> formattersProvider, CellTable.CellRowRenderer<Format, T> renderer) {
        if (page.get() > 0) { // has unflushed
            int i = table.getLastRowIndex();
            IntRange range = IntRange.of(1 + i - page.get(), i);
            table.renderRows(range, formattersProvider.apply(range, table), renderer);
            table.clearOnlyCells(range);
            page.set(0);
        }
    }

    protected int pageSize = 1000;
    protected BiFunction<IntRange, CellTable<Format, T>, Formatters<Format>> formattersProvider;
    protected CellTable.CellRowRenderer<Format, T> renderer;

    public void stream(CellTable<Format, T> table, Stream<Collection<T>> rowStream) {
        stream(pageSize, table, rowStream, formattersProvider, renderer);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public BiFunction<IntRange, CellTable<Format, T>, Formatters<Format>> getFormattersProvider() {
        return formattersProvider;
    }

    public void setFormattersProvider(BiFunction<IntRange, CellTable<Format, T>, Formatters<Format>> formattersProvider) {
        this.formattersProvider = formattersProvider;
    }

    public CellTable.CellRowRenderer<Format, T> getRenderer() {
        return renderer;
    }

    public void setRenderer(CellTable.CellRowRenderer<Format, T> renderer) {
        this.renderer = renderer;
    }

}
