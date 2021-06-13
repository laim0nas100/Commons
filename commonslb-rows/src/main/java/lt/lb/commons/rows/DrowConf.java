package lt.lb.commons.rows;

import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author laim0nas100
 * @param <R>
 * @param <C>
 * @param <N>
 * @param <L>
 * @param <U>
 */
public interface DrowConf<R extends Drow, C, N, L, U extends Updates> extends UpdateConfigAware<U, R> {

    /**
     * How much precision your rows need.
     *
     * @param drow
     * @return
     */
    public default int getMaxColSpan(R drow) {
        return 100;
    }

    /**
     * Used to calculate if colspan ratio difference is close enough, so no need
     * to resize colspans of this row. We are doing floating point number math,
     * so still need to use this to see if difference is close to zero.
     *
     * @param diff
     * @return
     */
    public default boolean colspanWithinMargin(double diff, R drow) {
        return diff <= 0.05;
    }

//    public CellInfo<C, N> getCellInfo(R drow);
    public N getEnclosingNode(R drow);

    public C createCell(List<N> nodes, N enclosingNode, R drow);

    /**
     * Should check if row is displayed and visible
     *
     * @param row
     * @param dirty if row was explicitly updated
     */
    public void renderRow(R row, boolean dirty);

    /**
     * Override this for different distribution of cell widths after hiding or
     * removing a cell in a row
     *
     * @param drow may customize for specific row
     * @param columnSpan
     * @param surplus
     * @return
     */
    public default Integer[] distributeSurplus(R drow, Integer[] columnSpan, int surplus) {
        return DrowUtils.ColSpan.distributeSurplusLeft(columnSpan, surplus);
    }

}
