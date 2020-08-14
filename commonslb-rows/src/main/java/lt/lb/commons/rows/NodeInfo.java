package lt.lb.commons.rows;

import java.util.List;

/**
 *
 * @author laim0nas100
 */
public interface NodeInfo<N> {

    public List<N> getChildren(N parent);

    public void appendChild(N parent, N child);

    public void removeChild(N parent, N child);

}
