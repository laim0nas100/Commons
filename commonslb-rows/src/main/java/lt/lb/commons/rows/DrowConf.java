package lt.lb.commons.rows;

import java.util.List;

/**
 *
 * @author laim0nas100
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
     * so still need to use this to see id difference is close to zero.
     *
     * @param diff
     * @return
     */
    public default boolean colspanWithinMargin(double diff, R drow) {
        return diff <= 0.05;
    }

    public CellInfo<C, N> getCellInfo(R drow);

    public N getEnclosingNode(R drow);

    public C createCell(List<N> nodes, N enclosingNode, R drow);

    /**
     * Should check if row is displayed and visible
     *
     * @param row
     */
    public void renderRow(R row);

}
