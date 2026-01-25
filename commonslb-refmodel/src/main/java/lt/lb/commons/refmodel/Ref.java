package lt.lb.commons.refmodel;

import lt.lb.commons.F;
import lt.lb.commons.clone.CloneSupport;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class Ref<Type> implements CloneSupport<Ref<Type>> {

    protected String local;
    protected String relative;
    protected Class[] parameterTypes = {};
    protected Ref memberContinuation;
    protected String separator;
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

    public String[] steps() {
        return StringUtils.split(getRelative(), getSeparator());
    }

    public String getSeparator() {
        return separator;
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
