package lt.lb.commons.rows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.datasync.BaseValidation;

import lt.lb.commons.datasync.DataSyncDisplay;
import lt.lb.commons.datasync.DataSyncManaged;
import lt.lb.commons.datasync.DataSyncPersist;
import lt.lb.commons.datasync.DisplayValidation;
import lt.lb.commons.datasync.PersistAndDisplayValidation;
import lt.lb.commons.datasync.PersistValidation;
import lt.lb.commons.datasync.SyncValidation;
import lt.lb.commons.datasync.Valid;

/**
 *
 * @author laim0nas100
 * @param <C>
 * @param <N>
 * @param <L>
 * @param <U>
 * @param <Conf>
 * @param <R>
 */
public abstract class SyncDrow<C extends CellInfo<N>, N, L, U extends Updates<U>, Conf extends SyncDrowConf<R, C, N, L, U>, R extends SyncDrow> extends Drow<C, N, L, U, Conf, R> implements SyncValidation {

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
            PersistAndDisplayValidation<Object, Valid<Object>> unmanaged = config.createBaseSyncValidationUnmanaged();
            unmanaged.withPersistValidation(valid);
            addPersistValidation(unmanaged);

        });

        return me();
    }

    public R addValidationDisplay(Valid valid) {
        addOnDisplayAndRunIfDone(() -> {
            PersistAndDisplayValidation<Object, Valid<Object>> unmanaged = config.createBaseSyncValidationUnmanaged();
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
            PersistAndDisplayValidation<M, Valid<M>> managedValidation = config.createBaseSyncValidation(managed);
            managedValidation.withDisplayValidation(valid);
            addDisplayValidation(managedValidation);
        });
        return me();
    }

    public <M, V extends Valid<M>> R addValidationPersist(V valid, Supplier<? extends M> managed) {
        addOnDisplayAndRunIfDone(() -> {
            PersistAndDisplayValidation<M, Valid<M>> managedValidation = config.createBaseSyncValidation(managed);
            managedValidation.withPersistValidation(valid);
            addPersistValidation(managedValidation);
        });
        return me();
    }

    @Override
    public void syncManagedFromDisplay() {
        for (DataSyncDisplay sync : displays) {
            sync.syncManagedFromDisplay();
        }
    }

    @Override
    public void syncManagedFromPersist() {
        for (DataSyncPersist sync : persists) {
            sync.syncManagedFromPersist();
        }
    }

    @Override
    public void syncDisplay() {
        for (DataSyncDisplay sync : displays) {
            sync.syncDisplay();
        }
    }

    @Override
    public void syncPersist() {
        for (DataSyncPersist sync : persists) {
            sync.syncPersist();
        }
    }

    @Override
    public void withDisplayValidation(Valid validation) {
        addValidationDisplay(validation);
    }

    @Override
    public boolean validDisplay() {
        return !invalidDisplay(false);
    }

    @Override
    public boolean validDisplayFull() {
        return !invalidDisplay(true);
    }

    @Override
    public boolean isValidDisplay(Object from) {
        return !BaseValidation.iterateFindFirst(displayValidations, false, p -> p.isInvalidDisplay(from));
    }

    @Override
    public void clearInvalidationDisplay(Object from) {
        displayValidations.forEach(m -> m.clearInvalidationDisplay(from));
    }

    @Override
    public void withPersistValidation(Valid validation) {
        addValidationPersist(validation);
    }

    @Override
    public boolean validPersist() {
        return !invalidPersist(false);
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

    @Override
    public boolean validPersistFull() {
        return invalidPersist(false);
    }

    @Override
    public boolean isValidPersist(Object from) {
        return !BaseValidation.iterateFindFirst(persistValidations, false, p -> p.isInvalidPersist(from));
    }

    @Override
    public void clearInvalidationPersist(Object from) {
        persistValidations.forEach(m -> m.clearInvalidationPersist(from));
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
