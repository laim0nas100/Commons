/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.interfaces;

/**
 *
 * @author laim0nas100
 */
public interface Modifier<Type> {

    public Type call(Type ob);

    public default Modifier<Type> chainNext(Modifier<Type> next) {
        return (Type object) -> {
            return next.call(this.call(object));
        };
    }

    public default Modifier<Type> chainPrev(Modifier<Type> prev) {
        return (Type object) -> {
            return this.call(prev.call(object));
        };
    }

    public static interface Modifiable<ObjectType> {

        public ObjectType apply(Modifier<ObjectType> mod);
    }
}
