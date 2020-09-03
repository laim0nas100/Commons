package lt.lb.commons.javafx.fxrows;

import java.util.Map;
import lt.lb.commons.rows.base.BaseDrowsBindsConf;

/**
 *
 * @author laim0nas100
 */
public class FXDrowsConf extends BaseDrowsBindsConf<FXDrows, FXDrow, FXUpdates> {

    public FXDrowConf conf = new FXDrowConf();

    @Override
    public FXDrow newRow(FXDrows rows, String key) {
        return new FXDrow(injectLine(rows), conf, key);
    }

    private FXLine injectLine(FXDrows rows) {
        FXLine line = new FXLine(rows);
        return line;
    }

    @Override
    public void composeDecorate(FXDrows parentRows, FXDrows childRows) {
        super.composeDecorate(parentRows, childRows);
    }

    @Override
    public void uncomposeDecorate(FXDrows parentRows, FXDrows childRows) {
        super.uncomposeDecorate(parentRows, childRows);
        childRows.getRowsInOrderNested().forEach(r -> r.render()); // to remove this
    }

    @Override
    public FXUpdates createUpdates(String type, FXDrows object) {
        return new FXUpdates(type);
    }

    @Override
    public void doUpdates(FXUpdates updates, FXDrows object) {
        updates.commit();
    }

    @Override
    public void configureUpdates(Map<String, FXUpdates> updates, FXDrows object) {
        super.configureUpdates(updates, object);
    }

    @Override
    public void addRowDecorate(FXDrows parentRows, FXDrow childRow) {
        super.addRowDecorate(parentRows, childRow);
    }

    @Override
    public void removeRowDecorate(FXDrows parentRows, FXDrow childRow) {
        super.removeRowDecorate(parentRows, childRow);
    }
    
    

}
