package lt.lb.commons.rows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lt.lb.commons.DataSyncs.DataSyncDisplay;
import lt.lb.commons.DataSyncs.DataSyncManaged;
import lt.lb.commons.DataSyncs.DataSyncPersist;
import lt.lb.commons.DataSyncs.DisplayValidation;
import lt.lb.commons.DataSyncs.PersistValidation;
import lt.lb.commons.DataSyncs.SyncValidation;
import lt.lb.commons.DataSyncs.Valid;

/**
 *
 * @author laim0nas100
 */
public abstract class SyncDrow<C extends CellInf<N>, N, L, U extends Updates<U>, Conf extends SyncDrowConf<R, C, N, L, U>, R extends SyncDrow> extends Drow<C, N, L, U, Conf, R> {

    protected List<DataSyncPersist> persists = new ArrayList<>();
    protected List<DataSyncDisplay> displays = new ArrayList<>();
    protected List<DisplayValidation> displayValidations = new ArrayList<>();
    protected List<PersistValidation> persistValidations = new ArrayList<>();

    public SyncDrow(L line, Conf config, String key) {
        super(line, config, key);
    }

    public R addSync(DataSyncManaged sync) {
        return addOnDisplayAndRunIfDone(() -> {
            addSyncPersist(sync);
            addSyncDisplay(sync);
        });

    }

    public R addSyncDisplay(DataSyncDisplay sync) {
        return addOnDisplayAndRunIfDone(() -> {
            displays.add(sync);
        });

    }

    public R addSyncPersist(DataSyncPersist sync) {
        return addOnDisplayAndRunIfDone(() -> {
            persists.add(sync);
        });

    }

    public R addDisplayValidation(DisplayValidation valid) {
        return addOnDisplayAndRunIfDone(() -> {
            displayValidations.add(valid);
        });
    }

    public R addPersistValidation(PersistValidation valid) {
        return addOnDisplayAndRunIfDone(() -> {
            persistValidations.add(valid);
        });
    }

    public R addSyncValidation(SyncValidation syncVal) {
        addOnDisplayAndRunIfDone(() -> {
            addPersistValidation(syncVal);
            addDisplayValidation(syncVal);
        });

        return me();

    }

    public R addValidationPersist(Valid valid) {
        addOnDisplayAndRunIfDone(() -> {
            SyncValidation<Object, Valid<Object>> unmanaged = config.createBaseSyncValidationUnmanaged();
            unmanaged.withPersistValidation(valid);
            addPersistValidation(unmanaged);

        });

        return me();
    }

    public R addValidationDisplay(Valid valid) {
        addOnDisplayAndRunIfDone(() -> {
            SyncValidation<Object, Valid<Object>> unmanaged = config.createBaseSyncValidationUnmanaged();
            unmanaged.withDisplayValidation(valid);
            addDisplayValidation(unmanaged);
        });

        return me();
    }

    public R addValidationMakerPersist(Function<R, Valid> maker) {
        addOnDisplayAndRunIfDone(() -> {
            addValidationPersist(maker.apply(me()));
        });

        return me();
    }

    public R addValidationMakerDisplay(Function<R, Valid> maker) {
        addOnDisplayAndRunIfDone(() -> {
            addValidationDisplay(maker.apply(me()));
        });

        return me();
    }

    public R addValidationPersist(Supplier<String> msg, Predicate<R> isValid) {
        addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(), t -> isValid.test(me()), t -> msg.get());
            addValidationPersist(valid);
        });

        return me();

    }

    public R addValidationDisplay(Supplier<String> msg, Predicate<R> isValid) {
        addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(), t -> isValid.test(me()), t -> msg.get());
            addValidationDisplay(valid);
        });

        return me();

    }

    public R addValidationPersist(Supplier<String> msg, Predicate<R> isValid, Supplier<N> nodeSupplier) {
        addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(), t -> isValid.test(me()), t -> msg.get());
            addValidationPersist(valid);
        });

        return me();

    }

    public R addValidationDisplay(Supplier<String> msg, Predicate<R> isValid, Supplier<N> nodeSupplier) {

        addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(), null, nodeSupplier.get(), t -> isValid.test(me()), t -> msg.get());
            addValidationDisplay(valid);
        });

        return me();
    }

    public <M, V extends Valid<M>> R addValidationDisplay(V valid, Supplier<? extends M> managed) {
        addOnDisplayAndRunIfDone(() -> {
            SyncValidation<M, Valid<M>> managedValidation = config.createBaseSyncValidation(managed);
            managedValidation.withDisplayValidation(valid);
            addDisplayValidation(managedValidation);
        });
        return me();
    }

    public <M, V extends Valid<M>> R addValidationPersist(V valid, Supplier<? extends M> managed) {
        addOnDisplayAndRunIfDone(() -> {
            SyncValidation<M, Valid<M>> managedValidation = config.createBaseSyncValidation(managed);
            managedValidation.withPersistValidation(valid);
            addPersistValidation(managedValidation);
        });
        return me();
    }

    public void syncManagedFromDisplay() {
        for (DataSyncDisplay sync : displays) {
            sync.syncManagedFromDisplay();
        }
    }

    public void syncManagedFromPersist() {
        for (DataSyncPersist sync : persists) {
            sync.syncManagedFromPersist();
        }
    }

    public void syncDisplay() {
        for (DataSyncDisplay sync : displays) {
            sync.syncDisplay();
        }
    }

    public void syncPersist() {
        for (DataSyncPersist sync : persists) {
            sync.syncPersist();
        }
    }

    public boolean invalidPersist(boolean full) {
        boolean invalid = false;

        for (PersistValidation validation : persistValidations) {
            if (full) {
                invalid = invalid || validation.invalidPersistFull();
                if (invalid) {
                    return invalid;
                }
            } else {
                invalid = invalid || validation.invalidPersist();
            }
        }

        return invalid;

    }

    public boolean invalidDisplay(boolean full) {
        boolean invalid = false;
        for (DisplayValidation validation : displayValidations) {
            if (full) {
                invalid = invalid || validation.invalidDisplayFull();
                if (invalid) {
                    return invalid;
                }
            } else {
                invalid = invalid || validation.invalidDisplay();
            }
        }

        return invalid;

    }

}
