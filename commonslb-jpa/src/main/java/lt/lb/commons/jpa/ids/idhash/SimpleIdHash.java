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
    public boolean __idLocalHashCalled() {
        return __localHashId != null;
    }

    @Override
    public Object __idLocalHash() {
        if (__localHashId == null) {
            __localHashId = IdHash.super.__idLocalHash();
        }
        return __localHashId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.__idHash());
    }

    @Override
    public boolean equals(Object obj) {
        return idHashEquals(obj);
    }

}
