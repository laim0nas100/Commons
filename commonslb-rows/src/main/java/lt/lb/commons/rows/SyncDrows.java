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
        doActiveRowsNested(r -> r.syncPersist());
    }

    @Override
    public void syncManagedFromDisplay() {
        doActiveRowsNested(r -> r.syncManagedFromDisplay());
    }

    @Override
    public void syncManagedFromPersist() {
        doActiveRowsNested(r -> r.syncManagedFromPersist());
    }

    @Override
    public void syncDisplay() {
        doActiveRowsNested(r -> r.syncDisplay());
    }

    @Override
    public boolean validDisplay() {
        return !BaseValidation.iterateFindFirst(getActiveRowsNested(), false, r -> r.invalidDisplay());
    }

    @Override
    public boolean validDisplayFull() {
        return !BaseValidation.iterateFindFirst(getActiveRowsNested(), true, r -> r.invalidDisplayFull());
    }

    @Override
    public boolean isValidDisplay(Object from) {
        return !BaseValidation.iterateFindFirst(getActiveRowsNested(), false, r -> r.isInvalidDisplay(from));
    }

    @Override
    public void clearInvalidationDisplay(Object from) {
        doActiveRowsNested(r -> r.clearInvalidationDisplay(from));
    }

    @Override
    public boolean validPersist() {
        return !BaseValidation.iterateFindFirst(getActiveRowsNested(), false, r -> r.invalidPersist());
    }

    @Override
    public boolean validPersistFull() {
        return !BaseValidation.iterateFindFirst(getActiveRowsNested(), true, r -> r.invalidPersistFull());
    }

    @Override
    public boolean isValidPersist(Object from) {
        return !BaseValidation.iterateFindFirst(getActiveRowsNested(), false, r -> r.isInvalidPersist(from));
    }

    @Override
    public void clearInvalidationPersist(Object from) {
        doActiveRowsNested(r -> r.clearInvalidationPersist(from));
    }

    /**
     * 
     * update
     * invalidate
     * render
     * Sync: managed,display
     */
    public void viewUpdate() {
        update();
        renderAfterStructureChange();
        syncDisplay();
    }

}
