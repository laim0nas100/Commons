package lt.lb.commons.rows;

import java.util.List;

/**
 *
 * @author laim0nas100
 */
public interface CellInfo<N> {

    public List<N> getNodes();

    public int getColSpan();

    public void setColSpan(int colspan);

    public boolean isVisible();

    public void setVisible(boolean visible);

    public boolean isDisabled();

    public void setDisabled(boolean disabled);

    public boolean isMerged();

    public void setMerged(boolean merged);
}
