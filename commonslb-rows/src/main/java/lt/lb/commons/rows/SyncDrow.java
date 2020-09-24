package lt.lb.commons.rows;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.F;

import lt.lb.commons.datasync.DataSyncDisplay;
import lt.lb.commons.datasync.DataSyncManaged;
import lt.lb.commons.datasync.DataSyncManagedValidation;
import lt.lb.commons.datasync.DataSyncPersist;
import lt.lb.commons.datasync.DisplayValidation;
import lt.lb.commons.datasync.PersistValidation;
import lt.lb.commons.datasync.SyncAndValidationAggregator;
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
public abstract class SyncDrow<C extends CellInfo<N>, N, L, U extends Updates<U>, Conf extends SyncDrowConf<R, C, N, L, U>, R extends SyncDrow<C, N, L, U, Conf, R>> extends Drow<C, N, L, U, Conf, R> implements SyncValidation {

    protected SyncAndValidationAggregator agg;

    public SyncDrow(L line, Conf config, String key) {
        super(line, config, key);
        agg = config.createAggregator();
    }

    public R addSync(DataSyncManaged sync) {
        return addOnDisplayAndRunIfDone(() -> {
            agg.addSync(sync);
        });

    }

    public R addDataSyncValidation(DataSyncManagedValidation sync) {
       return addOnDisplayAndRunIfDone(() -> {
            agg.addDataSyncValidation(sync);
        });
    }

    public R addSyncDisplay(DataSyncDisplay sync) {

        return addOnDisplayAndRunIfDone(() -> {
            agg.addSyncDisplay(sync);
        });

    }

    public R addSyncPersist(DataSyncPersist sync) {
        return addOnDisplayAndRunIfDone(() -> {
            agg.addSyncPersist(sync);
        });

    }

    public R addDisplayValidation(DisplayValidation valid) {
        return addOnDisplayAndRunIfDone(() -> {
            agg.addDisplayValidation(valid);
        });
    }

    public R addPersistValidation(PersistValidation valid) {
        return addOnDisplayAndRunIfDone(() -> {
            agg.addPersistValidation(valid);
        });
    }

    public R addSyncValidation(SyncValidation syncVal) {
        return addOnDisplayAndRunIfDone(() -> {
            agg.addSyncValidation(syncVal);
        });

    }

    public R addValidationPersist(Valid valid) {
        return addOnDisplayAndRunIfDone(() -> {
            agg.addValidationPersist(valid);
        });
    }

    public R addValidationDisplay(Valid valid) {
        return addOnDisplayAndRunIfDone(() -> {
            agg.addValidationDisplay(valid);
        });

    }

    public R addValidationMakerPersist(Function<R, Valid> maker) {
        return addOnDisplayAndRunIfDone(() -> {
            addValidationPersist(maker.apply(me()));
        });

    }

    public R addValidationMakerDisplay(Function<R, Valid> maker) {
        return addOnDisplayAndRunIfDone(() -> {
            addValidationDisplay(maker.apply(me()));
        });

    }

    public R addValidationPersistPredicate(Supplier<String> msg, Predicate<R> isValid) {
        return addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(), t -> isValid.test(me()), t -> msg.get());
            addValidationPersist(valid);
        });

    }

    public R addValidationDisplayPredicate(Supplier<String> msg, Predicate<R> isValid) {
        return addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(), t -> isValid.test(me()), t -> msg.get());
            addValidationDisplay(valid);
        });

    }

    public R addValidationPersist(Supplier<String> msg, Predicate<R> isValid, Supplier<N> nodeSupplier) {
        return addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(), t -> isValid.test(me()), t -> msg.get());
            addValidationPersist(valid);
        });

    }

    public R addValidationDisplay(Supplier<String> msg, Predicate<R> isValid, Supplier<N> nodeSupplier) {

        return addOnDisplayAndRunIfDone(() -> {
            Valid valid = config.createValidation(me(), null, nodeSupplier.get(), t -> isValid.test(me()), t -> msg.get());
            addValidationDisplay(valid);
        });

    }

    public <M, V extends Valid<M>> R addValidationDisplay(Supplier<? extends M> managed, V valid) {
        return addOnDisplayAndRunIfDone(() -> {
            agg.addValidationDisplay(managed, valid);
        });
    }

    public <M, V extends Valid<M>> R addValidationPersist(Supplier<? extends M> managed, V valid) {
        return addOnDisplayAndRunIfDone(() -> {
            agg.addValidationPersist(managed, valid);
        });
    }

    public SyncAndValidationAggregator getSyncAggregator() {
        return agg;
    }

    public <T extends DataSyncPersist> T getPersistenceSync(int index) {
        return (T) F.find(getSyncAggregator().getPersists(), (i, item) -> {
            return i == index;
        }).map(m -> m.g2).orElse(null);
    }

    public <T extends DataSyncDisplay> T getDisplaySync(int index) {
        return (T) F.find(getSyncAggregator().getDisplays(), (i, item) -> {
            return i == index;
        }).map(m -> m.g2).orElse(null);
    }

    public <T extends PersistValidation> T getPersistenceValid(int index) {
        return (T) F.find(getSyncAggregator().getPersistValidations(), (i, item) -> {
            return i == index;
        }).map(m -> m.g2).orElse(null);
    }

    public <T extends DisplayValidation> T getDisplaySyncValid(int index) {
        return (T) F.find(getSyncAggregator().getDisplayValidations(), (i, item) -> {
            return i == index;
        }).map(m -> m.g2).orElse(null);
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
