package lt.lb.commons.misc.compare;

import java.util.Comparator;
import java.util.Objects;
import lt.lb.commons.Equator;
import static lt.lb.commons.misc.compare.Compare.CompareNull.*;
import static lt.lb.commons.misc.compare.Compare.CompareOperator.*;

/**
 * Simple and easily readable comparator API.
 *
 * @author laim0nas100
 */
public abstract class Compare {

    public static enum CompareOperator {
        LESS, LESS_EQ, GREATER, GREATER_EQ, EQ, NOT_EQ
    }

    public static CompareNull reverseNullOrder(CompareNull cmp) {
        if (cmp == NULL_FIRST) {
            return NULL_LAST;
        }
        if (cmp == NULL_LAST) {
            return NULL_FIRST;
        }
        return cmp;
    }

    public static enum CompareNull {

        /**
         * Null argument is treated as a lower of the two
         */
        NULL_FIRST,
        /**
         * Null argument is treated as a higher of the two
         */
        NULL_LAST,
        /**
         * Null argument is treated as equal
         */
        NULL_IGNORE,
        /**
         * Null arguments throw {@link IllegalArgumentException}
         */
        NULL_THROW
    }

    public static <T> boolean compare(T elem1, CompareOperator cmpOp, T elem2, Comparator<T> cmp, CompareNull nullOp) {
        Objects.requireNonNull(cmpOp);
        Objects.requireNonNull(nullOp);
        Objects.requireNonNull(cmp);
        int cmpRes = cmpAll(elem1, elem2, cmp, nullOp);
        return cmpResultSwitch(cmpRes, cmpOp);

    }

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

    public static <T> boolean compareNulls(T elem1, CompareOperator cmpOp, T elem2, CompareNull nullOp) {
        Objects.requireNonNull(cmpOp);
        int cmpNullRes = cmpNulls(elem1, elem2, nullOp);
        return cmpResultSwitch(cmpNullRes, cmpOp);

    }

    public static <T> int cmpAll(T elem1, T elem2, Comparator<T> cmp, CompareNull cmpNull) {
        Objects.requireNonNull(cmpNull);
        Objects.requireNonNull(cmp);

        if (elem1 == null || elem2 == null) {
            return cmpNulls(elem1, elem2, cmpNull);
        } else {
            return cmp.compare(elem1, elem2);
        }
    }

    public static <T> int cmpNulls(T elem1, T elem2, CompareNull nullOp) {
        boolean firstNull = elem1 == null;
        boolean secondNull = elem2 == null;
        boolean bothNull = firstNull && secondNull;

        if (!firstNull && !secondNull) {
            throw new IllegalArgumentException("Both arguments aren't null");
        }
        if (nullOp == NULL_THROW) {
            if (firstNull) {
                throw new IllegalArgumentException("Recieved a null first argument");
            }
            if (secondNull) {
                throw new IllegalArgumentException("Recieved a null second argument");
            }
        }
        if (bothNull || nullOp == NULL_IGNORE) {
            return 0;
        }
        if (nullOp == NULL_FIRST) {
            return firstNull ? -1 : 1;
        } else {
            return firstNull ? 1 : -1;
        }
    }

    public static <T> boolean compareNotNulls(T elem1, CompareOperator cmpOp, T elem2, Comparator<T> cmp) {
        Objects.requireNonNull(cmp);
        Objects.requireNonNull(cmpOp);
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
            this.nullCmp = nullCmp;
            this.cmp = cmp;
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
