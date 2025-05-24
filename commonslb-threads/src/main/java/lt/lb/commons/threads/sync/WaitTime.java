package lt.lb.commons.threads.sync;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Convenient class to group TimeUnit and long for concurrent waiting. Similar
 * to Duration, but support conversion overflow checking and only allows
 * non-negative values.
 *
 * @author laim0nas100
 */
public class WaitTime implements Comparable<WaitTime> {

    public static final WaitTime MAX_NANOS = WaitTime.ofNanos(Long.MAX_VALUE);

    public final long time;
    public final TimeUnit unit;

    public WaitTime(long time, TimeUnit unit) {
        if (time < 0) {
            throw new IllegalArgumentException("Time should not be negative");
        }
        this.time = time;
        this.unit = Objects.requireNonNull(unit);
    }

    /**
     * Converts negative to positive. Tries to find biggest suitable TimeUnit
     * (up to seconds) to fit duration with nano offset
     *
     * @param dur
     */
    public WaitTime(Duration dur) {
        Objects.requireNonNull(dur);
        long s = dur.getSeconds();
        int n = dur.getNano();
        if (n == 0) {// no nanos, just use seconds
            this.time = Math.abs(s);
            this.unit = TimeUnit.SECONDS;
        } else {
            // try to find the biggest unit based on nano offset
            TimeUnit tu = TimeUnit.NANOSECONDS;
            if (n % 1000_000_000L == 0L) {//seconds
                tu = TimeUnit.SECONDS;
            } else if (n % 1000_000L == 0L) {//miliseconds
                tu = TimeUnit.MILLISECONDS;
            } else if (n % 1000L == 0L) {// micro seconds
                tu = TimeUnit.MICROSECONDS;
            }

            this.time = Math.abs(
                    Math.addExact(
                            tu.convert(s, TimeUnit.SECONDS),
                            tu.convert(n, TimeUnit.NANOSECONDS)
                    )
            );// nano offset and seconds can be of different signs
            this.unit = tu;
        }
    }

    /**
     * Converts to {@link Duration}
     *
     * @return
     */
    public Duration toDuration() {
        return Duration.of(time, toChronoUnit(unit));
    }

    /**
     * Converts to {@link WaitTime} without checking overflow.
     *
     * @param tu
     * @return
     */
    public WaitTime convert(TimeUnit tu) {
        return WaitTime.convert(this, tu);
    }

    /**
     * Converts to {@link WaitTime} checking overflow.
     *
     * @param tu
     * @return
     */
    public WaitTime convertAsserting(TimeUnit tu) {
        if (canConvertWithoutOverflow(tu)) {
            return WaitTime.convert(this, tu);
        }
        throw convertionException(tu);
    }

    /**
     * {@link WaitTime#add(lt.lb.commons.threads.sync.WaitTime, lt.lb.commons.threads.sync.WaitTime)
     * }
     *
     * @param other
     * @return
     */
    public WaitTime add(WaitTime other) {
        return WaitTime.add(this, other);
    }

    /**
     * {@link WaitTime#subtract(lt.lb.commons.threads.sync.WaitTime, lt.lb.commons.threads.sync.WaitTime)
     * }
     *
     * @param other
     * @return
     */
    public WaitTime subtract(WaitTime other) {
        return WaitTime.subtract(this, other);
    }

    /**
     * Factory constructor
     *
     * @param time
     * @param unit
     * @return
     */
    public static WaitTime of(long time, TimeUnit unit) {
        return new WaitTime(time, unit);
    }

    /**
     * Factory constructor
     *
     * @param dur
     * @return
     */
    public static WaitTime of(Duration dur) {
        return new WaitTime(dur);
    }

    /**
     * Factory constructor with {@link TimeUnit#NANOSECONDS}
     *
     * @param time
     * @return
     */
    public static WaitTime ofNanos(long time) {
        return new WaitTime(time, TimeUnit.NANOSECONDS);
    }

    /**
     * Factory constructor with {@link TimeUnit#MICROSECONDS}
     *
     * @param time
     * @return
     */
    public static WaitTime ofMicros(long time) {
        return new WaitTime(time, TimeUnit.MICROSECONDS);
    }

    /**
     * Factory constructor with {@link TimeUnit#MILLISECONDS}
     *
     * @param time
     * @return
     */
    public static WaitTime ofMillis(long time) {
        return new WaitTime(time, TimeUnit.MILLISECONDS);
    }

    /**
     * Factory constructor with {@link TimeUnit#SECONDS}
     *
     * @param time
     * @return
     */
    public static WaitTime ofSeconds(long time) {
        return new WaitTime(time, TimeUnit.SECONDS);
    }

