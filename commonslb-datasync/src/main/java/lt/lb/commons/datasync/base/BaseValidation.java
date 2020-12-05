package lt.lb.commons.datasync.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lt.lb.commons.containers.values.BooleanValue;
import lt.lb.commons.datasync.PersistAndDisplayValidation;
import lt.lb.commons.datasync.Valid;
import lt.lb.commons.iteration.For;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseValidation<M, V extends Valid<M>> implements PersistAndDisplayValidation<M, V> {

    protected List<V> validateDisplay = new ArrayList<>(0);
    protected List<V> validatePersistence = new ArrayList<>(0);

    public abstract M getManaged();

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
        return doValidation(validateDisplay, false, getManaged());
    }

    @Override
    public boolean validDisplayFull() {
        return doValidation(validateDisplay, true, getManaged());
    }

    @Override
    public boolean validPersist() {
        return doValidation(validatePersistence, false, getManaged());
    }

    @Override
    public boolean validPersistFull() {
        return doValidation(validatePersistence, true, getManaged());
    }

    @Override
    public boolean isValidDisplay(M from) {
        return checkValidation(validateDisplay, from);
    }

    @Override
    public void clearInvalidationDisplay(M from) {
        clearValidation(validateDisplay, from);
    }

    @Override
    public boolean isValidPersist(M from) {
        return checkValidation(validatePersistence, from);
    }

    @Override
    public void clearInvalidationPersist(M from) {
        clearValidation(validatePersistence, from);
    }

    public static <T> boolean iterateFindFirst(Iterable<T> list, boolean full, Predicate<T> satisfied) {

        if (full) {
            BooleanValue found = BooleanValue.FALSE();
            For.elements().iterate(list, (i, item) -> found.setOr(satisfied.test(item)));
            return found.get();
        } else {
            return For.elements().find(list, (i, item) -> satisfied.test(item)).isPresent();
        }
    }

    public static <T extends Valid<M>, M> void clearValidation(List<T> list, M managed) {
        For.elements().iterate(list, (i, c) -> c.clearInvalidation(managed));
    }

    public static <T extends Valid<M>, M> boolean checkValidation(List<T> list, M managed) {
        return !For.elements().find(list, (i, c) -> c.isInvalid(managed)).isPresent();//find first invalid
    }

    public static <T extends Valid<M>, M> boolean doValidation(List<T> list, boolean full, M managed) {
        boolean invalid = iterateFindFirst(list, full, val -> {
            val.clearInvalidation(managed);
            if (val.isInvalid(managed)) {
                val.showInvalidation(managed);
                return true;
            }
            return false;

        });
        return !invalid;
    }

}
