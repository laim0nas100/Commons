package lt.lb.commons.misc.datetime;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 *
 * @author laim0nas100
 */
public class DateTimePatternParser extends DateTimeParser {

    public static Predicate<String> ofRegex(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return str -> pattern.matcher(str).find();
    }

    private final Predicate<String> applicable;

    public DateTimePatternParser(String dateFormat, Predicate<String> applicable) {
        super(dateFormat);
        this.applicable = Objects.requireNonNull(applicable);
    }

    public boolean parsable(String str) {
        if (str == null) {
            return false;
        }
        return applicable.test(str);
    }

}
