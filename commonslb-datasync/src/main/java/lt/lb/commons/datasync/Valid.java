package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface Valid<M> {

        public boolean isValid(M from);

        public default boolean isInvalid(M from) {
            return !isValid(from);
        }

        public void showInvalidation(M from);

        public void clearInvalidation(M from);
    }
