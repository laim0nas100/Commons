/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

/**
 *
 * @author laim0nas100 Minimal timer using System.nanoTime()
 */
public class Timer {

    private final long k = 1000;
    private final long m = k * k;
    private final long b = k * k * k;

    private long startNanos;
    private long lastStopNanos;

    public Timer(long nanos) {
        startNanos = nanos;
    }

    public Timer() {
        startNanos = System.nanoTime();
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
        return stopNanos(System.nanoTime());
    }

    public long stopMillis() {
        return stopNanos(System.nanoTime()) / m;
    }

    public long stopSeconds() {
        return stopNanos(System.nanoTime()) / b;
    }

    public long lastStopNanos(long nanos) {
        long timePassed = lastStopNanos - nanos;
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
        return lastStopNanos(System.nanoTime());
    }

    public long lastStopMillis() {
        return lastStopNanos(System.nanoTime()) / m;
    }

    public long lastStopSeconds() {
        return lastStopNanos(System.nanoTime()) / b;
    }
}
