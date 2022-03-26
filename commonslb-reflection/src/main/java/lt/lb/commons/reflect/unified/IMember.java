package lt.lb.commons.reflect.unified;

import java.lang.reflect.Member;

/**
 *
 * @author laim0nas100
 */
public interface IMember extends Member {

    public default boolean nameIs(String str) {
        return getName().equals(str);
    }

    public default boolean nameStartsWith(String str) {
        return getName().startsWith(str);
    }

    public default boolean nameContains(String str) {
        return getName().contains(str);
    }

}
