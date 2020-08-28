package lt.lb.commons.datasync;


/**
 *
 * @author laim0nas100
 */
public interface PersistValidation<M, V extends Valid<M>> {

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

        public boolean isValidPersist(M from);

        public default boolean isInvalidPersist(M from) {
            return !isValidPersist(from);
        }

        public void clearInvalidationPersist(M from);

    }
