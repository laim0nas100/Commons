package lt.lb.commons.jpa.ids.idhash;

import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public interface IdHash {

    public Object entityId();

    public default Object __entityIdMaybeMap() {
        Object entityId = entityId();
        if (entityId == null) {
            return null;
        }
        if (__idLocalHashCalled()) {
            IdHashStore idHashStore = __idHashStore();
            if (idHashStore != null) {
                Object idLocal = __idLocalHash();
                idHashStore.register(entityId, idLocal);
            }
        }
        return entityId;
    }

    public boolean __idLocalHashCalled();

    public default int idHashCode() {
        return Objects.hashCode(__idHash());
    }

    public default boolean idHashEquals(Object b) {
        if (b == null) {
            return false;
        }
        if (this == b) {
            return true;
        }
        Class ca = this.getClass();
        Class cb = b.getClass();
        if (!ca.isAssignableFrom(cb) && !cb.isAssignableFrom(ca)) { // bad types
            return false;
        }
        if (b instanceof IdHash) {
            IdHash bHash = (IdHash) b;
            return Objects.equals(bHash.__idHash(), this.__idHash());
        }

        return false;
    }

    public default Object __idLocalHash() {
        IdHashStore hashStore = __idHashStore();
        if (hashStore == null) {
            return System.identityHashCode(this);
        } else {
            return hashStore.getLocalHash(this);
        }
    }

    public default Object __idHash() {

        Object entityId = __entityIdMaybeMap();
        if (entityId == null) {
            return __idLocalHash();
        }
        IdHashStore hasStore = __idHashStore();
        if (hasStore == null) {
            return entityId;
        }
        Object mapping = hasStore.getMapping(entityId);
        if (mapping == null) {
            return entityId;
        }
        return mapping;
    }

    public IdHashStore __idHashStore();
}
