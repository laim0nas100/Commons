package lt.lb.commons;

import static lt.lb.commons.Java.getNanoTime;

/**
 *
 * @author laim0nas100 Minimal timer using System.nanoTime()
 */
public class Timer {

    private static final long K = 1000;
    private static final long M = K * K;
    private static final long B = K * K * K;

    private long startNanos; //timer create time
    private long lastStopNanos; // incremental timer, used with lastStop

    /**
     * Create timer with custom instance
     * @param nanos long value to represent some instance in time
     */
    public Timer(long nanos) {
        startNanos = nanos;
        lastStopNanos = startNanos;
    }

    /**
     * Create timer, with default value from Timer.getNanoTime
     */
    public Timer() {
        this(getNanoTime());
    }

    public long stopNanos(long nowNanos) {
        lastStopNanos = nowNanos;
        return nowNanos - startNanos;
    }

    public long stopMillis(long nanos) {
        return stopNanos(nanos) / M;
    }

    public long stopSeconds(long nanos) {
        return stopNanos(nanos) / B;
    }

    public long stopNanos() {
        return stopNanos(getNanoTime());
    }

    public long stopMillis() {
        return stopNanos(getNanoTime()) / M;
    }

    public long stopSeconds() {
        return stopNanos(getNanoTime()) / B;
    }

    public long lastStopNanos(long nanos) {
        long timePassed = nanos - lastStopNanos;
        stopNanos(nanos);
        return timePassed;
    }

    public long lastStopMillis(long nanos) {
        return lastStopNanos(nanos) / M;
    }

    public long lastStopSeconds(long nanos) {
        return lastStopNanos(nanos) / B;
    }

    public long lastStopNanos() {
        return lastStopNanos(getNanoTime());
    }

    public long lastStopMillis() {
        return lastStopNanos(getNanoTime()) / M;
    }

    public long lastStopSeconds() {
        return lastStopNanos(getNanoTime()) / B;
    }
    
    
    public long getStartNanos(){
        return this.startNanos;
    }
    
    public long getLastStopNanos(){
        return this.lastStopNanos;
    }

}
