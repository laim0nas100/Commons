package lt.lb.commons.iteration.general.result;

import java.util.Objects;

/**
 *
 * Immutable
 *
 * @author laim0nas100
 */
public class IterIterableResult<T> {

    public final T val;
    public final int index;

    public IterIterableResult(int index, T val) {
        this.val = val;
        this.index = index;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.val);
        hash = 47 * hash + this.index;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IterIterableResult<?> other = (IterIterableResult<?>) obj;
        if (this.index != other.index) {
            return false;
        }
        if (!Objects.equals(this.val, other.val)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return index + ":" + val;
    }

}
