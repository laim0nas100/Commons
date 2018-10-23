/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads.sync;

import java.util.concurrent.TimeUnit;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.misc.ExtComparator;

/**
 *
 * Convenient class to group TimeUnit and long for concurrent waiting.
 *
 * @author laim0nas100
 */
public class WaitTime {

    public final long time;
    public final TimeUnit unit;

    public WaitTime(long time, TimeUnit unit) {
        this.time = time;
        this.unit = unit;
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

    private static final TimeUnit[] units = ArrayOp.asArray(
            TimeUnit.NANOSECONDS,
            TimeUnit.MICROSECONDS,
            TimeUnit.MILLISECONDS,
            TimeUnit.SECONDS,
            TimeUnit.MINUTES,
            TimeUnit.HOURS,
            TimeUnit.DAYS
    );

    private static int unitOrder(TimeUnit tu) {
        return F.find(units, (i, u) -> u == tu).get().g1; // ordinal() might work, but order of definition might be different in other JVM's
    }

    public static WaitTime add(WaitTime a, WaitTime b) {
        int aOrder = unitOrder(a.unit);
        int bOrder = unitOrder(b.unit);
        ExtComparator<Integer> cmp = ExtComparator.ofComparable();
        long sum = 0;
        TimeUnit unit = a.unit;
        if (cmp.equals(aOrder, bOrder)) {
            sum = a.time + b.time;
        } else if (cmp.greaterThan(aOrder, bOrder)) {
            sum = b.unit.convert(a.time, a.unit) + b.time;
            unit = b.unit;
        } else {
            sum = a.unit.convert(b.time, b.unit) + a.time;
        }
        return new WaitTime(sum, unit);

    }
    
    public static WaitTime subtract(WaitTime a, WaitTime b){
        int aOrder = unitOrder(a.unit);
        int bOrder = unitOrder(b.unit);
        ExtComparator<Integer> cmp = ExtComparator.ofComparable();
        long sum = 0;
        TimeUnit unit = a.unit;
        if (cmp.equals(aOrder, bOrder)) {
            sum = a.time - b.time;
        } else if (cmp.greaterThan(aOrder, bOrder)) {
            sum = b.unit.convert(a.time, a.unit) - b.time;
            unit = b.unit;
        } else {
            sum = a.time - a.unit.convert(b.time, b.unit);
        }
        return new WaitTime(sum, unit);
    }

}
