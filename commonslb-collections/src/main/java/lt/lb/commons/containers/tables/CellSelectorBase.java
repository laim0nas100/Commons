package lt.lb.commons.containers.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 */
public abstract class CellSelectorBase implements Predicate<CellPrep> {

    protected CellSelectorBase prev;
    protected List<CellSelectorBase> mergedOR;
    protected List<CellSelectorBase> mergedAND;
    protected Predicate<CellPrep> include;
    protected Predicate<CellPrep> exclude;

    public abstract String id();

    public void includeOR(Predicate<CellPrep> pred) {
        Objects.requireNonNull(pred);
        if (include == null) {
            include = pred;
        } else {
            include = include.or(pred);
        }
    }

    public void includeAND(Predicate<CellPrep> pred) {
        Objects.requireNonNull(pred);
        if (include == null) {
            include = pred;
        } else {
            include = include.and(pred);
        }
    }

    public void excludeOR(Predicate<CellPrep> pred) {
        Objects.requireNonNull(pred);
        if (exclude == null) {
            exclude = pred;
        } else {
            exclude = exclude.or(pred);
        }
    }

    public void excludeAND(Predicate<CellPrep> pred) {
        Objects.requireNonNull(pred);
        if (exclude == null) {
            exclude = pred;
        } else {
            exclude = exclude.and(pred);
        }
    }

    public void mergeAND(CellSelectorBase selector) {
        Objects.requireNonNull(selector);
        boolean changed = false;
        if (selector.include != null) {
            this.includeAND(selector.include);
            changed = true;
        }
        if (selector.exclude != null) {
            changed = true;
            this.excludeAND(selector.exclude);
        }
        if (changed) {
            mergedAND = Nulls.requireNonNullElseGet(mergedAND, ArrayList::new);
            mergedAND.add(selector);
        }
    }

    public void mergeOR(CellSelectorBase selector) {
        Objects.requireNonNull(selector);
        boolean changed = false;
        if (selector.include != null) {
            changed = true;
            this.includeOR(selector.include);
        }
        if (selector.exclude != null) {
            changed = true;
            this.excludeOR(selector.exclude);
        }
        if (changed) {
            mergedOR = Nulls.requireNonNullElseGet(mergedOR, ArrayList::new);
            mergedOR.add(selector);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.prev);
        hash = 37 * hash + Objects.hashCode(this.mergedOR);
        hash = 37 * hash + Objects.hashCode(this.mergedAND);
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
        final CellSelectorBase other = (CellSelectorBase) obj;
        if (!Objects.equals(this.prev, other.prev)) {
            return false;
        }
        if (!Objects.equals(this.mergedOR, other.mergedOR)) {
            return false;
        }
        if (!Objects.equals(this.mergedAND, other.mergedAND)) {
            return false;
        }
        return true;
    }

    private static <P> boolean nullTrue(Predicate<P> pred, P item) {
        return pred == null ? true : pred.test(item);
    }

    private static <P> boolean nullFalse(Predicate<P> pred, P item) {
        return pred == null ? false : pred.test(item);
    }

    private static <P> boolean nullTrueNot(Predicate<P> pred, P item) {
        return pred == null ? true : !pred.test(item);
    }

    public void replaceWith(CellSelectorBase selector) {
        this.include = null;
        this.exclude = null;
        mergeAND(selector);
    }

    @Override
    public boolean test(CellPrep t) {

        if (nullFalse(prev, t)) { // satisfied by parent predicate, no need to check mine.
            return true;
        }
        if (include != null || exclude != null) {
            return (nullTrue(include, t) && nullTrueNot(exclude, t));
        }
        return false;

    }
}
