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
        if (idLocalHashCalled()) {
            IdHashStore idHashStore = idHashStore();
            if (idHashStore != null) {
                Object idLocal = idLocalHash();
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

    public boolean idLocalHashCalled();


    public default boolean idHashEquals(Object b) {
        if (b == null) {
            return false;
        }
        if (this == b) {
            return true;
        }
        if (b instanceof IdHash) {
            IdHash bHash = (IdHash) b;
            return Objects.equals(bHash.idHash(), this.idHash());
        }

        return false;
    }

    /**
     * Make sure this local id is not cached in the JVM, like ints form -128,127
     * or Strings. Easy workaround is to use {@link HashHolder}.
     *
     * @return
     */
    public default Object idLocalHash() {
        return HashHolder.of(System.identityHashCode(this));
    }

    public default Object idHash() {

        Object entityId = entityIdMaybeRegister();
        if (entityId == null) {
            return idLocalHash();
        }
        IdHashStore hasStore = idHashStore();
        if (hasStore == null) {
            return entityId;
        }
        Object mapping = hasStore.getMapping(entityId);
        if (mapping == null) {
            return entityId;
        }
        return mapping;
    }

    public IdHashStore idHashStore();
}
