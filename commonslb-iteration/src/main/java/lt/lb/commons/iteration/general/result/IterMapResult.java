package lt.lb.commons.iteration.general.result;

import java.util.Objects;

/**
 * Immutable
 *
 * @author laim0nas100
 */
public class IterMapResult<K, V> extends IterIterableResult<V> {

    public final K key;

    public IterMapResult(int index, K key, V val) {
        super(index, val);
        this.key = key;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.val);
        hash = 41 * hash + Objects.hashCode(this.key);
        hash = 41 * hash + Objects.hashCode(this.index);
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
        final IterMapResult<?, ?> other = (IterMapResult<?, ?>) obj;
        if (!Objects.equals(this.val, other.val)) {
            return false;
        }
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.index, other.index)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return index + ":" + key + ":" + val;
    }

}
