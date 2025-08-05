package lt.lb.commons.reflect.unified;

import java.util.regex.Pattern;

/**
 *
 * @author laim0nas100
 */
public interface INamed {

    public String getName();

    public default boolean nameIs(String str) {
        return getName().equals(str);
    }

    public default boolean nameMatches(Pattern pattern) {
        return pattern.matcher(getName()).matches();
    }

    public default boolean nameStartsWith(String str) {
        return getName().startsWith(str);
    }

    public default boolean nameStartsWithIgnoreCase(String str) {
        return getName().toUpperCase().startsWith(str.toUpperCase());
    }

    public default boolean nameEndsWith(String str) {
        return getName().endsWith(str);
    }

    public default boolean nameEndsWithIgnoreCase(String str) {
        return getName().toUpperCase().endsWith(str.toUpperCase());
    }

    public default boolean nameContains(String str) {
        return getName().contains(str);
    }

    public default boolean nameContainsIgnoreCase(String str) {
        return getName().toUpperCase().contains(str.toUpperCase());
    }
}
