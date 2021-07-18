package lt.lb.commons.reflect.fields;

import java.lang.reflect.Modifier;

/**
 *
 * @author laim0nas100
 */
public interface IMethodModifierAware extends IBaseModifierAware {

    public default boolean isNative() {
        return Modifier.isNative(getModifiers());
    }

    public default boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }
}
