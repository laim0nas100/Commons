package lt.lb.commons.refmodel;

import java.util.List;
import lt.lb.commons.F;
import lt.lb.commons.clone.CloneSupport;

/**
 *
 * @author laim0nas100
 */
public class Ref<Type> implements CloneSupport<Ref<Type>> {

    protected String local;
    protected String relative;
    protected Class[] parameterTypes = {};
    protected Ref memberContinuation;
    protected RefNotation notation;
    protected int compileLeft;

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public String get() {
        return getRelative();
    }

    public String getRelative() {
        return relative;
    }

    public String getLocal() {
        return local;
    }

    public List<String> steps() {
        return notation.steps(getRelative());
    }

    public RefNotation getNotation() {
        return notation;
    }

    public Ref getMemberContinuation() {
        return memberContinuation;
    }

    public Ref() {
    }

    @Override
    public String toString() {
        return getRelative();
    }

    @Override
    public Ref clone() {
        try {
            return F.cast(super.clone());
        } catch (CloneNotSupportedException ex) {
            return this;
        }

    }
}
