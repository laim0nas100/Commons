package lt.lb.commons;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Positional equals helper for quick parameter comparison
 *
 * @author laim0nas100
 */
public class PosEq {

    public final Object[] objs;
    public final Equator eq;

    private PosEq(Object[] objs) {
        this(objs, Equator.simpleHashEquator());
    }

    public boolean isEmpty() {
        return objs.length == 0;
    }

    private PosEq(Object[] objs, Equator eq) {
        this.objs = Objects.requireNonNull(objs);
        this.eq = Objects.requireNonNull(eq);
    }

    public PosEq withEquator(Equator eq) {
        return new PosEq(objs, eq);
    }

    public PosEq withObjects(Object... obj) {
        return new PosEq(obj, eq);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Equator.deepHashCode(eq, objs);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PosEq other = (PosEq) obj;
        if (!Arrays.deepEquals(this.objs, other.objs)) {
            return false;
        }
        if (!Objects.equals(this.eq, other.eq)) {
            return false;
        }
        return true;
    }

    /**
     * Every object is equal to this objects array at the respected position
     *
     * @param objs
     * @return
     */
    public boolean eq(Object... objs) {
        return Equator.deepEqualsArray(eq, this.objs, objs);
    }

    /**
     * Some object is not equal to this object at respected position in the
     * array
     *
     * @param objs
     * @return
     */
    public boolean neq(Object... objs) {
        return !eq(objs);
    }

    /**
     * Some common elements
     *
     * @param objs
     * @return
     */
    public boolean any(Object... objs) {
        if (ArrayOp.isEmpty(objs)) {
            return false;
        }
        for (Object myElem : this.objs) {
            for (Object yourElem : objs) {
                if (Equator.deepEquals(eq, myElem, yourElem)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * No common elements
     *
     * @param objs
     * @return
     */
    public boolean none(Object... objs) {
        return !any(objs);
    }

    /**
     * All common elements
     *
     * @param objs
     * @return
     */
    public boolean all(Object... objs) {
        if (isEmpty()) {
            return false;
        }
        if (ArrayOp.isEmpty(objs)) {
            return false;
        }
        for (Object elem : objs) {
            if (!contains(elem)) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(Object element) {
        if (isEmpty()) {
            return false;
        }
        for (Object myElem : this.objs) {
            if (Equator.deepEquals(eq, myElem, element)) {
                return true;
            }

        }
        return false;
    }

    /**
     * Some null elements
     *
     * @return
     */
    public boolean anyNull() {
        return any(p -> p == null);
    }

    /**
     * Some elements satisfying predicate
     *
     * @param pred
     * @return
     */
    public boolean any(Predicate pred) {
        return Stream.of(objs).anyMatch(pred);
    }

    /**
     * All null elements
     *
     * @return
     */
    public boolean allNull() {
        return all(ob -> ob == null);
    }

    /**
     * All elements satisfy predicate
     *
     * @param pred
     * @return
     */
    public boolean all(Predicate pred) {
        return Stream.of(objs).allMatch(pred);
    }

    /**
     * None elements satisfy predicate
     *
     * @param pred
     * @return
     */
    public boolean none(Predicate pred) {
        return Stream.of(objs).noneMatch(pred);
    }

    /**
     * None null elements
     *
     * @return
     */
    public boolean noneNull() {
        return none(ob -> ob == null);
    }

    /**
     * Construct array-based parameter comparator with inner elements.
     *
     * @param objs
     * @return
     */
    public static PosEq of(Object... objs) {
        return new PosEq(objs);
    }
}
