package lt.lb.commons.rows;

import java.util.List;

/**
 *
 * @author laim0nas100
 */
public interface CellInfo<C, N> {

    public List<N> getNodes(C cell);

    public int getColSpan(C cell);

    public void setColSpan(C cell, int colspan);

    public boolean isVisible(C cell);

    public void setVisible(C cell, boolean visible);

    public boolean isDisabled(C cell);

    public void setDisabled(C cell, boolean disabled);
    
    public boolean isMerged(C cell);
    
    public void setMerged(C cell, boolean merged);

}
