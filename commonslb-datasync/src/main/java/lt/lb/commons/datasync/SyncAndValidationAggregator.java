package lt.lb.commons.datasync;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.datasync.base.BaseValidation;
import lt.lb.commons.iteration.For;

/**
 *
 * @author laim0nas100
 */
public interface SyncAndValidationAggregator<E extends SyncAndValidationAggregator> extends SyncValidation {

    /**
     * Add DataSyncManaged. Adds to both display and persist syncs.
     *
     * @param sync
     * @return
     */
    public default E addSync(DataSyncManaged sync) {
        addSyncPersist(sync);
        addSyncDisplay(sync);
        return me();
    }

    /**
     * Create a validation with predicate and a message function.
     *
     * @param <M>
     * @param pred
     * @param errorFunc
     * @return
     */
    public <M> Valid createValidation(Predicate<M> pred, Function<? super M, String> errorFunc);

    /**
     * Create a validation with predicate from a current row and a message
     * function.
     *
     * @param pred
     * @param errorFunc
     * @return
     */
    public Valid createValidationRow(Predicate<E> pred, Function<E, String> errorFunc);

    /**
     * {code this} supplement for extensions
     *
     * @return
     */
    public E me();

    /**
     * Get iterable of all persist syncs
     *
     * @return
     */
    public Iterable<DataSyncPersist> getPersists();

    /**
     * Get iterable of all display syncs
     *
     * @return
     */
    public Iterable<DataSyncDisplay> getDisplays();

    /**
     * Get iterable of all display validations
     *
     * @return
     */
    public Iterable<DisplayValidation> getDisplayValidations();

    /**
     * Get iterable of all persist validations
     *
     * @return
     */
    public Iterable<PersistValidation> getPersistValidations();

    @Override
    public default boolean validDisplay() {
        return !invalidDisplay(false);
    }

    @Override
    public default boolean validDisplayFull() {
        return !invalidDisplay(true);
    }

    @Override
    public default boolean isValidDisplay(Object from) {
        return !BaseValidation.iterateFindFirst(getDisplayValidations(), false, p -> p.isInvalidDisplay(from));
    }

    @Override
    public default void clearInvalidationDisplay(Object from) {
        getDisplayValidations().forEach(m -> m.clearInvalidationDisplay(from));
    }

    @Override
    public default boolean validPersist() {
        return !invalidPersist(false);
    }

