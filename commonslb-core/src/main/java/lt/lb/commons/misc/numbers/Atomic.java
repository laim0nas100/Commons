package lt.lb.commons.misc.numbers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 *
 * @author laim0nas100
 */
public abstract class Atomic {

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
        return accumulateAndGet(atomic, toAdd, (current, add) -> {
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
        return accumulateAndGet(atomic, 1, (current, add) -> {
            int abs = Math.abs(current);
            int sum = abs + add;
            return sum <= limit ? sum : -abs;
        });
    }

    public static int getAndIncrement(AtomicInteger atomic, int inc) {
        for (;;) {
            int get = atomic.get();
            if (atomic.compareAndSet(get, get + inc)) {
                return get;
            }
            LockSupport.parkNanos(1);
        }
    }

    public static int getAndIncrement(AtomicInteger atomic) {
        return getAndIncrement(atomic, 1);
    }

    public static int getAndDecrement(AtomicInteger atomic) {
        return getAndIncrement(atomic, -1);
    }

    public static int incrementAndGet(AtomicInteger atomic, int inc) {
        for (;;) {
            int get = atomic.get();
            int ret = get + inc;
            if (atomic.compareAndSet(get, ret)) {
                return ret;
            }
            LockSupport.parkNanos(1);
        }
    }

    public static int incrementAndGet(AtomicInteger atomic) {
        return incrementAndGet(atomic, 1);
    }

    public static int decrementAndGet(AtomicInteger atomic) {
        return incrementAndGet(atomic, -1);
    }

    public static int accumulateAndGet(AtomicInteger atomic, int x, IntBinaryOperator accumulatorFunction) {
        return accumulate(true, atomic, x, accumulatorFunction);
    }

    public static int getAndAccumulate(AtomicInteger atomic, int x, IntBinaryOperator accumulatorFunction) {
        return accumulate(false, atomic, x, accumulatorFunction);
    }

    public static int accumulate(final boolean returnNext, AtomicInteger atomic, int x, IntBinaryOperator accumulatorFunction) {
        int prev = atomic.get(), next = 0;
        for (boolean haveNext = false;;) {
            if (!haveNext) {
                next = accumulatorFunction.applyAsInt(prev, x);
            }
            if (atomic.compareAndSet(prev, next)) {
                return returnNext ? next : prev;
            }
            LockSupport.parkNanos(1);
            haveNext = (prev == (prev = atomic.get()));
        }
    }

    public static int updateAndGet(AtomicInteger atomic, IntUnaryOperator updateFunction) {
        return update(true, atomic, updateFunction);
    }

    public static int getAndUpdate(AtomicInteger atomic, IntUnaryOperator updateFunction) {
        return update(false, atomic, updateFunction);
    }

    public static int update(final boolean returnNext, AtomicInteger atomic, IntUnaryOperator updateFunction) {

        int prev = atomic.get(), next = 0;
        for (boolean haveNext = false;;) {
            if (!haveNext) {
                next = updateFunction.applyAsInt(prev);
            }
            if (atomic.compareAndSet(prev, next)) {
                return returnNext ? next : prev;
            }
            LockSupport.parkNanos(1);
            haveNext = (prev == (prev = atomic.get()));
        }
    }
}
