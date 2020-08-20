package lt.lb.commons.rows;

import lt.lb.commons.misc.UUIDgenerator;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseDrowsBindsConf<DR extends Drows, R extends Drow, U extends Updates> implements DrowsConf<DR, R, U> {

    @Override
    public R newRow(DR rows) {
        return newRow(rows, getNextRowID());
    }

    @Override
    public String getNextRowID() {
        return UUIDgenerator.nextUUID("Drow");
    }

    @Override
    public void composeDecorate(DR parentRows, DR childRows) {
        parentRows.bindDefaultUpdates(childRows);
        childRows.bindDefaultUpdates(parentRows);
    }

    @Override
    public void uncomposeDecorate(DR parentRows, DR childRows) {
        parentRows.unbindDefaultUpdates(childRows);
        childRows.unbindDefaultUpdates(parentRows);
    }

    @Override
    public void addRowDecorate(DR parentRows, R childRow) {
        parentRows.bindDefaultUpdates(childRow);
    }

    @Override
    public void removeRowDecorate(DR parentRows, R childRow) {
        parentRows.unbindDefaultUpdates(childRow);
    }

}
