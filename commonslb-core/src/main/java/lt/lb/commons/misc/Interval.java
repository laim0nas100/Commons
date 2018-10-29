/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

/**
 *
 * @author laim0nas100
 */
public class Interval<T> extends MinMax<Double> {

    public static Interval<Double> newExtendable() {
        return new Interval<>(Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    public static final Interval<Double> ZERO_ONE = new Interval(0d, 1d);
    public static final Interval<Double> MINUS_ONE_ONE = new Interval(-1d, 1d);

    public Interval(Double min, Double max) {
        super(min, max);
    }

    public double getDiff() {
        return max - min;
    }

    public double getAbsDiff() {
        return Math.abs(getDiff());
    }

    private static double d(Number val) {
        return val.doubleValue();
    }

    public boolean inRange(double val, boolean minInclusive, boolean maxInclusive) {
        boolean inRange = true;

        double v = d(val);
        if (minInclusive) {
            inRange = v >= min;
        } else {
            inRange = v > min;
        }

        if (inRange) {
            if (maxInclusive) {
                inRange = v <= max;
            } else {
                inRange = v < max;
            }
        }

        return inRange;

    }

    public boolean inRangeExclusive(double val) {
        return this.inRange(val, false, false);
    }

    public boolean inRangeInclusive(double val) {
        return this.inRange(val, true, true);
    }

    public double clamp(double val) {
        return Math.min(Math.max(val, min), max);
    }

    public Interval expand(double v) {
        double newMax = Math.max(max, v);
        double newMin = Math.min(min, v);
        return new Interval(newMin, newMax);
    }

}
