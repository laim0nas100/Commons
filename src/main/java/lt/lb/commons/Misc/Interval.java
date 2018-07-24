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
        return max.doubleValue() - min.doubleValue();
    }

    public double getAbsDiff() {
        return Math.abs(getDiff());
    }
    
    private static double d(Number val){
        return val.doubleValue();
    }

    public boolean inRange(Number val, boolean minInclusive, boolean maxInclusive) {
        boolean inRange = true;

        double v = d(val);
        if (minInclusive) {
            inRange = v >= d(min);
        } else {
            inRange = v > d(min);
        }

        if (inRange) {
            if (maxInclusive) {
                inRange = v <= d(max);
            } else {
                inRange = v < d(max);
            }
        }

        return inRange;

    }

    public boolean inRangeExclusive(Number val) {
        return this.inRange(val, false, false);
    }

    public boolean inRangeInclusive(Number val) {
        return this.inRange(val, true, true);
    }

    public double clamp(Number val) {
        return Math.min(Math.max(d(val), min.doubleValue()), max.doubleValue());
    }

    public void expand(Number val) {
        double v = d(val);
        if (d(this.max) < v) {
            this.max = val;
        }

        if (d(this.min) > v) {
            this.min = val;
        }
    }

}