    /**
     * Factory constructor with {@link TimeUnit#MINUTES}
     *
     * @param time
     * @return
     */
    public static WaitTime ofMinutes(long time) {
        return new WaitTime(time, TimeUnit.MINUTES);
    }

    /**
     * Factory constructor with {@link TimeUnit#HOURS}
     *
     * @param time
     * @return
     */
    public static WaitTime ofHours(long time) {
        return new WaitTime(time, TimeUnit.HOURS);
    }

    /**
     * Factory constructor with {@link TimeUnit#DAYS}
     *
     * @param time
     * @return
     */
    public static WaitTime ofDays(long time) {
        return new WaitTime(time, TimeUnit.DAYS);
    }

    /**
     * Convert current wait time to give {@link TimeUnit} without checking
     * overflow.
     *
     * @param tu
     * @return
     */
    public long to(TimeUnit tu) {
        return tu.convert(time, unit);
    }

    /**
     * {@link WaitTime#to(TimeUnit.NANOSECONDS) }
     *
     * @return
     */
    public long toNanos() {
        return to(TimeUnit.NANOSECONDS);
    }

    /**
     * {@link WaitTime#to(TimeUnit.MICROSECONDS) }
     *
     * @return
     */
    public long toMicros() {
        return to(TimeUnit.MICROSECONDS);
    }

    /**
     * {@link WaitTime#to(TimeUnit.MILLISECONDS) }
     *
     * @return
     */
    public long toMillis() {
        return to(TimeUnit.MILLISECONDS);
    }

    /**
     * {@link WaitTime#to(TimeUnit.SECONDS) }
     *
     * @return
     */
    public long toSeconds() {
        return to(TimeUnit.SECONDS);
    }

    /**
     * {@link WaitTime#to(TimeUnit.MINUTES) }
     *
     * @return
     */
    public long toMinutes() {
        return to(TimeUnit.MINUTES);
    }

    /**
     * {@link WaitTime#to(TimeUnit.HOURS) }
     *
     * @return
     */
    public long toHours() {
        return to(TimeUnit.HOURS);
    }

    /**
     * {@link WaitTime#to(TimeUnit.DAYS) }
     *
     * @return
     */
    public long toDays() {
        return to(TimeUnit.DAYS);
    }

    /**
     * Convert given current {@link WaitTime} to given {@link TimeUnit} without
     * checking overflow.
     *
     * @param current
     * @param unit
     * @return
     */
    public static WaitTime convert(WaitTime current, TimeUnit unit) {
        return new WaitTime(unit.convert(current.time, current.unit), unit);
    }

    /**
     * The maximum amount of given unit type units that can be converted to
     * {@link TimeUnit#NANOSECONDS} without long overflow.
     *
     * @param unit
     * @return
     */
    public static long maxNanosForUnit(TimeUnit unit) {
        return MAX_NANOS.convert(unit).toNanos();
    }

    /**
     * Returns biggest amount of units that can be converted without
     * overflowing. Must supply correctly different {@link TimeUnit} arguments.
     *
     * @param bigger
     * @param smaller
     * @return
     * @throws IllegalArgumentException if bigger is not greater than smaller
     * based on {@link WaitTime#unitOrder(java.util.concurrent.TimeUnit)}
     */
    public static long maxForUnitConversion(TimeUnit bigger, TimeUnit smaller) {
        if (unitOrder(bigger) <= unitOrder(smaller)) {
            throw new IllegalArgumentException("Expected " + bigger + " to be bigger magnitude than " + smaller);
        }
        return WaitTime.of(Long.MAX_VALUE, smaller).to(bigger);
    }

    /**
     * Checks if current time is smaller or equal to
     * {@link WaitTime#maxForUnitConversion(java.util.concurrent.TimeUnit, java.util.concurrent.TimeUnit)}
     * if converting to smaller unit
     *
     * @param other
     * @return
     */
    public boolean canConvertWithoutOverflow(TimeUnit other) {
        if (unitOrder(unit) <= unitOrder(other)) {// smaller to bigger or same, no problem (like nanos to days)
            return true;
        }
        //bigger to smaller, need to check, (like days to nanos)
        return time <= maxForUnitConversion(unit, other);
    }

    private IllegalArgumentException convertionException(TimeUnit tu) {
        return new IllegalArgumentException("Convertion would exeed maximum, for " + tu + " max is:" + maxForUnitConversion(tu, unit));
    }

