package lt.lb.commons.misc.datetime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static lt.lb.commons.misc.datetime.DateTimePatternParser.ofRegex;

/**
 *
 * @author laim0nas100
 */
public class FlexibleDateFormat {

    public static final FlexibleDateFormat commonDateParsers() {
        return new FlexibleDateFormat(Arrays.asList(
                new DateTimePatternParser("yyyy-MM-dd", ofRegex("[0-9]{4}\\-[0-9]{1,2}\\-[0-9]{1,2}")),
                new DateTimePatternParser("yyyy.MM.dd", ofRegex("[0-9]{4}\\.[0-9]{1,2}\\.[0-9]{1,2}")),
                new DateTimePatternParser("yyyy/MM/dd", ofRegex("[0-9]{4}\\/[0-9]{1,2}\\/[0-9]{1,2}")),
                new DateTimePatternParser("MM-dd-yyyy", ofRegex("[0-9]{1,2}\\-[0-9]{1,2}\\-[0-9]{4}")),
                new DateTimePatternParser("MM.dd.yyyy", ofRegex("[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{4}")),
                new DateTimePatternParser("MM/dd/yyyy", ofRegex("[0-9]{1,2}\\/[0-9]{1,2}\\/[0-9]{4}"))
        ));
    }

    public FlexibleDateFormat(List<DateTimePatternParser> parsers) {
        this.parsers = Collections.unmodifiableList(parsers);
    }

    public List<DateTimePatternParser> getParsers() {
        return parsers;
    }

    private ThreadLocal<DateTimePatternParser> cachedParser = new ThreadLocal<>();
    private final List<DateTimePatternParser> parsers;

    public DateTimePatternParser parse(String str) {
        DateTimePatternParser cached = cachedParser.get();
        if (cached != null) {
            if (cached.parsable(str)) {
                return cached;
            }
        } else {
            for (DateTimePatternParser parser : parsers) {
                if (parser.parsable(str)) {
                    cachedParser.set(parser);
                    return parser;
                }
            }
        }
        throw new IllegalArgumentException("Failed to find applicable date parser for str:" + str);
    }

    public DateTimePatternParserChain given(String str) {
        return new DateTimePatternParserChain(parse(str), str);
    }

}
