package lt.lb.commons.reflect.fields;

import java.lang.reflect.Modifier;

/**
 *
 * @author laim0nas100
 */
public interface IBaseModifierAware {

    public int getModifiers();

    public default boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    public default boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    public default boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    public default boolean isPackagePrivate() {
        return !isPrivate() && !isProtected() && !isPublic();
    }

    public default boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public default boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

}
