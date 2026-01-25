package lt.lb.commons.refmodel;

import lt.lb.commons.F;
import lt.lb.commons.clone.CloneSupport;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class Ref<Type> implements CloneSupport<Ref<Type>> {

    String local;
    String relative;
    Class[] parameterTypes;
    Ref memberContinuation;
    String separator;
    int compileLeft;

    public final Class[] getParameterTypes() {
        return parameterTypes;
    }

    public final String get() {
        return getRelative();
    }

    public final String getRelative() {
        return relative;
    }

    public final String getLocal() {
        return local;
    }

    public final String[] steps() {
        return StringUtils.split(getRelative(), separator);
    }

    public final String getSeparator() {
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
