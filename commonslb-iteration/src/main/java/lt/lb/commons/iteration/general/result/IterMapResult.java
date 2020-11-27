package lt.lb.commons.iteration.general.result;

import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class IterMapResult<K, V> {

    public final V val;
    public final K key;

    public IterMapResult(K key, V val) {
        this.val = val;
        this.key = key;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.val);
        hash = 23 * hash + Objects.hashCode(this.key);
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
        return true;
    }

    @Override
    public String toString() {
        return key + ":" + val;
    }

}
