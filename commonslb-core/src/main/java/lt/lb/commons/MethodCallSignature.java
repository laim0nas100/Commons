package lt.lb.commons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class MethodCallSignature {

    /**
     * name of the method call Not null.
     */
    public final String name;
    /**
     * Arguments used.
     */
    public final List args;

    /**
     * Concrete call
     *
     * @param name
     * @param args
     */
    public MethodCallSignature(String name, Object... args) {
        this.name = Objects.requireNonNull(name, "Method name must be provided");
        this.args = args.length == 0 ? Collections.EMPTY_LIST : Collections.unmodifiableList(Arrays.asList(args));
    }

    protected int hash = 0;

    @Override
    public int hashCode() {
        if (hash == 0) {// in unlikely case that the computed hash is 0, recompute everytime
            int h = 7;
            h = 11 * h + Objects.hashCode(this.name);
            h = 11 * h + Objects.hashCode(this.args);
            hash = h;
        }

        return hash;
    }

    private static boolean argEquals(List a, List b) {
        if (a == b) {// both empty
            return true;
        }
        if (a.size() != b.size()) {
            return false;
        }
        //compare reference
        int size = a.size();
        for (int i = 0; i < size; i++) {
            if (a.get(i) != b.get(i)) {
                return false;
            }
        }
        return true;

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
        final MethodCallSignature other = (MethodCallSignature) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return argEquals(this.args, other.args);
    }

    @Override
    public String toString() {

        LineStringBuilder sb = new LineStringBuilder();

        sb.append(name).append("(");
        if (!args.isEmpty()) {
            for (Object o : args) {
                sb.append(String.valueOf(o)).append(",");
            }
            sb.removeFromEnd(1);
        }

        return sb.append(")").toString();
    }

}
