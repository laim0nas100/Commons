package lt.lb.commons.rows;

import lt.lb.commons.datasync.base.BaseValidation;
import lt.lb.commons.datasync.PureSyncValidation;


/**
 *
 * @author laim0nas100
 * @param <R>
 * @param <L>
 * @param <DR>
 * @param <U>
 */
public abstract class SyncDrows<R extends SyncDrow, L, DR extends SyncDrows, U extends Updates> extends Drows<R, L, DR, U> implements PureSyncValidation {

    
    public SyncDrows(String key, DrowsConf<DR, R, U> conf) {
        super(key, conf);
    }

    @Override
    public void syncPersist() {
        doInOrderNested(r -> r.syncPersist());
    }

    @Override
    public void syncManagedFromDisplay() {
        doInOrderNested(r -> r.syncManagedFromDisplay());
    }

    @Override
    public void syncManagedFromPersist() {
        doInOrderNested(r -> r.syncManagedFromPersist());
    }

    @Override
    public void syncDisplay() {
        doInOrderNested(r -> r.syncDisplay());
    }

    @Override
    public boolean validDisplay() {
        return !BaseValidation.iterateFindFirst(getRowsInOrderNested(), false, r->r.invalidDisplay());
    }

    @Override
    public boolean validDisplayFull() {
        return !BaseValidation.iterateFindFirst(getRowsInOrderNested(), true, r->r.invalidDisplayFull());
    }

    @Override
    public boolean isValidDisplay(Object from) {
        return !BaseValidation.iterateFindFirst(getRowsInOrderNested(), false, r->r.isInvalidDisplay(from));
    }

    @Override
    public void clearInvalidationDisplay(Object from) {
        doInOrderNested(r -> r.clearInvalidationDisplay(from));
    }

    @Override
    public boolean validPersist() {
        return !BaseValidation.iterateFindFirst(getRowsInOrderNested(), false, r->r.invalidPersist());
    }

    @Override
    public boolean validPersistFull() {
        return !BaseValidation.iterateFindFirst(getRowsInOrderNested(), true, r->r.invalidPersistFull());
    }

    @Override
    public boolean isValidPersist(Object from) {
        return !BaseValidation.iterateFindFirst(getRowsInOrderNested(), false, r->r.isInvalidPersist(from));
    }

    @Override
    public void clearInvalidationPersist(Object from) {
        doInOrderNested(r -> r.clearInvalidationPersist(from));
    }
}
