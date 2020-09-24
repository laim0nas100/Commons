package lt.lb.commons.datasync;

import lt.lb.commons.datasync.base.BaseValidation;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class SyncValidationAggregator implements SyncAndValidationAggregator {

    protected Collection<DataSyncPersist> persists = new LinkedHashSet<>();
    protected Collection<DataSyncDisplay> displays = new LinkedHashSet<>();
    protected Collection<DisplayValidation> displayValidations = new LinkedHashSet<>();
    protected Collection<PersistValidation> persistValidations = new LinkedHashSet<>();
    protected Function<Supplier, PersistAndDisplayValidation> factory;

    public SyncValidationAggregator(Function<Supplier, PersistAndDisplayValidation> factory) {
        this.factory = factory;
    }

    @Override
    public Iterable<DataSyncPersist> getPersists() {
        return persists;
    }

    @Override
    public Iterable<DataSyncDisplay> getDisplays() {
        return displays;
    }

    @Override
    public Iterable<DisplayValidation> getDisplayValidations() {
        return displayValidations;
    }

    @Override
    public Iterable<PersistValidation> getPersistValidations() {
        return persistValidations;
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
        PersistAndDisplayValidation unmanaged = createBaseSyncValidationUnmanaged();
        unmanaged.withDisplayValidation(validation);
        displayValidations.add(unmanaged);
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
        PersistAndDisplayValidation unmanaged = createBaseSyncValidationUnmanaged();
        unmanaged.withPersistValidation(validation);
        persistValidations.add(unmanaged);
    }

    @Override
    public boolean validPersist() {
        return !invalidPersist(false);
    }

    public boolean invalidPersist(boolean full) {
        boolean invalid = false;

        for (PersistValidation validation : persistValidations) {
            if (!full) {
                invalid = invalid || validation.invalidPersist();
                if (invalid) {
                    return invalid;
                }
            } else {
                invalid = invalid || validation.invalidPersistFull();
            }
        }

        return invalid;

    }

    @Override
    public boolean validPersistFull() {
        return !invalidPersist(true);
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
            if (!full) {
                invalid = invalid || validation.invalidDisplay();
                if (invalid) {
                    return invalid;
                }
            } else {
                invalid = invalid || validation.invalidDisplayFull();
            }
        }

        return invalid;

    }

    @Override
    public void addSyncDisplay(DataSyncDisplay sync) {
        displays.add(sync);
    }

    @Override
    public void addSyncPersist(DataSyncPersist sync) {
        persists.add(sync);
    }

    @Override
    public void addDisplayValidation(DisplayValidation valid) {
        displayValidations.add(valid);
    }

    @Override
    public void addPersistValidation(PersistValidation valid) {
        persistValidations.add(valid);
    }

    @Override
    public <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidationUnmanaged() {
        return factory.apply(()->null);
    }

    @Override
    public <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidationManaged(Supplier<M> supl) {
        return factory.apply(supl);
    }

}
