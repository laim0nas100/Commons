package lt.lb.commons.reflect.unified;

import java.lang.reflect.Modifier;

/**
 *
 * @author laim0nas100
 */
public interface IClassModifierAware extends IBaseModifierAware{

    public default boolean isStrict() {
        return Modifier.isStatic(getModifiers());
    }

    public default boolean isSynchronized() {
        return Modifier.isSynchronized(getModifiers());
    }

    public default boolean isInterface() {
        return Modifier.isInterface(getModifiers());
    }

    public default boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

}
