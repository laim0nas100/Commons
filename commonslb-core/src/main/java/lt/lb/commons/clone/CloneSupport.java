package lt.lb.commons.clone;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Interface for cloning, explicitly declaring clone method publicly.
 *
 * @author laim0nas100
 * @param <T>
 */
@FunctionalInterface
public interface CloneSupport<T> extends Cloneable {

    /**
     * Explicit public method for cloning
     *
     * @return
     * @throws CloneNotSupportedException
     */
    public T clone() throws CloneNotSupportedException;

    /**
     * Explicit public method for cloning with optional {@link Cloner}, default method
     * just delegates to {@link CloneSupport#clone() }. Override this for
     * customized cloning using provided or your own {@link Cloner}
     *
     * @param cloner
     * @return
     * @throws CloneNotSupportedException
     */
    public default T clone(Cloner cloner) throws CloneNotSupportedException {
        return clone();
    }

    /**
     * Explicit public method for cloning, masking
     * {@link CloneNotSupportedException} in
     * {@link UnsupportedOperationException}
     *
     * @return
     */
    public default T uncheckedClone() throws UnsupportedOperationException {
        try {
            return clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    /**
     * Explicit public method for cloning, masking
     * {@link CloneNotSupportedException} in
     * {@link UnsupportedOperationException} with optional {@link Cloner}, default
     * method just delegates to {@link CloneSupport#clone(lt.lb.commons.clone.Cloner)
     * }
     *
     * @param cloner
     * @return
     */
    public default T uncheckedClone(Cloner cloner) throws UnsupportedOperationException {
        try {
            return clone(cloner);
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

}
