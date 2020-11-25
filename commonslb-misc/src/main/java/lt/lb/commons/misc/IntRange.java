package lt.lb.commons.misc;

import lt.lb.commons.misc.compare.ExtComparator;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 *
 * Range to check int-based ranges with custom assertions
 * @author laim0nas100
 */
public class IntRange extends Range<Integer> {

    public IntRange(Integer min, Integer max, Comparator<Integer> cmp) {
        super(min, max, cmp);
    }

    public static IntRange of(Integer min, Integer max) {
        return new IntRange(min, max, ExtComparator.ofComparable());
    }

    /**
     *
     * @return max - min
     */
    public Integer diff() {
        return max - min;
    }

    /**
     * Check if index is within range [min,max) and throw
     * IndexOutOfBoundsException if not
     *
     * @param i
     * @return
     */
    public IntRange assertIndexBoundsExclusive(Integer i) {
        if (!this.inRangeIncExc(i)) {
            throw new IndexOutOfBoundsException("Index " + i
                    + " does not fit in range [" + min + ", " + max + ")");
        }
        return this;
    }

    /**
     * Check if index is within range [min,max] and throw
     * IndexOutOfBoundsException if not
     *
     * @param i
     * @return
     */
    public IntRange assertIndexBoundsInclusive(Integer i) {
        if (!this.inRangeInclusive(i)) {
            throw new IndexOutOfBoundsException("Index " + i
                    + " does not fit in range [" + min + ", " + max + "]");
        }
        return this;
    }

    /**
     * Check if range is at least of given size and throw
     * IllegalArgumentException if not
     *
     * @param size
     * @return
     */
    public IntRange assertRangeSizeAtLeast(Integer size) {
        if (this.diff() < size) {
            throw new IllegalArgumentException("Range size too small, expected at least size of "
                    + size + " got range of size " + diff() + " (" + min + ":" + max + ")");
        }
        return this;
    }

    /**
     * Check if range is at least of given size and throw
     * IllegalArgumentException if not
     *
     * @param size
     * @return
     */
    public IntRange assertRangeSizeAtMost(Integer size) {
        if (this.diff() > size) {
            throw new IllegalArgumentException("Range size too big, expected at most size of "
                    + size + " got range of size " + diff() + " (" + min + ":" + max + ")");
        }
        return this;
    }
    /**
     * Custom assert method. Throw IllegalArgumentException if does not satisfy predicate.
     * @param cons
     * @return 
     */
    public IntRange assertRangeIsValidIf(BiPredicate<Integer,Integer> cons){
        return assertRangeNotValidIf(cons.negate());
    }
    
    /**
     * Custom assert method. Throw IllegalArgumentException if does satisfy predicate.
     * @param cons
     * @return 
     */
    public IntRange assertRangeNotValidIf(BiPredicate<Integer,Integer> cons){
        if(cons.test(min, max)){
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
