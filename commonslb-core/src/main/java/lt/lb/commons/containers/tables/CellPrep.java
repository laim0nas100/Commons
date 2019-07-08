package lt.lb.commons.containers.tables;

import lt.lb.commons.containers.tables.CellTable.TableCellMerge;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 *
 * @author laim0nas100
 */
public class CellPrep<T> {
    

    private static final AtomicLong idInc = new AtomicLong(0);
    
    public final long id = idInc.getAndIncrement();
    Optional<T> content;
    TableCellMerge verticalMerge = TableCellMerge.NONE;
    TableCellMerge horizontalMerge = TableCellMerge.NONE;

    public CellPrep(T content) {
        this.content = Optional.ofNullable(content);
    }

    public CellPrep() {
        this(null);
    }

    /**
     * 
     * @return optional content for this cell
     */
    public Optional<T> getContent() {
        return content;
    }
    
    /**
     * Map content based on Optional
     * @param mapper
     * @return 
     */
    public CellPrep<T> mapContent(Function<? super T, ? extends T> mapper) {
        content = content.map(mapper);
        return this;
    }

    /**
     * Set new content value
     * @param content 
     */
    public void setContent(T content) {
        this.content = Optional.ofNullable(content);
    }

    public TableCellMerge getHorizontalMerge() {
        return horizontalMerge;
    }

    public TableCellMerge getVerticalMerge() {
        return verticalMerge;
    }

    @Override
    public String toString() {
        return "CellPrep{" + "content=" + content + ", verticalMerge=" + verticalMerge + ", horizontalMerge=" + horizontalMerge + '}';
    }

}
