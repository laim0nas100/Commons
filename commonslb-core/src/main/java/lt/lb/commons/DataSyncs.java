package lt.lb.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.containers.values.SetOnce;

/**
 *
 * @author laim0nas100
 */
public class DataSyncs {

    public static interface Valid<F> {

        public boolean isValid(F from);

        public default boolean isInvalid(F from) {
            return !isValid(from);
        }

        public void displayInvalidation();

        public void clearInvalidation();
    }

    public static interface SyncValidation<M, V extends Valid<M>>
            extends DisplayValildation<M, V>, PersistValidation<M, V> {

    }

    public static interface DisplayValildation<M, V extends Valid<M>>{

        public void withDisplayValidation(V validation);

        /**
         * If managed value can be displayed
         *
         * @return
         */
        public boolean validDisplay();

        /**
         * If managed value can be displayed, fire every validation
         *
         * @return
         */
        public boolean validDisplayFull();

        public default boolean invalidDisplay() {
            return !validDisplay();
        }

        public default boolean invalidDisplayFull() {
            return !validDisplayFull();
        }

    }

    public static interface PersistValidation<M, V extends Valid<M>> {

        public void withPersistValidation(V validation);

        /**
         * If managed value can be persisted
         *
         * @return
         */
        public boolean validPersist();

        /**
         * If managed value can be persisted, fire every validation
         *
         * @return
         */
        public boolean validPersistFull();

        public default boolean invalidPersist() {
            return !validPersist();
        }

        public default boolean invalidPersistFull() {
            return !validPersistFull();
        }

    }

    public static interface DataSyncManaged<P, M, D> {

        public void withDisplaySync(Consumer<? super D> displaySync);

        public void withPersistSync(Consumer<? super P> persSync);

        public void withDiplaySup(Supplier<? extends D> displaySup);

        public void withPersisSup(Supplier<? extends P> persistSup);

        public void withPersistGet(Function<? super P, ? extends M> func);

        public void withDisplayGet(Function<? super D, ? extends M> func);

        public void withPersistSet(Function<? super M, ? extends P> func);

        public void withDisplaySet(Function<? super M, ? extends D> func);

        public void setManaged(M managed);

        public M getManaged();

        public void syncPersist();

        public void syncDisplay();

        public void syncManagedFromDisplay();

        public void syncManagedFromPersist();

    }

    public static interface DataSync<P, D> extends DataSyncManaged<P, P, D> {

    }

    public static interface DataSyncManagedValidation<P, M, D, V extends Valid<M>> extends DataSyncManaged<P, M, D>, SyncValidation<M, V> {

    }

    public static interface DataSyncValidation<P, D, V extends Valid<P>> extends DataSync<P, D>, SyncValidation<P, V> {

    }

    /*
    BaseBuilder<ID, F, T, E extends BaseBuilder<ID, F, T, E>> implements Builder<ID, F, T, E>
     */
    public static abstract class ExplicitDataSync<P, M, D, V extends Valid<M>> implements DataSyncManagedValidation<P, M, D, V> {

        protected SetOnce<Supplier<? extends D>> displaySupp = new SetOnce<>();
        protected SetOnce<Consumer<? super D>> displaySync = new SetOnce<>();
        protected SetOnce<Supplier<? extends P>> persistenceSupp = new SetOnce<>();
        protected SetOnce<Consumer<? super P>> persistenceSync = new SetOnce<>();
        protected SetOnce<Function<? super P, ? extends M>> persistGet = new SetOnce<>();
        protected SetOnce<Function<? super M, ? extends P>> persistSet = new SetOnce<>();
        protected SetOnce<Function<? super D, ? extends M>> displayGet = new SetOnce<>();
        protected SetOnce<Function<? super M, ? extends D>> displaySet = new SetOnce<>();

        protected List<V> validateDisplay = new ArrayList<>();
        protected List<V> validatePersistence = new ArrayList<>();

        protected M managed;

        protected abstract ExplicitDataSync<P, M, D, V> me();

        @Override
        public void withDisplaySync(Consumer<? super D> displaySync) {
            this.displaySync.set(displaySync);
        }

