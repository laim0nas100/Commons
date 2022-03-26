package lt.lb.commons.reflect.unified;

import java.lang.reflect.Modifier;

/**
 *
 * @author laim0nas100
 */
public interface IMethodModifierAware extends IBaseModifierAware, IAccessible {

    public default boolean isNative() {
        return Modifier.isNative(getModifiers());
    }

    public default boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    public boolean isDefault();
    
    public boolean isVarArgs();

}
