package lt.lb.commons.reflect.unified;

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
    
    public default boolean isNotPublic(){
        return !isPublic();
    }

    public default boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }
    
     public default boolean isNotProtected() {
        return !isProtected();
    }

    public default boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }
    
    public default boolean isNotPrivate(){
        return !isPrivate();
    }

    public default boolean isPackagePrivate() {
        return !isPrivate() && !isProtected() && !isPublic();
    }
    
    public default boolean isNotPackagePrivate(){
        return isPrivate() || isProtected() || isPublic();
    }

    public default boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }
    
    public default boolean isNotStatic(){
        return !isStatic();
    }

    public default boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }
    
    public default boolean isNotFinal(){
        return !isFinal();
    }

}
