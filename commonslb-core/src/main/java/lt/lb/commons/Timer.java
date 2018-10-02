/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

/**
 *
 * @author laim0nas100
 * Minimal timer using System.nanoTime()
 */
public class Timer {

    private final long k = 1000;
    private final long m = k * k;
    private final long b = k * k * k;
    
    private long startNanos;

    public Timer(long nanos) {
        startNanos = nanos;
    }

    public Timer() {
        startNanos = System.nanoTime();
    }

    public long stopNanos(long nowNanos) {
        return nowNanos - startNanos;
    }

    public long stopMillis(long millis) {
        return stopNanos(millis * m) / m;
    }

    public long stopSeconds(long seconds) {
        return stopMillis(seconds * k) / k;
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

}
