package lt.lb.commons.rows.base;

import java.util.List;
import lt.lb.commons.rows.CellInfo;

/**
 *
 *
 * @param <C>
 * @param <N>
 * @author laim0nas100
 */
public class BaseCellInfo<C extends BaseCell<N, ?>, N> implements CellInfo<C, N> {

    @Override
    public List<N> getNodes(C cell) {
        return cell.getChildren();
    }

    @Override
    public int getColSpan(C cell) {
        return cell.getColspan();
    }

    @Override
    public void setColSpan(C cell, int colspan) {
        cell.setColspan(colspan);
    }

    @Override
    public boolean isVisible(C cell) {
        return cell.isVisible();
    }

    @Override
    public void setVisible(C cell, boolean visible) {
        cell.setVisible(visible);
    }

    @Override
    public boolean isDisabled(C cell) {
        return cell.isDisabled();
    }

    @Override
    public void setDisabled(C cell, boolean disabled) {
        cell.setDisabled(disabled);
    }

    @Override
    public boolean isMerged(C cell) {
        return cell.isMerged();
    }

    @Override
    public void setMerged(C cell, boolean merged) {
        cell.setMerged(merged);
    }

}
