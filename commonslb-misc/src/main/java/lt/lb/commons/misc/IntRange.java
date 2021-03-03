package lt.lb.commons.misc;

import java.util.Comparator;
import java.util.function.BiPredicate;

/**
 *
 * Range to check int-based ranges with custom assertions. Useful for argument
 * checking, or just passing int ranges.
 *
 * @author laim0nas100
 */
public class IntRange extends Range<Integer> {

    public IntRange(Integer min, Integer max, Comparator<Integer> cmp) {
        super(min, max, cmp);
    }

    public static IntRange of(Integer min, Integer max) {
        return new IntRange(min, max, Comparator.naturalOrder());
    }

    /**
     *
     * @return max - min
     */
    public Integer getDiff() {
        return max - min;
    }

    /**
     * |max - min|
     *
     * @return
     */
    public Integer getAbsDiff() {
        return Math.abs(getDiff());
    }

    /**
     * Check if index(es) is within range [min,max) and throw
     * IndexOutOfBoundsException if not
     *
     * @param indexes
     * @return
     */
    public IntRange assertIndexBoundsExclusive(int... indexes) {
        for (int i : indexes) {
            if (!this.inRangeIncExc(i)) {
                throw new IndexOutOfBoundsException("Index " + i
                        + " does not fit in range [" + min + ", " + max + ")");
            }
        }

        return this;
    }

    /**
     * Check if index(es) is within range [min,max] and throw
     * IndexOutOfBoundsException if not
     *
     * @param indexes
     * @return
     */
    public IntRange assertIndexBoundsInclusive(int... indexes) {
        for (int i : indexes) {
            if (!this.inRangeInclusive(i)) {
                throw new IndexOutOfBoundsException("Index " + i
                        + " does not fit in range [" + min + ", " + max + "]");
            }
        }

        return this;
    }

    /**
     * Check if range is at least of given size(es) and throw
     * IllegalArgumentException if not
     *
     * @param sizes
     * @return
     */
    public IntRange assertRangeSizeAtLeast(int... sizes) {
        for (int size : sizes) {
            if (this.getDiff() < size) {
                throw new IllegalArgumentException("Range size too small, expected at least size of "
                        + size + " got range of size " + getDiff() + " (" + min + ":" + max + ")");
            }
        }

        return this;
    }

    /**
     * Check if range is at least of given size and throw
     * IllegalArgumentException if not
     *
     * @param sizes
     * @return
     */
    public IntRange assertRangeSizeAtMost(int... sizes) {
        for (int size : sizes) {
            if (this.getDiff() > size) {
                throw new IllegalArgumentException("Range size too big, expected at most size of "
                        + size + " got range of size " + getDiff() + " (" + min + ":" + max + ")");
            }
        }

        return this;
    }

    /**
     * Custom assert method. Throw IllegalArgumentException if does not satisfy
     * predicate.
     *
     * @param cons
     * @return
     */
    public IntRange assertRangeIsValidIf(BiPredicate<Integer, Integer> cons) {
        return assertRangeNotValidIf(cons.negate());
    }

    /**
     * Custom assert method. Throw IllegalArgumentException if does satisfy
     * predicate.
     *
     * @param cons
     * @return
     */
    public IntRange assertRangeNotValidIf(BiPredicate<Integer, Integer> cons) {
        if (cons.test(min, max)) {
            throw new IllegalArgumentException("Invalid range (" + min + ":" + max + ")");
        }
        return this;
    }

    /**
     * Check if max &ge min and throw IllegalArgumentException if not
     *
     * @return
     */
    public IntRange assertRangeIsValid() {
        if (max < min) {
            throw new IllegalArgumentException("Invalid range (" + min + ":" + max + ")");
        }
        return this;
    }

}
