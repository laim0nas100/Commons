package lt.lb.commons.threads.sync;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Convenient class to group TimeUnit and long for concurrent waiting.
 *
 * @author laim0nas100
 */
public class WaitTime {

    public final long time;
    public final TimeUnit unit;

    public WaitTime(long time, TimeUnit unit) {
        Objects.requireNonNull(time);
        Objects.requireNonNull(unit);
        if (time < 0) {
            throw new IllegalArgumentException("Time should not be negative");
        }
        this.time = time;
        this.unit = unit;
    }

    public WaitTime(Duration dur) {
        Objects.requireNonNull(dur);
        if(dur.getNano() == 0){
           this.time = dur.getSeconds();
           this.unit = TimeUnit.SECONDS;
        }else{
            this.time = dur.toNanos();
            this.unit = TimeUnit.NANOSECONDS;
        }
    }

    public Duration toDuration() {
        return Duration.of(time, toChronoUnit(unit));
    }

    public WaitTime convert(TimeUnit unit) {
        return WaitTime.convert(this, unit);
    }

    public WaitTime add(WaitTime other) {
        return WaitTime.add(this, other);
    }

    public WaitTime subtract(WaitTime other) {
        return WaitTime.subtract(this, other);
    }

    public static WaitTime of(long time, TimeUnit unit) {
        return new WaitTime(time, unit);
    }

    public static WaitTime ofNanos(long time) {
        return new WaitTime(time, TimeUnit.NANOSECONDS);
    }

    public static WaitTime ofMicros(long time) {
        return new WaitTime(time, TimeUnit.MICROSECONDS);
    }

    public static WaitTime ofMillis(long time) {
        return new WaitTime(time, TimeUnit.MILLISECONDS);
    }

    public static WaitTime ofSeconds(long time) {
        return new WaitTime(time, TimeUnit.SECONDS);
    }

    public static WaitTime ofMinutes(long time) {
        return new WaitTime(time, TimeUnit.MINUTES);
    }

    public static WaitTime ofHours(long time) {
        return new WaitTime(time, TimeUnit.HOURS);
    }

    public static WaitTime ofDays(long time) {
        return new WaitTime(time, TimeUnit.DAYS);
    }

    public static WaitTime convert(WaitTime current, TimeUnit unit) {
        return new WaitTime(unit.convert(current.time, current.unit), unit);
    }

    /**
     * Returns unit type order in ascending fashion from 0 to 6.
     *
     * @param tu
     * @return
     */
    public static int unitOrder(TimeUnit tu) {

        Objects.requireNonNull(tu);
        // TimeUnit.ordinal() might work, but order of definition might be different in other JVM's
        switch (tu) {
            case NANOSECONDS:
                return 0;
            case MICROSECONDS:
                return 1;
            case MILLISECONDS:
                return 2;
            case SECONDS:
                return 3;
            case MINUTES:
                return 4;
            case HOURS:
                return 5;
            case DAYS:
                return 6;
            default:
                throw new IllegalArgumentException("Unrecognized time unit " + tu);
        }
    }

    public static WaitTime add(WaitTime a, WaitTime b) {
        int aOrder = unitOrder(a.unit);
        int bOrder = unitOrder(b.unit);
        long sum = 0;
        TimeUnit unit = a.unit;
        if (aOrder == bOrder) {
            sum = a.time + b.time;
        } else if (aOrder > bOrder) {
            sum = b.unit.convert(a.time, a.unit) + b.time;
            unit = b.unit;
        } else {
            sum = a.unit.convert(b.time, b.unit) + a.time;
        }
        return new WaitTime(sum, unit);

    }

    public static WaitTime subtract(WaitTime a, WaitTime b) {
        int aOrder = unitOrder(a.unit);
        int bOrder = unitOrder(b.unit);
        long sum = 0;
        TimeUnit unit = a.unit;
        if (aOrder == bOrder) {
            sum = a.time - b.time;
        } else if (aOrder > bOrder) {
            sum = b.unit.convert(a.time, a.unit) - b.time;
            unit = b.unit;
        } else {
            sum = a.time - a.unit.convert(b.time, b.unit);
        }
        return new WaitTime(sum, unit);
    }

    public static ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        switch (Objects.requireNonNull(timeUnit, "timeUnit")) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new AssertionError();
        }
    }

    public static TimeUnit toTimeUnit(ChronoUnit chronoUnit) {
        switch (Objects.requireNonNull(chronoUnit, "chronoUnit")) {
            case NANOS:
                return TimeUnit.NANOSECONDS;
            case MICROS:
                return TimeUnit.MICROSECONDS;
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                return TimeUnit.SECONDS;
            case MINUTES:
                return TimeUnit.MINUTES;
            case HOURS:
                return TimeUnit.HOURS;
            case DAYS:
                return TimeUnit.DAYS;
            default:
                throw new IllegalArgumentException(
                        "No TimeUnit equivalent for " + chronoUnit);
        }
    }
    
    public static boolean canConvertToTimeUnit(ChronoUnit chronoUnit) {
        switch (Objects.requireNonNull(chronoUnit, "chronoUnit")) {
            case NANOS:
            case MICROS:
            case MILLIS:
            case SECONDS:
            case MINUTES:
            case HOURS:
            case DAYS:
                return true;
            default:
                return false;
        }
    }

}
