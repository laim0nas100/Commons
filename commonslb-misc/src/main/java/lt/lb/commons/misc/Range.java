package lt.lb.commons.misc;

import java.util.Comparator;
import java.util.Objects;
import lt.lb.readablecompare.Bound;
import lt.lb.readablecompare.Compare;
import lt.lb.readablecompare.CompareNull;
import lt.lb.readablecompare.SimpleCompare;

/**
 *
 * @author laim0nas100
 */
public class Range<T> extends MinMax<T> {

    protected SimpleCompare<T> simpleCmp;

    public Range(T min, T max, SimpleCompare<T> cmp) {
        super(min, max);
        this.simpleCmp = Objects.requireNonNull(cmp);
    }

    public Range(T min, T max, Comparator<T> cmp) {
        this(min, max, Compare.of(CompareNull.NULL_THROW, cmp));
    }

    public static <T extends Comparable> Range<T> of(T min, T max) {
        return new Range(min, max, Comparator.naturalOrder());
    }

    /**
     * inside range, given the concrete bound
     *
     * @param bound
     * @param val
     * @return
     */
    public boolean inRange(Bound bound, T val) {
        return simpleCmp.inside(bound, min, val, max);
    }

    /**
     * inside (min,max)
     *
     * @param val
     * @return
     */
    public boolean inRangeExclusive(T val) {
        return inRange(Bound.EXC_EXC, val);
    }

    /**
     * inside [min,max]
     *
     * @param val
     * @return
     */
    public boolean inRangeInclusive(T val) {
        return inRange(Bound.INC_INC, val);

    }

    /**
     * inside [min,max)
     *
     * @param val
     * @return
     */
    public boolean inRangeIncExc(T val) {
        return inRange(Bound.INC_EXC, val);

    }

    /**
     * inside (min,max]
     *
     * @param val
     * @return
     */
    public boolean inRangeExcInc(T val) {
        return inRange(Bound.EXC_INC, val);
    }

    /**
     * Return the value, or the limit, if not in bounds. Inclusive.
     *
     * @param val
     * @return
     */
    public T clamp(T val) {
        return simpleCmp.clamp(min, val, max);
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
