package lt.lb.commons.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;

/**
 *
 * @author laim0nas100
 */
public class NestingHelper {

    public static class N<K, T> {

        protected K key;
        protected T val;
        protected List<N<K, T>> nested = new ArrayList<>();

        public N(T b) {
            val = b;
        }

        public N(K k, T b) {
            val = b;
            key = k;
        }

        public static <K, T> N<K, T> ofk(K k, T first, T... vals) {
            T[] array = ArrayOp.addAt(vals, 0, first);
            N<K, T> n = new N<>(k, array[0]);
            for (int i = 1; i < array.length; i++) {
                n.nested.add(new N<>(array[i]));
            }
            return n;
        }

        public static <K, T> N<K, T> ofk(K k, T val, N<K, T>... vals) {
            N<K, T> n = new N<>(k, val);
            for (N<K, T> t : vals) {
                n.nested.add(t);
            }
            return n;
        }

        public static <K, T> N<K, T> of(T val, N<K, T>... vals) {
            return ofk(null, val, vals);
        }

        public static <K, T> N<K, T> of(T first, T... vals) {
            return ofk(null, first, vals);
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public List<N<K, T>> getNested() {
            return nested;
        }

        public void setNested(List<N<K, T>> nested) {
            this.nested = nested;
        }

        public T getVal() {
            return val;
        }

        public void setVal(T val) {
            this.val = val;
        }

        public ArrayList<N<K, T>> collectLeafs() {
            ArrayList<N<K, T>> collector = new ArrayList<>();
            TreeVisitor.of(
                    (N<K, T> item) -> {
                        if (item.getNested().isEmpty()) {
                            collector.add(item);
                        }
                        return false;
                    },
                    (N<K, T> item) -> ReadOnlyIterator.of(item.getNested()))
                    .DFSIterative(this);
            return collector;
        }

        @Override
        public String toString() {
            String k = Optional.ofNullable(key).map(m -> m + "=").orElse("");
            return k + val;
        }

        public String toNestedString() {
            return this.toNestedString("");
        }

        private String toNestedString(String buf) {
            String str = buf + val + " \n";
            for (N<K, T> n : nested) {
                str += n.toNestedString(buf + "..");
            }
            return str;
        }
    }
}