    /**
     * Check and fire persist validations, stop at first invalid if not full.
     *
     * @param full
     * @return
     */
    public default boolean invalidPersist(boolean full) {
        boolean invalid = false;

        for (PersistValidation validation : getPersistValidations()) {
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
    public default boolean validPersistFull() {
        return !invalidPersist(true);
    }

    @Override
    public default boolean isValidPersist(Object from) {
        return !BaseValidation.iterateFindFirst(getPersistValidations(), false, p -> p.isInvalidPersist(from));
    }

    @Override
    public default void clearInvalidationPersist(Object from) {
        getPersistValidations().forEach(m -> m.clearInvalidationPersist(from));
    }

    /**
     * Check and fire display validations, stop at first invalid if not full.
     *
     * @param full
     * @return
     */
    public default boolean invalidDisplay(boolean full) {
        boolean invalid = false;
        for (DisplayValidation validation : getDisplayValidations()) {
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

    /**
     * Add DataSyncManagedValidation. Adds a sync and sync validation
     *
     * @param syncValid
     * @return
     */
    public default E addDataSyncValidation(DataSyncManagedValidation syncValid) {
        addSync(syncValid);
        addSyncValidation(syncValid);
        return me();
    }

    /**
     * Add DataSyncDisplay.
     *
     * @param sync
     * @return
     */
    public E addSyncDisplay(DataSyncDisplay sync);

    /**
     * Add DataSyncPersist.
     *
     * @param sync
     * @return
     */
    public E addSyncPersist(DataSyncPersist sync);

    /**
     * Add DisplayValidation.
     *
     * @param valid
     * @return
     */
    public E addDisplayValidation(DisplayValidation valid);

    /**
     * Add PersistValidation.
     *
     * @param valid
     * @return
     */
    public E addPersistValidation(PersistValidation valid);

    /**
     * Add SyncValidation.Adds a persist and display validation
     *
     * @param syncVal
     * @return
     */
    public default E addSyncValidation(SyncValidation syncVal) {
        addPersistValidation(syncVal);
        addDisplayValidation(syncVal);
        return me();
    }

    /**
     * Create a PersistAndDisplayValidation that can accept many validations,
     * with null managed value
     *
     * @param <M>
     * @param <V>
     * @return
     */
    public default <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidationUnmanaged() {
        return createBaseSyncValidationManaged(() -> null);
    }

    /**
     * Create a PersistAndDisplayValidation that can accept many validations,
     * with null managed value
     *
     * @param <M>
     * @param <V>
     * @param managed value supplier
     * @return
     */
    public <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidationManaged(Supplier<M> managed);

    /**
     * Create an unmanaged validation and add to it given Valid component and no
     * managed value, then add it to the persist validations
     *
     * @param valid
     * @return
     */
    public default E addValidationPersist(Valid valid) {
        PersistAndDisplayValidation unmanaged = createBaseSyncValidationUnmanaged();
        unmanaged.withPersistValidation(valid);
        addPersistValidation(unmanaged);
        return me();
    }

    /**
     * Create an unmanaged validation and add to it given Valid component and no
     * managed value, then add it to the display validations
     *
     * @param valid
     * @return
     */
    public default E addValidationDisplay(Valid valid) {
        PersistAndDisplayValidation unmanaged = createBaseSyncValidationUnmanaged();
        unmanaged.withDisplayValidation(valid);
        addDisplayValidation(unmanaged);
        return me();
    }

    /**
     * Create an unmanaged validation and add to it given Valid component and
     * managed value, then add it to the persist validations
     *
     * @param supl
     * @param valid
     * @return
     */
    public default E addValidationPersist(Supplier supl, Valid valid) {
        PersistAndDisplayValidation managed = createBaseSyncValidationManaged(supl);
        managed.withPersistValidation(valid);
        addPersistValidation(managed);
        return me();
    }

    /**
     * Create an unmanaged validation and add to it given Valid component and
     * managed value, then add it to the display validations
     *
     * @param supl
     * @param valid
     * @return
     */
    public default E addValidationDisplay(Supplier supl, Valid valid) {
        PersistAndDisplayValidation managed = createBaseSyncValidationManaged(supl);
        managed.withDisplayValidation(valid);
        addDisplayValidation(managed);
        return me();
    }

    /**
     * Create an unmanaged validation as a function and add to it given Valid
     * component and managed value, then add it to the persist validations
     *
     * @param maker
     * @return
     */
    public default E addValidationMakerPersist(Function<E, Valid> maker) {
        return addValidationPersist(maker.apply(me()));

    }

    /**
     * Create an unmanaged validation as a function and add to it given Valid
     * component and managed value, then add it to the display validations
     *
     * @param maker
     * @return
     */
    public default E addValidationMakerDisplay(Function<E, Valid> maker) {
        return addValidationDisplay(maker.apply(me()));

    }

    /**
     * Create an unmanaged validation, then create a Valid component with no
     * managed value from a predicate and a message supplier, then add it to the
     * persist validations
     *
     * @param msg
     * @param isValid
     * @return
     */
    public default E addValidationPersistPredicate(Supplier<String> msg, Predicate<E> isValid) {
        Valid<E> valid = createValidationRow(isValid, m -> msg.get());
        return addValidationPersist(valid);

    }

    /**
     * Create an unmanaged validation, then create a Valid component with no
     * managed value from a predicate and a message supplier, then add it to the
     * display validations
     *
     * @param msg
     * @param isValid
     * @return
     */
    public default E addValidationDisplayPredicate(Supplier<String> msg, Predicate<E> isValid) {
        Valid<E> valid = createValidationRow(isValid, m -> msg.get());
        return addValidationDisplay(valid);

    }

    /**
     * Create an unmanaged validation, then create a Valid component with no
     * managed value from a predicate and a message, then add it to the persist
     * validations
     *
     * @param msg
     * @param isValid
     * @return
     */
    public default E addValidationPersistPredicate(String msg, Predicate<E> isValid) {
        return addValidationPersistPredicate(() -> msg, isValid);
    }

    /**
     * Create an unmanaged validation, then create a Valid component with no
     * managed value from a predicate and a message, then add it to the display
     * validations
     *
     * @param msg
     * @param isValid
     * @return
     */
    public default E addValidationDisplayPredicate(String msg, Predicate<E> isValid) {
        return addValidationDisplayPredicate(() -> msg, isValid);
    }

    /**
     * Get DataSyncPersist object with given index or null
     *
     * @param <T>
     * @param index
     * @return
     */
    public default <T extends DataSyncPersist> T getPersistenceSync(int index) {
        return (T) For.elements().find(getPersists(), (i, item) -> {
            return i == index;
        }).map(m -> m.val).orElse(null);
    }

    /**
     * Get DataSyncDisplay object with given index or null
     *
     * @param <T>
     * @param index
     * @return
     */
    public default <T extends DataSyncDisplay> T getDisplaySync(int index) {
        return (T) For.elements().find(getDisplays(), (i, item) -> {
            return i == index;
        }).map(m -> m.val).orElse(null);
    }

    /**
     * Get PersistValidation object with given index or null
     *
     * @param <T>
     * @param index
     * @return
     */
    public default <T extends PersistValidation> T getPersistenceValid(int index) {
        return (T) For.elements().find(getPersistValidations(), (i, item) -> {
            return i == index;
        }).map(m -> m.val).orElse(null);
    }

    /**
     * Get DisplayValidation object with given index or null
     *
     * @param <T>
     * @param index
     * @return
     */
    public default <T extends DisplayValidation> T getDisplaySyncValid(int index) {
        return (T) For.elements().find(getDisplayValidations(), (i, item) -> {
            return i == index;
        }).map(m -> m.val).orElse(null);
    }

}
