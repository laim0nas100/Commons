package lt.lb.zk.rows;

import com.github.laim0nas100.commonslb.rows.DrowsConf;
import com.github.laim0nas100.commonslb.rows.SyncDrows;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Rows;

/**
 *
 * @author laim0nas100
 */
public abstract class ZKBaseDrows<R extends ZKBaseDrow, DR extends ZKBaseDrows<R,DR>> extends SyncDrows<R, ZKLine<R,DR>, DR, ZKUpdates> {

    public Grid grid;

    public Rows getZKRows() {
        return grid.getRows();
    }

    public ZKBaseDrows(Grid grid, String key, DrowsConf<DR, R, ZKUpdates> conf) {
        super(key, conf);
        this.grid = grid;
    }

}
