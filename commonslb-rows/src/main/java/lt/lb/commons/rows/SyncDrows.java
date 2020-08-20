package lt.lb.commons.rows;

import lt.lb.commons.DataSyncs;

/**
 *
 * @author laim0nas100
 */
public abstract class SyncDrows<R extends SyncDrow, L, DR extends SyncDrows, U extends Updates> extends Drows<R, L, DR, U> {

    public SyncDrows(String key, DrowsConf<DR, R, U> conf) {
        super(key, conf);
    }

    public boolean isInvalidPersist(boolean all) {
        return DataSyncs.BaseValidation.iterateFindFirst(getRowsInOrderNested(), all, r -> r.invalidPersist(all));
    }

    public boolean isInvalidDisplay(boolean all) {
        return DataSyncs.BaseValidation.iterateFindFirst(getRowsInOrderNested(), all, r -> r.invalidDisplay(all));
    }

    public void syncPersist() {
        doInOrderNested(r -> r.syncPersist());
    }

    public void syncManagedFromDisplay() {
        doInOrderNested(r -> r.syncManagedFromDisplay());
    }

    public void syncManagedFromPersist() {
        doInOrderNested(r -> r.syncManagedFromPersist());
    }

    public void syncDisplay() {
        doInOrderNested(r -> r.syncDisplay());
    }

}
