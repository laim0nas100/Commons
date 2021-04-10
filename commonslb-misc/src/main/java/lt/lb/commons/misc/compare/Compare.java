package lt.lb.commons.misc.compare;

import java.util.Comparator;
import java.util.Objects;
import lt.lb.commons.Equator;
import static lt.lb.commons.misc.compare.Compare.CompareNull.*;
import static lt.lb.commons.misc.compare.Compare.CompareOperator.*;

/**
 * Easily readable null-friendly compare API.
 *
 * @author laim0nas100
 */
public abstract class Compare {

    public static enum CompareOperator {
        LESS, LESS_EQ, GREATER, GREATER_EQ, EQ, NOT_EQ
    }

    /**
     * If possible, reverses null order. Only works with {@link CompareNull#NULL_LOWER) or {@link CompareNull#NULL_HIGHER}.
     *
     * @param cmp
     * @return
     */
    public static CompareNull reverseNullOrder(CompareNull cmp) {
        if (cmp == NULL_LOWER) {
            return NULL_HIGHER;
        }
        if (cmp == NULL_HIGHER) {
            return NULL_LOWER;
        }
        return cmp;
    }

    /**
     * How to behave when encountering a null element in a comparison operation.
     */
    public static enum CompareNull {

        /**
         * Null argument is treated as a lower of the two
         */
        NULL_LOWER,
        /**
         * Null argument is treated as a higher of the two
         */
        NULL_HIGHER,
        /**
         * Null argument is treated as equal
         */
        NULL_EQUAL,
        /**
         * Null arguments throw {@link NullPointerException}
         */
        NULL_THROW
    }

    /**
     * Compare 2 optionally-null {@link Comparable} elements using given
     * {@link CompareNull} operator treating {@code null} as lower of the two.
     *
     * @param <T>
     * @param elem1
     * @param cmpOp
     * @param elem2
     * @return
     */
    public static <T extends Comparable<T>> boolean compareNullLower(T elem1, CompareOperator cmpOp, T elem2) {
        return compare(elem1, cmpOp, elem2, Comparator.naturalOrder(), NULL_LOWER);
    }

    /**
     * Compare 2 optionally-null {@link Comparable} elements using given
     * {@link CompareNull} operator treating {@code null} as higher of the two.
     *
     * @param <T>
     * @param elem1
     * @param cmpOp
     * @param elem2
     * @return
     */
    public static <T extends Comparable<T>> boolean compareNullHigher(T elem1, CompareOperator cmpOp, T elem2) {
        return compare(elem1, cmpOp, elem2, Comparator.naturalOrder(), NULL_HIGHER);
    }

    /**
     * Compare 2 optionally-null operators based on given {@link CompareNull}
     * and {@link CompareNull} then map result according to
     * {@link CompareOperator}.
     *
     * @param <T>
     * @param elem1
     * @param cmpOp
     * @param elem2
     * @param cmp
     * @param nullOp
     * @return
     */
    public static <T> boolean compare(T elem1, CompareOperator cmpOp, T elem2, Comparator<T> cmp, CompareNull nullOp) {
        Objects.requireNonNull(cmpOp);
        Objects.requireNonNull(nullOp);
        Objects.requireNonNull(cmp);
        int cmpRes = cmpAll(elem1, elem2, cmp, nullOp);
        return cmpResultSwitch(cmpRes, cmpOp);

    }

    /**
     * Map traditional (-1,0,1) result to given {@link CompareOperator}.
     *
     * @param cmpRes
     * @param cmpOp
     * @return
     */
    public static boolean cmpResultSwitch(int cmpRes, CompareOperator cmpOp) {
        Objects.requireNonNull(cmpOp);
        switch (cmpOp) {
            case LESS:
                return cmpRes < 0;
            case LESS_EQ:
                return cmpRes <= 0;
            case GREATER:
                return cmpRes > 0;
            case GREATER_EQ:
                return cmpRes >= 0;
            case EQ:
                return cmpRes == 0;
            case NOT_EQ:
                return cmpRes != 0;
            default:
                throw new IllegalArgumentException("Unrecognized CompareOperator:" + cmpOp);
        }
    }

    /**
     * Compare 2 optionally-null operators based on given {@link CompareNull}
     * and map result according to {@link CompareOperator}.
     *
     * @param <T>
     * @param elem1
     * @param cmpOp
     * @param elem2
     * @param nullOp
     * @return
     */
    public static <T> boolean compareNulls(T elem1, CompareOperator cmpOp, T elem2, CompareNull nullOp) {
        Objects.requireNonNull(cmpOp);
        int cmpNullRes = cmpNulls(elem1, elem2, nullOp);
        return cmpResultSwitch(cmpNullRes, cmpOp);

    }

