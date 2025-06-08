package lt.lb.zk.rows;

import lt.lb.commons.rows.base.BaseLine;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Row;

/**
 *
 * @author laim0nas100
 */
public class ZKLine<R extends ZKBaseDrow,DR extends ZKBaseDrows<R,DR>> extends BaseLine<DR,ZKCell, Component> {

    
    public Row row = new Row();

    public ZKLine(DR originalRows) {
        super(originalRows);
    }
    
    
    
}
