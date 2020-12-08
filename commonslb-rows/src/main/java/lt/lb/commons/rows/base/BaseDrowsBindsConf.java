package lt.lb.commons.rows.base;

import java.util.HashMap;
import java.util.Map;
import lt.lb.commons.FastIDGen;
import lt.lb.commons.rows.Drow;
import lt.lb.commons.rows.Drows;
import lt.lb.commons.rows.DrowsConf;
import lt.lb.commons.rows.Updates;

/**
 *
 * @author laim0nas100
 * @param <DR> Drows
 * @param <R> Drow
 * @param <U> Updates
 */
public abstract class BaseDrowsBindsConf<DR extends Drows, R extends Drow, U extends Updates> implements DrowsConf<DR, R, U>  {

    protected static FastIDGen idgen = new FastIDGen();
    protected Map<String,U> updateMap = new HashMap<>();
    
    
    @Override
    public void configureUpdates(Map<String, U> updates, DR object) {
        object.initUpdates();
    }
    
    @Override
    public R newRow(DR rows) {
        return newRow(rows, getNextRowID());
    }

    @Override
    public String getNextRowID() {
        return idgen.getAndIncrement()+"-Drow";
    }

    @Override
    public void composeDecorate(DR parentRows, DR childRows) {
        parentRows.bindBindableUpdates(childRows);
        childRows.bindBindableUpdates(parentRows);
    }

    @Override
    public void uncomposeDecorate(DR parentRows, DR childRows) {
        parentRows.unbindBindableUpdates(childRows);
        childRows.unbindBindableUpdates(parentRows);
    }

    @Override
    public void addRowDecorate(DR parentRows, R childRow) {
        parentRows.bindBindableUpdates(childRow);
    }

    @Override
    public void removeRowDecorate(DR parentRows, R childRow) {
        parentRows.unbindBindableUpdates(childRow);
    }
}
