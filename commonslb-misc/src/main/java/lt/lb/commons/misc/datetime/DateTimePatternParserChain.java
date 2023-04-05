package lt.lb.commons.misc.datetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author laim0nas100
 */
public class DateTimePatternParserChain {

    private final DateTimePatternParser parser;
    private final String str;

    public DateTimePatternParserChain(DateTimePatternParser parser, String str) {
        this.parser = parser;
        this.str = str;
    }

    public LocalDateTime toLocalDateTime() {
        return parser.toLocalDateTime(str);
    }

    public LocalDate toLocalDate() {
        return parser.toLocalDate(str);
    }

    public ZonedDateTime toZonedDateTime() {
        return parser.toZonedDateTime(str);
    }

    public ZonedDateTime toZonedDateTimeDefaultZone() {
        return parser.toZonedDateTimeDefaultZone(str);
    }

    public ZonedDateTime toZonedDateTime(ZoneId id) {
        return parser.toZonedDateTime(str);
    }

    public Date toDate() {
        return parser.toDate(str);
    }

    public Calendar toCalendar() {
        return parser.toCalendar(str);
    }

    public long toMillis() {
        return parser.toMillis(str);
    }

}
