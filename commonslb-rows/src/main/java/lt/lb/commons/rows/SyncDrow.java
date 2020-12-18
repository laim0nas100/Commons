package lt.lb.commons.rows;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.datasync.DataSyncDisplay;
import lt.lb.commons.datasync.DataSyncManaged;
import lt.lb.commons.datasync.DataSyncManagedValidation;
import lt.lb.commons.datasync.DataSyncPersist;
import lt.lb.commons.datasync.DisplayValidation;
import lt.lb.commons.datasync.PersistValidation;
import lt.lb.commons.datasync.SyncAndValidationAggregator;
import lt.lb.commons.datasync.SyncAndValidationAggregatorHolder;
import lt.lb.commons.datasync.SyncValidation;
import lt.lb.commons.datasync.Valid;
import lt.lb.commons.iteration.For;

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
public abstract class SyncDrow<C extends CellInfo<N>, N, L, U extends Updates<U>, Conf extends SyncDrowConf<R, C, N, L, U>, R extends SyncDrow<C, N, L, U, Conf, R>>
        extends Drow<C, N, L, U, Conf, R> implements SyncValidation, SyncAndValidationAggregatorHolder<R> {

    protected SyncAndValidationAggregator agg;

    public SyncDrow(L line, Conf config, String key) {
        super(line, config, key);
        agg = config.createAggregator(me());
    }

    @Override
    public SyncAndValidationAggregator<R> getAggregator() {
        return agg;
    }

    
    public R addValidationPersist(Supplier<String> msg, Predicate<R> isValid, Supplier<N> nodeSupplier) {
        return addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(),null, nodeSupplier.get(),t -> isValid.test(me()), t -> msg.get());
            addValidationPersist(valid);
        });

    }

    public R addValidationDisplay(Supplier<String> msg, Predicate<R> isValid, Supplier<N> nodeSupplier) {

        return addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(), null, nodeSupplier.get(), t -> isValid.test(me()), t -> msg.get());
            addValidationDisplay(valid);
        });

    }

    @Override
    public void syncManagedFromDisplay() {
        agg.syncManagedFromDisplay();
    }

    @Override
    public void syncManagedFromPersist() {
        agg.syncManagedFromPersist();
    }

    @Override
    public void syncDisplay() {
        agg.syncDisplay();
    }

    @Override
    public void syncPersist() {
        agg.syncPersist();
    }

    @Override
    public void withDisplayValidation(Valid validation) {
        agg.withDisplayValidation(validation);
    }

    @Override
    public boolean validDisplay() {
        return agg.validDisplay();
    }

    @Override
    public boolean validDisplayFull() {
        return agg.validDisplayFull();
    }

    @Override
    public boolean isValidDisplay(Object from) {
        return agg.isValidDisplay(from);
    }

    @Override
    public void clearInvalidationDisplay(Object from) {
        agg.clearInvalidationDisplay(from);
    }

    @Override
    public void withPersistValidation(Valid validation) {
        agg.withPersistValidation(validation);
    }

    @Override
    public boolean validPersist() {
        return agg.validPersist();
    }

    @Override
    public boolean validPersistFull() {
        return agg.validPersistFull();
    }

    @Override
    public boolean isValidPersist(Object from) {
        return agg.isValidPersist(from);
    }

    @Override
    public void clearInvalidationPersist(Object from) {
        agg.clearInvalidationPersist(from);
    }
}