    /**
     * Same as {@link WaitTime#to(java.util.concurrent.TimeUnit) } but checks
     * overflow.
     *
     * @param tu
     * @return
     */
    public long toAssert(TimeUnit tu) {
        if (!canConvertWithoutOverflow(tu)) {
            throw convertionException(tu);
        }
        return to(tu);
    }

    /**
     * {@link WaitTime#toAssert(java.util.concurrent.TimeUnit)} with
     * {@link TimeUnit#NANOSECONDS}
     *
     * useful with {@link LockSupport} or simple await methods.
     *
     * @return
     */
    public long toNanosAssert() {
        return toAssert(TimeUnit.NANOSECONDS);
    }

    /**
     * {@link WaitTime#toAssert(java.util.concurrent.TimeUnit)} with
     * {@link TimeUnit#MILLISECONDS}
     *
     * useful with {@link Thread#sleep(long) } or simple await methods.
     *
     * @return
     */
    public long toMillisAssert() {
        return toAssert(TimeUnit.MILLISECONDS);
    }

    /**
     * Returns unit type order in ascending fashion from 0 to 6. <br>
     * {@link TimeUnit#NANOSECONDS} is 0 <br> {@link TimeUnit#MICROSECONDS} is 1
     * <br> {@link TimeUnit#MILLISECONDS} is 2 <br> {@link TimeUnit#SECONDS} is
     * 3 <br> {@link TimeUnit#MINUTES} is 4 <br> {@link TimeUnit#HOURS} is 5
     * <br> {@link TimeUnit#DAYS} is 6 <br>
     *
     * @param tu
     * @return
     */
    public static int unitOrder(TimeUnit tu) {
        // TimeUnit.ordinal() might work, but order of definition depends on JVM's, so do not risk it.
        switch (Objects.requireNonNull(tu)) {
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
                throw new IllegalArgumentException("Unrecognized TimeUnit " + tu);
        }
    }

    /**
     * a + b; Converts to smaller unit of the two. Uses
     * {@link Math#addExact(long, long)}.
     *
     * @param a
     * @param b
     * @return
     * @throws AritmeticException
     */
    public static WaitTime add(WaitTime a, WaitTime b) throws ArithmeticException {
        int aOrder = unitOrder(a.unit);
        int bOrder = unitOrder(b.unit);
        long sum = 0;
        TimeUnit unit = a.unit;
        if (aOrder == bOrder) {
            sum = Math.addExact(a.time, b.time);
        } else if (aOrder > bOrder) {
            sum = Math.addExact(b.unit.convert(a.time, a.unit), b.time);
            unit = b.unit;
        } else {
            sum = Math.addExact(a.unit.convert(b.time, b.unit), a.time);
        }
        return new WaitTime(sum, unit);

    }

    /**
     * max(a - b,0); Converts to smaller unit of the two. Coverts negative
     * results to zero. Uses {@link Math#subtractExact(long, long)}.
     *
     * @param a
     * @param b
     * @return
     */
    public static WaitTime subtract(WaitTime a, WaitTime b) {
        int aOrder = unitOrder(a.unit);
        int bOrder = unitOrder(b.unit);
        long sum = 0;
        TimeUnit smallerUnit = a.unit;
        if (aOrder > bOrder) {
            smallerUnit = b.unit;
        } 
        sum = Math.subtractExact(smallerUnit.convert(a.time, a.unit), smallerUnit.convert(b.time, b.unit));
        return new WaitTime(Math.max(0, sum), smallerUnit);
    }

    /**
     * {@link TimeUnit} to {@link ChronoUnit} mapping.
     *
     * @param timeUnit
     * @return
     */
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
                throw new IllegalArgumentException("No ChronoUnit equivalent for " + timeUnit);
        }
    }

    /**
     * {@link ChronoUnit} to {@link TimeUnit} mapping.
     *
     * @param timeUnit
     * @return
     */
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

    /**
     * {@link ChronoUnit} to {@link TimeUnit} mapping checking. TimeUnit only
     * supports up to {@link TimeUnit#DAYS}.
     *
     * @param timeUnit
     * @return
     */
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

    @Override
    public int compareTo(WaitTime b) {
        WaitTime a = this;
        int aOrder = unitOrder(a.unit);
        int bOrder = unitOrder(b.unit);
        if (aOrder == bOrder) {
            return Long.compare(a.time, b.time);
        } else if (aOrder > bOrder) {
            return Long.compare(b.unit.convert(a.time, a.unit), b.time);
        } else {
            return Long.compare(a.time, a.unit.convert(b.time, b.unit));
        }
    }

}
