/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

/**
 *
 * @author laim0nas100
 */
public interface IFieldResolver {

    public void cloneField(Object source, Object parentObject, ReferenceCounter refCoounter) throws Exception;

    public default IFieldResolver nest(IFieldResolver fr) {
        IFieldResolver me = this;
        return (Object source, Object parentObject, ReferenceCounter ref) -> {
            me.cloneField(source, parentObject, ref);
            fr.cloneField(source, parentObject, ref);
        };
    }

    public static IFieldResolver empty() {
        return (Object source, Object parentObject, ReferenceCounter refCoounter) -> {
        };
    }
}
