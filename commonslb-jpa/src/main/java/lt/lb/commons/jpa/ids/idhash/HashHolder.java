package lt.lb.commons.jpa.ids.idhash;

import java.util.Objects;

/*
 * Helper class to use with any value so that it surely eventually will be
 * garbage collected when using alongside WeakReference.
 * @author laim0nas100
 */
public class HashHolder {

    public final Object val;

    public static HashHolder of(Object obj) {
        if (obj instanceof HashHolder) {
            return (HashHolder) obj;
        }
        return new HashHolder(obj);
    }

    private HashHolder(Object val) {
        this.val = val;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.val);
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
        final HashHolder other = (HashHolder) obj;
        return Objects.equals(this.val, other.val);
    }

}
