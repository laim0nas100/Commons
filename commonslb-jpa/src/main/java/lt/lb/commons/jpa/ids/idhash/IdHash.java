package lt.lb.commons.jpa.ids.idhash;

import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public interface IdHash {

    public Object entityId();

    /**
     * Call this method after persisting. Only registers if local hash has been
     * called. If hashCode first time call was after entityID was assigned, then
     * no registration is needed.
     *
     * @return
     */
    public default boolean __tryRegisterLocalHashId() {
        Object entityId = entityId();
        if (entityId == null) {
            return false;
        }
        if (__idLocalHashCalled()) {
            IdHashStore idHashStore = __idHashStore();
            if (idHashStore != null) {
                Object idLocal = __idLocalHash();
                return idLocal == idHashStore.register(entityId, idLocal);
            }
        }
        return false;
    }

    public default Object entityIdMaybeRegister() {
        Object entityId = entityId();
        if (entityId == null) {
            return null;
        }
        __tryRegisterLocalHashId();
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

    /**
     * Make sure this local id is not cached in the JVM, like ints form -128,127
     * or Strings. Easy workaround is to use {@link HashHolder}.
     *
     * @return
     */
    public default Object __idLocalHash() {
        return HashHolder.of(System.identityHashCode(this));
    }

    public default Object __idHash() {

        Object entityId = entityIdMaybeRegister();
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
