package lt.lb.commons.rows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.DataSyncs;
import lt.lb.commons.DataSyncs.ExplicitDataSync;
import lt.lb.commons.DataSyncs.SyncValidation;
import lt.lb.commons.DataSyncs.Valid;

/**
 *
 * @author laim0nas100
 */
public abstract class SyncDrow<C, N, L, U extends Updates<U>, Conf extends SyncDrowConf<R, C, N, L, U>, R extends SyncDrow> extends Drow<C, N, L, U, Conf, R> {

    protected List<ExplicitDataSync> syncs = new ArrayList<>();
    protected List<SyncValidation> syncValidations = new ArrayList<>();
    protected SyncValidation unmanagedValidation = new DataSyncs.UnmanagedValidation();

    public SyncDrow(L line, Conf config, String key) {
        super(line, config, key);
    }

    public R addSync(ExplicitDataSync sync) {
        syncs.add(sync);
        return me();
    }

    public R addSyncValidation(SyncValidation syncVal) {
        addOnDisplayAndRunIfDone(() -> {
            syncValidations.add(syncVal);
        });

        return me();

    }

    public R addValidationPersist(Valid valid) {
        addOnDisplayAndRunIfDone(() -> {
            unmanagedValidation.withPersistValidation(valid);
        });

        return me();
    }

    public R addValidationDisplay(Valid valid) {
        addOnDisplayAndRunIfDone(() -> {
            unmanagedValidation.withDisplayValidation(valid);
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

    public <M, V extends Valid<M>> R addDisplayValidation(V valid, Supplier<? extends M> managed) {
        addOnDisplayAndRunIfDone(() -> {
            addSyncValidation(config.createSyncValidationDisplay(valid, managed));
        });
        return me();
    }

    public <M, V extends Valid<M>> R addPersistValidation(V valid, Supplier<? extends M> managed) {
        addOnDisplayAndRunIfDone(() -> {
            addSyncValidation(config.createSyncValidationPersist(valid, managed));
        });
        return me();
    }

    public void syncManagedFromDisplay() {
        for (ExplicitDataSync sync : syncs) {
            sync.syncManagedFromDisplay();
        }
    }

    public void syncManagedFromPersist() {
        for (ExplicitDataSync sync : syncs) {
            sync.syncManagedFromPersist();
        }
    }

    public void syncDisplay() {
        for (ExplicitDataSync sync : syncs) {
            sync.syncDisplay();
        }
    }

    public void syncPersist() {
        for (ExplicitDataSync sync : syncs) {
            sync.syncPersist();
        }
    }

    public boolean invalidPersist(boolean full) {
        boolean invalid = full ? unmanagedValidation.invalidPersistFull() : unmanagedValidation.invalidPersist();
        if (invalid && full) {
            return invalid;
        }

        for (ExplicitDataSync sync : syncs) {

            if (full) {
                invalid = invalid || sync.invalidPersistFull();
                if (invalid) {
                    return invalid;
                }
            } else {
                invalid = invalid || sync.invalidPersist();
            }
        }
        for (SyncValidation validation : syncValidations) {
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
        boolean invalid = full ? unmanagedValidation.invalidDisplayFull() : unmanagedValidation.invalidDisplay();
        if (invalid && full) {
            return invalid;
        }
        for (ExplicitDataSync sync : syncs) {
            if (full) {
                invalid = invalid || sync.invalidDisplayFull();
                if (invalid) {
                    return invalid;
                }
            } else {
                invalid = invalid || sync.invalidDisplay();
            }
        }
        for (SyncValidation validation : syncValidations) {
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
