package lt.lb.commons.containers.tables;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import lt.lb.commons.misc.IntRange;
import lt.lb.fastid.FastID;

/**
 *
 * @author laim0nas100
 */
public class CellSelector extends CellSelectorBase {

    protected Map<String, Object> keys;

    protected Map<String, Object> keys() {
        if (keys == null) {
            keys = new LinkedHashMap<>();
        }
        return keys;
    }

    CellSelector() {
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + super.hashCode();
        hash = 17 * hash + Objects.hashCode(this.keys());
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
        if (!super.equals(obj)) {
            return false;
        }
        final CellSelector other = (CellSelector) obj;
        if (!Objects.equals(this.keys(), other.keys())) {
            return false;
        }
        return true;
    }

    public static CellSelector empty() {
        return new CellSelector();
    }

    public static CellSelector full() {
        CellSelector s = new CellSelector();
        s.keys().put("full", true);
        s.includeAND(p -> true);
        return s;
    }

    public static CellSelector cellsInclude(Set<FastID> set) {
        CellSelector s = new CellSelector();
        s.keys().put("cellsInclude", set);
        s.includeAND(c -> set.contains(c.id));
        return s;
    }

    public static CellSelector cellsExclude(Set<FastID> set) {
        CellSelector s = new CellSelector();
        s.keys().put("cellsExclude", set);
        s.excludeAND(c -> set.contains(c.id));
        return s;
    }

    public static <E> CellSelector cellAt(int row, int col) {
        CellSelector s = new CellSelector();
        s.keys().put("startRow", row);
        s.keys().put("startCol", col);
        s.includeAND(c -> c.colIndex == col && c.rowIndex == row);
        return s;
    }

    public static CellSelector diagonal(int sr, int sc, int er, int ec) {
        IntRange.of(sr, er).assertRangeIsValid().assertRangeSizeAtLeast(1);
        IntRange.of(sc, ec).assertRangeIsValid().assertRangeSizeAtLeast(1);

        CellSelector s = new CellSelector();
        s.keys().put("startRow", sr);
        s.keys().put("startCol", sc);
        s.keys().put("endRow", er);
        s.keys().put("endCol", ec);
        s.includeAND(c -> {
            return (c.colIndex >= sc && c.colIndex <= ec) && (c.rowIndex >= sr && c.rowIndex <= er);
        });
        return s;
    }

    public static CellSelector rows(Integer... rows) {
        if (rows.length == 0) {
            return full();
        }
        CellSelector s = new CellSelector();
        Set<Integer> set = new HashSet<>();
        for (Integer r : rows) {
            Objects.requireNonNull(r);
            set.add(r);
        }
        s.keys().put("rows", set);
        s.includeAND(c -> set.contains(c.rowIndex));
        return s;
    }

    public static CellSelector columns(Integer... cols) {
        if (cols.length == 0) {
            return full();
        }
        CellSelector s = new CellSelector();
        Set<Integer> set = new HashSet<>();
        for (Integer r : cols) {
            Objects.requireNonNull(r);
            set.add(r);
        }
        s.keys().put("cols", set);
        s.includeAND(c -> set.contains(c.colIndex));

        return s;
    }

    public static CellSelector ofPredicate(String predName, Predicate<CellPrep> pred) {
        Objects.requireNonNull(pred);
        CellSelector s = new CellSelector();
        s.keys().put("pred-" + predName, pred);
        s.includeAND(c -> pred.test(c));
        return s;
    }

    @Override
    public String id() {
        return String.valueOf(keys());
    }

}
