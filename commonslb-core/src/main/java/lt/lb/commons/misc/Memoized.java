package lt.lb.commons.misc;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.func.Lambda.L1R;
import lt.lb.commons.func.Lambda.L2R;
import lt.lb.commons.func.Lambda.L3R;

/**
 *
 * @author laim0nas100
 */
public class Memoized {

    private Map<Object, Map<MemArray, Object>> mem = new ConcurrentHashMap<>();
    private Supplier<Map<MemArray, Object>> supp = () -> new ConcurrentHashMap<>();

    private static class MemArray {

        Object[] array;

        MemArray(Object... objects) {
            array = objects;
        }

        static MemArray of(Object... objects) {
            return new MemArray(objects);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Arrays.deepHashCode(this.array);
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
            final MemArray other = (MemArray) obj;
            if (!Arrays.deepEquals(this.array, other.array)) {
                return false;
            }
            return true;
        }

        public <R> R get(int i) {
            return (R) array[i];
        }

    }

    private interface AnyFunc extends Function<MemArray, Object> {
        
        default <P1, R> L1R<P1, R> toTyped1() {
            return (p) -> (R) this.apply(MemArray.of(p));
        }

        default <P1, P2, R> L2R<P1, P2, R> toTyped2() {
            return (p1, p2) -> (R) this.apply(MemArray.of(p1, p2));
        }

        default <P1, P2, P3, R> L3R<P1, P2, P3, R> toTyped3() {
            return (p1, p2, p3) -> (R) this.apply(MemArray.of(p1, p2, p3));
        }
        
        
    }

    public Memoized() {
    }

    public void clear() {
        mem.clear();
    }

    private AnyFunc memoizeAny(Object realFunc, AnyFunc func) {
        return (memArray) -> {
            Map<MemArray, Object> functionMap = mem.computeIfAbsent(realFunc, key -> supp.get());
            return functionMap.computeIfAbsent(memArray, func);
        };
    }
    
    public <R> L1R<Object[],R> memoizeGeneric(Object key, Function<Object[],R> func){
        return memoizeAny(func, p -> func.apply(p.array)).toTyped1();
    }

    public <P1, R> L1R<P1, R> memoize(L1R<P1, R> func) {
        return memoizeAny(func, p -> func.apply(p.get(0))).toTyped1();
    }

    public <P1, P2, R> L2R<P1, P2, R> memoize(L2R<P1, P2, R> func) {
        return memoizeAny(func, p -> func.apply(p.get(0), p.get(1))).toTyped2();
    }

    public <P1, P2, P3, R> L3R<P1, P2, P3, R> memoize(L3R<P1, P2, P3, R> func) {
        return memoizeAny(func, p -> func.apply(p.get(0), p.get(1), p.get(2))).toTyped3();
    }

}
