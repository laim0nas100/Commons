package lt.lb.commons.rows;

import lt.lb.commons.misc.UUIDgenerator;

/**
 *
 * @author laim0nas100
 */
public interface DrowsConf <DR extends Drows, R extends Drow, U extends Updates> extends UpdateConfigAware<U, DR>{
    
    
    public R newRow(DR rows,String key);
    
    public default R newRow(DR rows){
        return newRow(rows,UUIDgenerator.nextUUID("Drow"));
    }
    
    public default void composeDecorate(DR parentRows,DR childRows){};
    
    public default void uncomposeDecorate(DR parentRows, DR childRows){};
    
    public default void addRowDecorate(DR parentRows, R childRow){};
    
    public default void removeRowDecorate(DR parentRows, R childRow){};
    
}
