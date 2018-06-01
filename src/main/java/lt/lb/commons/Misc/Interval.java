/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Misc;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class Interval extends MinMax {

    public static Interval newExtendable() {
        return new Interval(Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    public Interval(Number min, Number max) {
        super(min, max);
    }

    public double getDiff() {
        return max - min;
    }

    public double getAbsDiff() {
        return Math.abs(getDiff());
    }

    public boolean inRange(double val, boolean minInclusive, boolean maxInclusive) {
        boolean inRange = true;

        if (minInclusive) {
            inRange = val >= min;
        } else {
            inRange = val > min;
        }

        if (inRange) {
            if (maxInclusive) {
                inRange = val <= max;
            } else {
                inRange = val < max;
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

    public void expand(double val) {
        if (this.max < val) {
            this.max = val;
        }

        if (this.min > val) {
            this.min = val;
        }
    }

}
