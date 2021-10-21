package lt.lb.commons.misc;

import java.util.Comparator;
import lt.lb.readablecompare.Compare;
import lt.lb.readablecompare.CompareNull;
import lt.lb.readablecompare.CompareOperator;
import lt.lb.readablecompare.SimpleCompare;

/**
 *
 * @author laim0nas100
 */
public class Range<T> extends MinMax<T> {

    protected SimpleCompare<T> simpleCmp;

    public Range(T min, T max, Comparator<T> cmp) {
        super(min, max);
        this.simpleCmp = Compare.of(CompareNull.NULL_THROW, cmp);
    }

    public static <T extends Comparable> Range<T> of(T min, T max) {
        return new Range(min, max, Comparator.naturalOrder());
    }

    public boolean inRange(T val, boolean minInclusive, boolean maxInclusive) {
        CompareOperator maxOp = maxInclusive ? CompareOperator.LESS_EQ : CompareOperator.LESS;
        CompareOperator minOp = minInclusive ? CompareOperator.GREATER_EQ : CompareOperator.GREATER;

        return simpleCmp.compare(val, maxOp, max) // val < max or val <= max
                && simpleCmp.compare(val, minOp, min);  // val > min or val >= min
    }

    /**
     * inside (min,max)
     *
     * @param val
     * @return
     */
    public boolean inRangeExclusive(T val) {
        return this.inRange(val, false, false);
    }

    /**
     * inside [min,max]
     *
     * @param val
     * @return
     */
    public boolean inRangeInclusive(T val) {
        return this.inRange(val, true, true);
    }

    /**
     * inside [min,max)
     *
     * @param val
     * @return
     */
    public boolean inRangeIncExc(T val) {
        return this.inRange(val, true, false);
    }

    /**
     * inside (min,max]
     *
     * @param val
     * @return
     */
    public boolean inRangeExcInc(T val) {
        return this.inRange(val, false, true);
    }

    /**
     * Return the value, or the limit, if not in bounds. Inclusive.
     *
     * @param val
     * @return
     */
    public T clamp(T val) {
        return simpleCmp.min(simpleCmp.max(val, min), max);
    }

    /**
     * Return new Range of expanded limits, if given value is not in range.
     *
     * @param val
     * @return
     */
    public Range<T> expand(T val) {
        return new Range(simpleCmp.min(val, min), simpleCmp.max(val, max), simpleCmp.cmp);
    }
}
