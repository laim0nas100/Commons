package lt.lb.commons.reflect.beans;

import java.util.Locale;

/**
 *
 * @author laim0nas100
 */
public class NameUtil {

    /**
     * Returns a String which capitalizes the first letter of the string.
     */
    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
    }
}
