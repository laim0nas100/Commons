/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

import java.util.Comparator;

/**
 *
 * @author Lemmin
 */
public interface ExtComparator<T> extends Comparator<T> {

    /**
     *
     * @param o1
     * @param o2
     * @return think of it like: o1 &lt o2
     */
    public default boolean lessThan(T o1, T o2) {
        return this.compare(o1, o2) < 0;
    }

    /**
     *
     * @param o1
     * @param o2
     * @return think of it like: o1 &gt o2
     */
    public default boolean greaterThan(T o1, T o2) {
        return this.compare(o1, o2) > 0;
    }

    public static <F> ExtComparator<F> of(Comparator<F> cmp) {
        return (F o1, F o2) -> cmp.compare(o1, o2);
    }

    public static <F extends Comparable> ExtComparator<F> ofComparable() {
        return (F o1, F o2) -> {
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                }
                return -1 * o2.compareTo(o1);
            }

            return o1.compareTo(o2);
        };

    }
    
    public default Comparable<T> asComparable(T obj){
        return (T o) -> this.compare(obj, o);
    }

}
