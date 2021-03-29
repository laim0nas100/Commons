package lt.lb.commons.datasync;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class SyncValidationAggregator<E extends SyncAndValidationAggregator<E>> implements SyncAndValidationAggregator<E> {

    public static class ValidArgs<M> {

        public Predicate<M> pred;
        public Function<? super M, String> error;
    }

    protected Collection<DataSyncPersist> persists = new LinkedHashSet<>();
    protected Collection<DataSyncDisplay> displays = new LinkedHashSet<>();
    protected Collection<DisplayValidation> displayValidations = new LinkedHashSet<>();
    protected Collection<PersistValidation> persistValidations = new LinkedHashSet<>();
    protected Function<Supplier, PersistAndDisplayValidation> factory;
    protected Function<ValidArgs<E>, Valid> validationFactoryRow;
    protected Function<ValidArgs, Valid> validationFactory;
    protected E me;

    public SyncValidationAggregator(
            E me,
            Function<Supplier, PersistAndDisplayValidation> factory,
            Function<ValidArgs, Valid> validationFactory,
            Function<ValidArgs<E>, Valid> validationFactoryRow
    ) {
        this.me = me;
        this.factory = factory;
        this.validationFactory = validationFactory;
        this.validationFactoryRow = validationFactoryRow;
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
    public void withPersistValidation(Valid validation) {
        PersistAndDisplayValidation unmanaged = createBaseSyncValidationUnmanaged();
        unmanaged.withPersistValidation(validation);
        persistValidations.add(unmanaged);
    }

    @Override
    public E addSyncDisplay(DataSyncDisplay sync) {
        displays.add(sync);
        return me();
    }

    @Override
    public E addSyncPersist(DataSyncPersist sync) {
        persists.add(sync);
        return me();
    }

    @Override
    public E addDisplayValidation(DisplayValidation valid) {
        displayValidations.add(valid);
        return me();
    }

    @Override
    public E addPersistValidation(PersistValidation valid) {
        persistValidations.add(valid);
        return me();
    }

    @Override
    public <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidationManaged(Supplier<M> supl) {
        return factory.apply(supl);
    }

    @Override
    public Valid createValidationRow(Predicate<E> pred, Function<E, String> errorFunc) {
        ValidArgs<E> validArgs = new ValidArgs<>();
        validArgs.pred = pred;
        validArgs.error = errorFunc;
        return validationFactoryRow.apply(validArgs);
    }

    @Override
    public E me() {
        return me;
    }

    @Override
    public <M> Valid createValidation(Predicate<M> pred, Function<? super M, String> errorFunc) {
        ValidArgs<M> validArgs = new ValidArgs<>();
        validArgs.pred = pred;
        validArgs.error = errorFunc;
        return validationFactory.apply(validArgs);
    }

}
