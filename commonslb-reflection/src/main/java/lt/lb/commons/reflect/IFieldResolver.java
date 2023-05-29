package lt.lb.commons.reflect;

import java.util.IdentityHashMap;

/**
 *
 * @author laim0nas100
 */
public interface IFieldResolver {

    public void cloneField(Object source, Object parentObject, IdentityHashMap refCounter) throws Exception;

    public default IFieldResolver nest(IFieldResolver fr) {
        IFieldResolver me = this;
        return (Object source, Object parentObject, IdentityHashMap ref) -> {
            me.cloneField(source, parentObject, ref);
            fr.cloneField(source, parentObject, ref);
        };
    }

    public static IFieldResolver empty() {
        return (Object source, Object parentObject, IdentityHashMap refCoounter) -> {
        };
    }
}
