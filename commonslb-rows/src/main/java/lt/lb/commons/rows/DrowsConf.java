package lt.lb.commons.rows;

import lt.lb.commons.misc.UUIDgenerator;

/**
 *
 * @author laim0nas100
 */
public interface DrowsConf <DR extends Drows, R extends Drow>{
    
    
    public R newRow(DR rows,String key);
    
    public default R newRow(DR rows){
        return newRow(rows,UUIDgenerator.nextUUID("Drow"));
    }
    
    public void composeDecorate(DR parentRows,R parentRow, DR childRows);
    
    public void uncomposeDecorate(DR parentRows,R parentRow, DR childRows);
    
}
