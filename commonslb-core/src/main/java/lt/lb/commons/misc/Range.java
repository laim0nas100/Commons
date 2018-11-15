/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

import java.util.Comparator;
import lt.lb.commons.Lambda;

/**
 *
 * @author laim0nas100
 */
public class Range<T> extends MinMax<T> {

    protected ExtComparator<T> cmp;

    public Range(T min, T max, Comparator<T> cmp) {
        super(min, max);
        this.cmp = ExtComparator.of(cmp);
    }

    public static <T extends Comparable> Range<T> of(T min, T max) {
        return new Range(min, max, ExtComparator.ofComparable());
    }

    public boolean inRange(T val, boolean minInclusive, boolean maxInclusive) {
        Lambda.L2R<T, T, Boolean> cmpLess = minInclusive ? cmp::lessThanOrEq : cmp::lessThan;
        Lambda.L2R<T, T, Boolean> cmpMore = maxInclusive ? cmp::greaterThanOrEq : cmp::greaterThan;

        return cmpLess.apply(val, max) && cmpMore.apply(val, min);

    }

    public boolean inRangeExclusive(T val) {
        return this.inRange(val, false, false);
    }

    public boolean inRangeInclusive(T val) {
        return this.inRange(val, true, true);
    }

    public boolean inRangeIncExc(T val) {
        return this.inRange(val, true, false);
    }

    public boolean inRangeExcInc(T val) {
        return this.inRange(val, false, true);
    }

    public T clamp(T val) {
        return cmp.min(cmp.max(val, min), max);
    }

    public Range<T> expand(T val) {
        return new Range(cmp.min(val, min), cmp.max(val, max), cmp) {
        };
    }
}
