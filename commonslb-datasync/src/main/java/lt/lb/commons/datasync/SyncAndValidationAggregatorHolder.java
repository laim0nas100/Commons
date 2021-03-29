package lt.lb.commons.datasync;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public interface SyncAndValidationAggregatorHolder<E extends SyncAndValidationAggregatorHolder<E>> extends SyncAndValidationAggregator<E> {

    public SyncAndValidationAggregator<E> getAggregator();

    @Override
    public default Iterable<DataSyncPersist> getPersists() {
        return getAggregator().getPersists();
    }

    @Override
    public default Iterable<DataSyncDisplay> getDisplays() {
        return getAggregator().getDisplays();
    }

    @Override
    public default Iterable<DisplayValidation> getDisplayValidations() {
        return getAggregator().getDisplayValidations();
    }

    @Override
    public default Iterable<PersistValidation> getPersistValidations() {
        return getAggregator().getPersistValidations();
    }

    @Override
    public default E addSyncDisplay(DataSyncDisplay sync) {
        return getAggregator().addSyncDisplay(sync);
    }

    @Override
    public default E addSyncPersist(DataSyncPersist sync) {
        return getAggregator().addSyncPersist(sync);
    }

    @Override
    public default E addDisplayValidation(DisplayValidation valid) {
        return getAggregator().addDisplayValidation(valid);
    }

    @Override
    public default E addPersistValidation(PersistValidation valid) {
        return getAggregator().addPersistValidation(valid);
    }

    @Override
    public default <M> Valid createValidation(Predicate<M> pred, Function<? super M, String> errorFunc) {
        return getAggregator().createValidation(pred, errorFunc);
    }

    @Override
    public default Valid createValidationRow(Predicate<E> pred, Function<E, String> errorFunc) {
        return getAggregator().createValidationRow(pred, errorFunc);
    }

    @Override
    public default <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidationUnmanaged() {
        return getAggregator().createBaseSyncValidationUnmanaged();
    }

    @Override
    public default <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidationManaged(Supplier<M> managed) {
        return getAggregator().createBaseSyncValidationManaged(managed);
    }

    @Override
    public default void withDisplayValidation(Valid validation) {
        getAggregator().withDisplayValidation(validation);
    }

    @Override
    public default boolean validDisplay() {
        return getAggregator().validDisplay();
    }

    @Override
    public default boolean validDisplayFull() {
        return getAggregator().validDisplayFull();
    }

    @Override
    public default boolean isValidDisplay(Object from) {
        return getAggregator().isValidDisplay(from);
    }

    @Override
    public default void clearInvalidationDisplay(Object from) {
        getAggregator().clearInvalidationDisplay(from);
    }

    @Override
    public default void withPersistValidation(Valid validation) {
        getAggregator().withPersistValidation(validation);
    }

    @Override
    public default boolean validPersist() {
        return getAggregator().validPersist();
    }

    @Override
    public default boolean validPersistFull() {
        return getAggregator().validDisplayFull();
    }

    @Override
    public default boolean isValidPersist(Object from) {
        return getAggregator().isValidPersist(from);
    }

    @Override
    public default void clearInvalidationPersist(Object from) {
        getAggregator().clearInvalidationPersist(from);
    }

    @Override
    public default void syncDisplay() {
        getAggregator().syncDisplay();
    }

    @Override
    public default void syncManagedFromDisplay() {
        getAggregator().syncManagedFromDisplay();
    }

    @Override
    public default void syncPersist() {
        getAggregator().syncPersist();
    }

    @Override
    public default void syncManagedFromPersist() {
        getAggregator().syncManagedFromPersist();
    }

}