        @Override
        public void withPersistSync(Consumer<? super P> persSync) {
            this.persistenceSync.set(persSync);
        }

        @Override
        public void withDiplaySup(Supplier<? extends D> displaySup) {
            this.displaySupp.set(displaySup);
        }

        @Override
        public void withPersisSup(Supplier<? extends P> persistSup) {
            this.persistenceSupp.set(persistSup);
        }

        @Override
        public void withPersistGet(Function<? super P, ? extends M> func) {
            this.persistGet.set(func);
        }

        @Override
        public void withDisplayGet(Function<? super D, ? extends M> func) {

            this.displayGet.set(func);
        }

        @Override
        public void withPersistSet(Function<? super M, ? extends P> func) {
            this.persistSet.set(func);
        }

        @Override
        public void withDisplaySet(Function<? super M, ? extends D> func) {
            this.displaySet.set(func);
        }

        @Override
        public void setManaged(M managed) {
            this.managed = managed;
        }

        @Override
        public M getManaged() {
            return managed;
        }

        @Override
        public void syncPersist() {
            if (this.persistSet.isNotNull() && this.persistenceSync.isNotNull()) {
                Function<? super M, ? extends P> toPersist = this.persistSet.get();
                P newPersist = toPersist.apply(this.getManaged());
                this.persistenceSync.get().accept(newPersist);
            } else {
                //explicitly launch to throw exceptions
                this.persistSet.get();
                this.persistenceSync.get();
            }
        }

        @Override
        public void syncDisplay() {
            if (this.displaySet.isNotNull() && this.displaySync.isNotNull()) {
                Function<? super M, ? extends D> toDisplay = this.displaySet.get();
                D newDisplay = toDisplay.apply(this.getManaged());
                this.displaySync.get().accept(newDisplay);
            } else {
                //explicitly launch to throw exceptions
                this.displaySet.get();
                this.displaySync.get();
            }
        }

        @Override
        public void syncManagedFromDisplay() {
            if (this.displayGet.isNotNull() && this.displaySupp.isNotNull()) {
                Supplier<? extends D> get = this.displaySupp.get();
                D display = get.get();

                Function<? super D, ? extends M> toManaged = this.displayGet.get();
                M newManaged = toManaged.apply(display);
                this.setManaged(newManaged);

            } else {
                //explicitly launch to throw exceptions
                this.displayGet.get();
                this.displaySupp.get();
            }
        }

        @Override
        public void syncManagedFromPersist() {
            if (this.persistGet.isNotNull() && this.persistenceSupp.isNotNull()) {
                Supplier<? extends P> get = this.persistenceSupp.get();
                P persist = get.get();

                Function<? super P, ? extends M> toManaged = this.persistGet.get();
                M newManaged = toManaged.apply(persist);
                this.setManaged(newManaged);
            } else {
                //explicitly launch to throw exceptions
                persistGet.get();
                persistenceSupp.get();
            }
        }

        @Override
        public void withDisplayValidation(V validation) {
            this.validateDisplay.add(validation);
        }

        @Override
        public void withPersistValidation(V validation) {
            this.validatePersistence.add(validation);
        }

        @Override
        public boolean validDisplay() {
            return doValidation(validateDisplay, false);
        }

        @Override
        public boolean validDisplayFull() {
            return doValidation(validateDisplay, true);
        }

        @Override
        public boolean validPersist() {
            return doValidation(validatePersistence, false);
        }

        @Override
        public boolean validPersistFull() {
            return doValidation(validatePersistence, true);
        }

        protected boolean doValidation(List<V> list, boolean full) {
            boolean valid = true;
            for (V val : list) {
                val.clearInvalidation();
                if (val.isInvalid(managed)) {
                    valid = false;
                    val.displayInvalidation();
                    if (!full) {
                        return valid;
                    }
                }
            }
            return valid;
        }

    }

    public static abstract class GenDataSync<P, D, V extends Valid<P>> extends ExplicitDataSync<P, P, D, V> {

        public GenDataSync() {
            persistGet.set(v -> v);
            persistSet.set(v -> v);
        }

    }

}
