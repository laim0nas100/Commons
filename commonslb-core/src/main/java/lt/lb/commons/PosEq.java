package lt.lb.commons;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Positional equals helper for quick parameter comparison
 *
 * @author laim0nas100
 */
public class PosEq {

    private Object[] objs;

    private PosEq(Object[] objs) {
        this.objs = objs;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Arrays.deepHashCode(this.objs);
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

        return true;
    }

    public boolean eq(Object... objs) {
        return this.equals(PosEq.of(objs));
    }

    public boolean neq(Object... objs) {
        return !eq(objs);
    }

    /**
     * Some common elements
     * @param objs
     * @return 
     */
    public boolean any(Object... objs) {
        return Stream.of(objs).anyMatch(ob -> ArrayOp.contains(this.objs, ob));
    }

    /**
     * No common elements
     * @param objs
     * @return 
     */
    public boolean none(Object... objs) {
        return Stream.of(objs).noneMatch(ob -> ArrayOp.contains(this.objs, ob));
    }
    
    /**
     * All common elements
     * @param objs
     * @return 
     */
    public boolean all(Object... objs) {
        return Stream.of(objs).allMatch(ob -> ArrayOp.contains(this.objs, ob));
    }

    /**
     * Some null elements
     * @return 
     */
    public boolean anyNull() {
        return Stream.of(objs).anyMatch(ob -> ob == null);
    }

    /**
     * All null elements
     * @return 
     */
    public boolean allNull() {
        return Stream.of(objs).allMatch(ob -> ob == null);
    }

    /**
     * None null elements
     * @return 
     */
    public boolean noneNull() {
        return Stream.of(objs).allMatch(ob -> ob == null);
    }

    public static PosEq of(Object... objs) {
        return new PosEq(objs);
    }
}
