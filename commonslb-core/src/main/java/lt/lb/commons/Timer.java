package lt.lb.commons;

/**
 *
 * @author laim0nas100 Minimal timer using System.nanoTime()
 */
public class Timer {

    private final long k = 1000;
    private final long m = k * k;
    private final long b = k * k * k;

    private long startNanos; //timer create time
    private long lastStopNanos; // incremental timer, used with lastStop

    public Timer(long nanos) {
        startNanos = nanos;
        lastStopNanos = startNanos;
    }

    public Timer() {
        this(getNanoTime());
    }

    public long stopNanos(long nowNanos) {
        lastStopNanos = nowNanos;
        return nowNanos - startNanos;
    }

    public long stopMillis(long millis) {
        return stopNanos(millis * m) / m;
    }

    public long stopSeconds(long seconds) {
        return stopNanos(seconds * b) / b;
    }

    public long stopNanos() {
        return stopNanos(getNanoTime());
    }

    public long stopMillis() {
        return stopNanos(getNanoTime()) / m;
    }

    public long stopSeconds() {
        return stopNanos(getNanoTime()) / b;
    }

    public long lastStopNanos(long nanos) {
        long timePassed = nanos - lastStopNanos;
        stopNanos(nanos);
        return timePassed;
    }

    public long lastStopMillis(long millis) {
        return lastStopNanos(millis * m) / m;
    }

    public long lastStopSeconds(long seconds) {
        return lastStopNanos(seconds * b) / b;
    }

    public long lastStopNanos() {
        return lastStopNanos(getNanoTime());
    }

    public long lastStopMillis() {
        return lastStopNanos(getNanoTime()) / m;
    }

    public long lastStopSeconds() {
        return lastStopNanos(getNanoTime()) / b;
    }

    private static final long timeOffset = System.nanoTime() - Long.MIN_VALUE;

    public static final long getNanoTime() {
        return System.nanoTime() - timeOffset;
    }

}
