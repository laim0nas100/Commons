package lt.lb.commons.misc.numbers;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author laim0nas100
 */
public class Atomic {

    /**
     * Using both signs to determine if increment was successful. Will return
     * negative value of the same amount if increment if value toAdd was not
     * successful.
     *
     * @param atomic
     * @param toAdd
     * @param limit
     * @param sumOffset
     * @return
     */
    public static int signedAccumulate(AtomicInteger atomic, final int toAdd, final int limit, final int sumOffset) {
        return atomic.accumulateAndGet(toAdd, (current, add) -> {
            int abs = Math.abs(current);
            int sum = abs + add;
            return (sumOffset + sum) <= limit ? sum : -abs;
        });
    }

    /**
     * Using both signs to determine if increment by one was successful. Will
     * return negative value of the same amount if increment was not successful.
     *
     * @param atomic
     * @param limit
     * @return
     */
    public static int signedIncrement(AtomicInteger atomic, final int limit) {
        return atomic.accumulateAndGet(1, (current, add) -> {
            int abs = Math.abs(current);
            int sum = abs + add;
            return sum <= limit ? sum : -abs;
        });
    }
}
