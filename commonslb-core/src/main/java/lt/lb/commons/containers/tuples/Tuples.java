package lt.lb.commons.containers.tuples;

/**
 *
 * @author laim0nas100
 */
public class Tuples {

    public static <T1, T2> Tuple<T1, T2> create(T1 t1, T2 t2) {
        return new Tuple<>(t1, t2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> create(T1 t1, T2 t2, T3 t3) {
        return new Tuple3<>(t1, t2, t3);
    }
    
    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> create(T1 t1, T2 t2, T3 t3, T4 t4) {
        return new Tuple4<>(t1, t2, t3, t4);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 t1, Tuple<T2, T3> tup) {
        return create(t1, tup.g1, tup.g2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> of(Tuple<T1, T2> tup, T3 t3) {
        return create(tup.g1, tup.g2, t3);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 t1, T2 t2, Tuple<T3, T4> tup1) {
        return create(t1, t2, tup1.g1, tup1.g2);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 t1, Tuple<T2, T3> tup1, T4 t4) {
        return create(t1, tup1.g1, tup1.g2, t4);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(Tuple<T1, T2> tup1, T3 t3, T4 t4) {
        return create(tup1.g1, tup1.g2, t3, t4);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(Tuple<T1, T2> tup1, Tuple<T3, T4> tup2) {
        return create(tup1.g1, tup1.g2, tup2.g1, tup2.g2);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 t1, Tuple3<T2, T3, T4> tup) {
        return create(t1, tup.g1, tup.g2, tup.g3);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(Tuple3<T1, T2, T3> tup, T4 t4) {
        return create(tup.g1, tup.g2, tup.g3, t4);
    }

}
