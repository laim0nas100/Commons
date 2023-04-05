package lt.lb.commons.misc.datetime;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 *
 * @author laim0nas100
 */
public class DateTimeParser {

    private final FastDateFormat format;
    private final DateTimeFormatter formatter;

    public DateTimeParser(String dateFormat) {
        Objects.requireNonNull(dateFormat);
        format = FastDateFormat.getInstance(dateFormat);
        formatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    public FastDateFormat getFormat() {
        return format;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public String format(ZonedDateTime time) {
        return time.format(getFormatter());
    }

    public String format(LocalDateTime time) {
        return time.format(getFormatter());
    }

    public String format(LocalDate time) {
        return time.format(getFormatter());
    }

    public String format(Calendar time) {
        return getFormat().format(time);
    }

    public String format(Date date) {
        return getFormat().format(date);
    }

    public String format(long millis) {
        return getFormat().format(millis);
    }

    public LocalDateTime toLocalDateTime(String str) {
        return LocalDateTime.parse(str, getFormatter());
    }

    public LocalDate toLocalDate(String str) {
        return LocalDate.parse(str, getFormatter());
    }

    public ZonedDateTime toZonedDateTime(String str) {
        return ZonedDateTime.parse(str, getFormatter());
    }

    public ZonedDateTime toZonedDateTimeDefaultZone(String str) {
        return toZonedDateTime(str, ZoneId.systemDefault());
    }

    public ZonedDateTime toZonedDateTime(String str, ZoneId id) {
        return LocalDateTime.parse(str, getFormatter()).atZone(id);
    }

    public Date toDate(String str) {
        try {
            return getFormat().parse(str);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public Calendar toCalendar(String str) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(toDate(str));
        return instance;
    }

    public long toMillis(String str) {
        return toDate(str).getTime();
    }
}
