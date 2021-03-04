package lt.lb.commons.containers.tables;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lt.lb.commons.containers.tables.CellTable.TableCellMerge;
import lt.lb.fastid.FastID;
import lt.lb.fastid.FastIDGen;

/**
 *
 * @author laim0nas100
 */
public class CellPrep<T>  {

    private static final FastIDGen idInc = new FastIDGen();

    public final FastID id = idInc.getAndIncrement();
    public final int rowIndex;
    public final int colIndex;
    protected Optional<T> content;
    protected TableCellMerge verticalMerge = TableCellMerge.NONE;
    protected TableCellMerge horizontalMerge = TableCellMerge.NONE;
    protected TableCellMerge diagonalMerge = TableCellMerge.NONE;

    protected CellPrep(int rowIndex,int colIndex, T content) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.content = Optional.ofNullable(content);
    }

    protected CellPrep(int rowIndex,int colIndex) {
        this(rowIndex,colIndex, null);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.id);
        hash = 23 * hash + this.rowIndex;
        hash = 23 * hash + this.colIndex;
        hash = 23 * hash + Objects.hashCode(this.content);
        hash = 23 * hash + Objects.hashCode(this.verticalMerge);
        hash = 23 * hash + Objects.hashCode(this.horizontalMerge);
        hash = 23 * hash + Objects.hashCode(this.diagonalMerge);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CellPrep<?> other = (CellPrep<?>) obj;
        if (this.rowIndex != other.rowIndex) {
            return false;
        }
        if (this.colIndex != other.colIndex) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        if (this.verticalMerge != other.verticalMerge) {
            return false;
        }
        if (this.horizontalMerge != other.horizontalMerge) {
            return false;
        }
        if (this.diagonalMerge != other.diagonalMerge) {
            return false;
        }
        return true;
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
     *
     * @param mapper
     * @return
     */
    public CellPrep<T> mapContent(Function<? super T, ? extends T> mapper) {
        content = content.map(mapper);
        return this;
    }

    /**
     * Set new content value
     *
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

    public TableCellMerge getDiagonalMerge() {
        return diagonalMerge;
    }

    @Override
    public String toString() {
        return "CellPrep{" + "id=" + id + ", content=" + content + ", verticalMerge=" + verticalMerge + ", horizontalMerge=" + horizontalMerge + ", diagonalMerge=" + diagonalMerge + '}';
    }

}
