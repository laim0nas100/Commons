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
public class Interval extends Range<Double> {

    public static Interval newExtendable() {
        return new Interval(Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    public static final Interval ZERO_ONE = new Interval(0d, 1d);
    public static final Interval MINUS_ONE_ONE = new Interval(-1d, 1d);

    public Interval(Double min, Double max) {
        super(min, max, Double::compare);
    }

    public double getDiff() {
        return max - min;
    }

    public double getAbsDiff() {
        return Math.abs(getDiff());
    }

    @Override
    public Interval expand(Double val) {
        Range<Double> expand = super.expand(val);
        return new Interval(expand.min, expand.max);
    }

}
