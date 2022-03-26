package lt.lb.commons.reflect.unified;

import java.lang.reflect.Modifier;

/**
 *
 * @author laim0nas100
 */
public interface IFieldModifierAware extends IBaseModifierAware {

    public default boolean isVolatile() {
        return Modifier.isVolatile(getModifiers());
    }

    public default boolean isTransient() {
        return Modifier.isTransient(getModifiers());
    }
}
