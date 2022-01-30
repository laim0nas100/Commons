package lt.lb.commons.reflect;

import java.util.Date;

/**
 *
 * @author laim0nas100
 */
public class DefaultFieldFactory extends FieldFactory {

    public DefaultFieldFactory() {
        this.addImmutableType(FieldFactory.JVM_IMMUTABLE_TYPES);
        this.addExplicitClone(Date.class, (factory, date) -> new Date(date.getTime()));
    }

}
