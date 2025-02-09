package lt.lb.commons.jpa.ids.idhash;

import java.util.Objects;

/**
 *
 * Minimal implementation with one field to hold local hash.
 * @author laim0nas100
 */
public abstract class SimpleIdHash implements IdHash {

    protected transient Object __localHashId;

    @Override
    public boolean idLocalHashCalled() {
        return __localHashId != null;
    }

    @Override
    public Object idLocalHash() {
        if (__localHashId == null) {
            __localHashId = IdHash.super.idLocalHash();
        }
        return __localHashId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idHash());
    }

    @Override
    public boolean equals(Object obj) {
        return idHashEquals(obj);
    
    }

    /**
     * The table id mapping
     * @return 
     */
    @Override
    public abstract Object entityId();

    /**
     * Table id and local id mapper
     * @return 
     */
    @Override
    public abstract IdHashStore idHashStore();
    

}