    /**
     * Traditional (-1,0,1) compare 2 optionally-null operators based on given
     * {@link Comparator} and {@link CompareNull}.
     *
     * @param <T>
     * @param elem1
     * @param elem2
     * @param cmp
     * @param cmpNull
     * @return
     */
    public static <T> int cmpAll(T elem1, T elem2, Comparator<T> cmp, CompareNull cmpNull) {
        Objects.requireNonNull(cmpNull);
        Objects.requireNonNull(cmp);

        if (elem1 == null || elem2 == null) {
            return cmpNulls(elem1, elem2, cmpNull);
        } else {
            return cmp.compare(elem1, elem2);
        }
    }

    /**
     * Traditional (-1,0,1) compare 2 optionally-null elements based on
     * {@link CompareNull} operator. One of them must be null though.
     *
     * @param <T>
     * @param elem1
     * @param elem2
     * @param nullOp
     * @return
     */
    public static <T> int cmpNulls(T elem1, T elem2, CompareNull nullOp) {
        Objects.requireNonNull(nullOp);
        boolean firstNull = elem1 == null;
        boolean secondNull = elem2 == null;
        boolean bothNull = firstNull && secondNull;

        if (!firstNull && !secondNull) {
            throw new IllegalArgumentException("One of the arguments must be null");
        }
        if (nullOp == NULL_THROW) {
            Objects.requireNonNull(elem1, "Recieved a null first argument");
            Objects.requireNonNull(elem2, "Recieved a null second argument");
        }
        if (bothNull || nullOp == NULL_EQUAL) {
            return 0;
        }
        if (nullOp == NULL_LOWER) {
            return firstNull ? -1 : 1;
        } else {
            return firstNull ? 1 : -1;
        }
    }

    /**
     * Traditional (-1,0,1) compare 2 non-null elements using
     * {@link CompareOperator} and {@link Comparator}.
     *
     * @param <T>
     * @param elem1
     * @param cmpOp
     * @param elem2
     * @param cmp
     * @return
     */
    public static <T> boolean compareNotNulls(T elem1, CompareOperator cmpOp, T elem2, Comparator<T> cmp) {
        Objects.requireNonNull(cmp, "Comparator is null");
        Objects.requireNonNull(cmpOp, "CompareOperator is null");
        Objects.requireNonNull(elem1, "Element 1 is null");
        Objects.requireNonNull(elem2, "Element 2 is null");
        int compare = cmp.compare(elem1, elem2);
        return cmpResultSwitch(compare, cmpOp);
    }

    public static <T> SimpleCompare<T> of(CompareNull nullCmp, Comparator<T> cmp) {
        return new SimpleCompare<>(nullCmp, cmp);
    }

    public static <T extends Comparable<T>> SimpleCompare<T> of(CompareNull nullCmp) {
        Comparator<T> cmp = Comparator.naturalOrder();
        return new SimpleCompare<>(nullCmp, cmp);
    }

    public static class SimpleCompare<T> implements Comparator<T>, Equator<T> {

        public final CompareNull nullCmp;
        public final Comparator<T> cmp;

        protected SimpleCompare(CompareNull nullCmp, Comparator<T> cmp) {
            this.nullCmp = Objects.requireNonNull(nullCmp, "CompareNull is null");
            this.cmp = Objects.requireNonNull(cmp, "Comparator is null");
        }

        public boolean compare(T elem1, CompareOperator op, T elem2) {
            return Compare.compare(elem1, op, elem2, cmp, nullCmp);
        }

        @Override
        public int compare(T o1, T o2) {
            return Compare.cmpAll(o1, o2, cmp, nullCmp);
        }

        /**
         *
         * @param o1
         * @param o2
         * @return bigger value by this comparator
         */
        public T max(T o1, T o2) {
            return compare(o1, GREATER_EQ, o2) ? o1 : o2;
        }

        /**
         *
         * @param o1
         * @param o2
         * @return smaller value by this comparator
         */
        public T min(T o1, T o2) {
            return compare(o1, LESS_EQ, o2) ? o1 : o2;
        }

        @Override
        public Comparator<T> reversed() {
            CompareNull reverseNullOrder = Compare.reverseNullOrder(this.nullCmp);
            return new SimpleCompare<>(reverseNullOrder, cmp.reversed());
        }

        @Override
        public Comparator<T> thenComparing(Comparator<? super T> other) {
            return new SimpleCompare<>(this.nullCmp, cmp.thenComparing(other));
        }

        public SimpleCompare<T> thenComparing(CompareNull nullCmp, Comparator<? super T> other) {
            return new SimpleCompare<>(nullCmp, cmp.thenComparing(other));
        }

        @Override
        public boolean equate(T value1, T value2) {
            return compare(value1, EQ, value2);
        }

    }

}
