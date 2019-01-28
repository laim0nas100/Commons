package lt.lb.commons.misc;

/**
 *
 * Left-terminating boolean chaining in a Builder-like fashion. To avoid clunky
 * parenthesis and exclamation marks.
 *
 * @author laim0nas100
 */
public class Bool {

    private final boolean val;

    public Bool(boolean b) {
        val = b;
    }

    public static Bool TRUE() {
        return new Bool(true);
    }

    public static Bool FALSE() {
        return new Bool(false);
    }

    public static Bool AND(boolean... bools) {
        if (bools.length == 0) {
            return Bool.FALSE();
        }
        for (boolean s : bools) {
            if (!s) {
                return new Bool(false);
            }
        }
        return new Bool(true);
    }

    public static Bool AND(boolean b, boolean[] bools) {
        if (b) {
            for (boolean s : bools) {
                if (!s) {
                    return new Bool(false);
                }
            }
        }
        return new Bool(true);
    }

    public static Bool OR(boolean... bools) {
        if (bools.length == 0) {
            return Bool.FALSE();
        }
        for (boolean s : bools) {
            if (s) {
                return new Bool(true);
            }
        }
        return new Bool(false);
    }

    public static Bool OR(boolean b, boolean[] bools) {
        if (b) {
            return new Bool(true);
        }
        for (boolean s : bools) {
            if (s) {
                return new Bool(true);
            }
        }
        return new Bool(false);
    }

    public Bool and(Bool b) {
        return this.and(b.get());
    }

    public Bool and(boolean... b) {
        return Bool.AND(this.get(), b);
    }

    public Bool or(Bool b) {
        return this.or(b.get());
    }

    public Bool or(boolean... b) {
        return Bool.OR(this.get(), b);
    }

    public Bool not() {
        return new Bool(!this.get());
    }

    public Bool xor(boolean b) {
        return new Bool(this.get() != b);
    }

    public Bool xor(boolean... b) {
        Bool all = Bool.AND(b);
        Bool any = Bool.OR(b);
        return all.equals(any).not();
    }

    public Bool xor(Bool b) {
        return this.xor(b.get());
    }

    public Bool nand(Bool b) {
        return this.nand(b.get());
    }

    public Bool nand(boolean... b) {
        return Bool.AND(this.get(), b).not();
    }

    public Bool nor(Bool b) {
        return this.nor(b.get());
    }

    public Bool nor(boolean b) {
        return new Bool(!(this.get() || b));
    }

    public Bool equals(boolean b) {
        return new Bool(this.get() == b);
    }

    public Bool equals(Bool b) {
        return new Bool(this.get()== b.get());
    }

    public Bool notEquals(boolean b) {
        return new Bool(this.get() != b);
    }

    public Bool notEquals(Bool b) {
        return new Bool(this.get() != b.get());
    }

    public Boolean isTrue() {
        return get();
    }

    public Boolean isFalse() {
        return !get();
    }

    private boolean get() {
        return val;
    }

}
