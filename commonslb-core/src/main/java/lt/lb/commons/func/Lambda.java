package lt.lb.commons.func;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.containers.values.Value;

/**
 *
 * Explicit type Lambdas for everybody. Simple way to reference any reasonable
 * function. Supports up to 9 parameters. Optional result type.
 *
 * @author laim0nas100
 */
public abstract class Lambda {

    public static L0 of(L0... l) {
        return () -> {
            Stream.of(l).forEach(lam ->{
                lam.apply();
            });
        };
    }

    public static <T> L1<T> of(L1<T>... l) {
        return (p) -> {
             Stream.of(l).forEach(lam ->{
                lam.apply(p);
            });
        };
    }

    public static <T1, T2> L2<T1, T2> of(L2<T1, T2>... l) {
        return (p1, p2) -> {
             Stream.of(l).forEach(lam ->{
                lam.apply(p1, p2);
            });
        };
    }

    public static <T1, T2, T3> L3<T1, T2, T3> of(L3<T1, T2, T3>... l) {
        return (p1, p2, p3) -> {
           Stream.of(l).forEach(lam ->{
                lam.apply(p1, p2, p3);
            });
        };
    }

    public static <T1, T2, T3, T4> L4<T1, T2, T3, T4> of(L4<T1, T2, T3, T4>... l) {
        return (p1, p2, p3, p4) -> {
           Stream.of(l).forEach(lam ->{
                lam.apply(p1, p2, p3, p4);
            });
        };
    }

    public static <T1, T2, T3, T4, T5> L5<T1, T2, T3, T4, T5> of(L5<T1, T2, T3, T4, T5>... l) {
        return (p1, p2, p3, p4, p5) -> {
           Stream.of(l).forEach(lam ->{
                lam.apply(p1, p2, p3, p4, p5);
            });
        };
    }

    public static <T1, T2, T3, T4, T5, T6> L6<T1, T2, T3, T4, T5, T6> of(L6<T1, T2, T3, T4, T5, T6>... l) {
        return (p1, p2, p3, p4, p5, p6) -> {
           Stream.of(l).forEach(lam ->{
                lam.apply(p1, p2, p3, p4, p5, p6);
            });
        };
    }

    public static <T1, T2, T3, T4, T5, T6, T7> L7<T1, T2, T3, T4, T5, T6, T7> of(L7<T1, T2, T3, T4, T5, T6, T7>... l) {
        return (p1, p2, p3, p4, p5, p6, p7) -> {
           Stream.of(l).forEach(lam ->{
                lam.apply(p1, p2, p3, p4, p5, p6, p7);
            });
        };
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> L8<T1, T2, T3, T4, T5, T6, T7, T8> of(L8<T1, T2, T3, T4, T5, T6, T7, T8>... l) {
        return (p1, p2, p3, p4, p5, p6, p7, p8) -> {
           Stream.of(l).forEach(lam ->{
                lam.apply(p1, p2, p3, p4, p5, p6, p7, p8);
            });
        };
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> L9<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(L9<T1, T2, T3, T4, T5, T6, T7, T8, T9>... l) {
        return (p1, p2, p3, p4, p5, p6, p7, p8, p9) -> {
           Stream.of(l).forEach(lam ->{
                lam.apply(p1, p2, p3, p4, p5, p6, p7, p8, p9);
            });
        };
    }

    public static <R1> L0R<R1> of(L0R<R1>... l) {
        return () -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply());
            });
            return val.get();
        };
    }

    public static <T1, R1> L1R<T1, R1> of(L1R<T1, R1>... l) {
        return (p1) -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply(p1));
            });
            return val.get();
        };
    }

    public static <T1, T2, R1> L2R<T1, T2, R1> of(L2R<T1, T2, R1>... l) {
        return (p1, p2) -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply(p1, p2));
            });
            return val.get();
        };
    }

    public static <T1, T2, T3, R1> L3R<T1, T2, T3, R1> of(L3R<T1, T2, T3, R1>... l) {
        return (p1, p2, p3) -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply(p1, p2, p3));
            });
            return val.get();
        };
    }

    public static <T1, T2, T3, T4, R1> L4R<T1, T2, T3, T4, R1> of(L4R<T1, T2, T3, T4, R1>... l) {
        return (p1, p2, p3, p4) -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply(p1, p2, p3, p4));
            });
            return val.get();
        };
    }

    public static <T1, T2, T3, T4, T5, R1> L5R<T1, T2, T3, T4, T5, R1> of(L5R<T1, T2, T3, T4, T5, R1>... l) {
        return (p1, p2, p3, p4, p5) -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply(p1, p2, p3, p4, p5));
            });
            return val.get();
        };
    }

    public static <T1, T2, T3, T4, T5, T6, R1> L6R<T1, T2, T3, T4, T5, T6, R1> of(L6R<T1, T2, T3, T4, T5, T6, R1>... l) {
        return (p1, p2, p3, p4, p5, p6) -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply(p1, p2, p3, p4, p5, p6));
            });
            return val.get();
        };
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R1> L7R<T1, T2, T3, T4, T5, T6, T7, R1> of(L7R<T1, T2, T3, T4, T5, T6, T7, R1>... l) {
        return (p1, p2, p3, p4, p5, p6, p7) -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply(p1, p2, p3, p4, p5, p6, p7));
            });
            return val.get();
        };
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R1> L8R<T1, T2, T3, T4, T5, T6, T7, T8, R1> of(L8R<T1, T2, T3, T4, T5, T6, T7, T8, R1>... l) {
        return (p1, p2, p3, p4, p5, p6, p7, p8) -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply(p1, p2, p3, p4, p5, p6, p7, p8));
            });
            return val.get();
        };
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R1> L9R<T1, T2, T3, T4, T5, T6, T7, T8, T9, R1> of(L9R<T1, T2, T3, T4, T5, T6, T7, T8, T9, R1>... l) {
        return (p1, p2, p3, p4, p5, p6, p7, p8, p9) -> {
            Value<R1> val = new Value<>();
           Stream.of(l).forEach(lam ->{
                val.set(lam.apply(p1, p2, p3, p4, p5, p6, p7, p8, p9));
            });
            return val.get();
        };
    }


    /*
     * For one liners, with type inference
     * 
     */
    public static <T1> L0R<T1> ofResult(L0R<T1>... l) {
        return of(l);
    }

    public static <T1, R1> L1R<T1, R1> ofResult(L1R<T1, R1>... l) {
        return of(l);
    }

    public static <T1, T2, R1> L2R<T1, T2, R1> ofResult(L2R<T1, T2, R1>... l) {
        return of(l);
    }

    public static <T1, T2, T3, R1> L3R<T1, T2, T3, R1> ofResult(L3R<T1, T2, T3, R1>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, R1> L4R<T1, T2, T3, T4, R1> ofResult(L4R<T1, T2, T3, T4, R1>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5, R1> L5R<T1, T2, T3, T4, T5, R1> ofResult(L5R<T1, T2, T3, T4, T5, R1>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5, T6, R1> L6R<T1, T2, T3, T4, T5, T6, R1> ofResult(L6R<T1, T2, T3, T4, T5, T6, R1>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R1> L7R<T1, T2, T3, T4, T5, T6, T7, R1> ofResult(L7R<T1, T2, T3, T4, T5, T6, T7, R1>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R1> L8R<T1, T2, T3, T4, T5, T6, T7, T8, R1> ofResult(L8R<T1, T2, T3, T4, T5, T6, T7, T8, R1>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R1> L9R<T1, T2, T3, T4, T5, T6, T7, T8, T9, R1> ofResult(L9R<T1, T2, T3, T4, T5, T6, T7, T8, T9, R1>... l) {
        return of(l);
    }

    /*
     * For one liners, with type inference
     * 
     */
    public static <R1> L1<R1> ofVoid(L1<R1>... l) {
        return of(l);
    }

    public static <T1, T2> L2<T1, T2> ofVoid(L2<T1, T2>... l) {
        return of(l);
    }

    public static <T1, T2, T3> L3<T1, T2, T3> ofVoid(L3<T1, T2, T3>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4> L4<T1, T2, T3, T4> ofVoid(L4<T1, T2, T3, T4>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5> L5<T1, T2, T3, T4, T5> ofVoid(L5<T1, T2, T3, T4, T5>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5, T6> L6<T1, T2, T3, T4, T5, T6> ofVoid(L6<T1, T2, T3, T4, T5, T6>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> L7<T1, T2, T3, T4, T5, T6, T7> ofVoid(L7<T1, T2, T3, T4, T5, T6, T7>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> L8<T1, T2, T3, T4, T5, T6, T7, T8> ofVoid(L8<T1, T2, T3, T4, T5, T6, T7, T8>... l) {
        return of(l);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> L9<T1, T2, T3, T4, T5, T6, T7, T8, T9> ofVoid(L9<T1, T2, T3, T4, T5, T6, T7, T8, T9>... l) {
        return of(l);
    }

    /**
     * Lambda with no parameters and no result
     */
    public interface L0 extends Runnable {

        public default void apply() {
            run();
        }

        public static L0 empty() {
            return () -> {
            };
        }
    }

    /**
     * Lambda with no parameters and result
     */
    public interface L0R<R> extends Supplier<R> {

        public default R apply() {
            return get();
        }

        public static <R> L0R<R> empty() {
            return () -> null;
        }
    }

    /**
     * Lambda with 1 parameter and no result
     */
    public interface L1<P1> extends Consumer<P1> {

        public default void apply(P1 p1) {
            accept(p1);
        }

        public static <P1> L1<P1> empty() {
            return (p1) -> {
            };
        }
    }

    /**
     * Lambda with 1 parameter and result
     */
    public interface L1R<P1, R> extends Function<P1, R> {

        @Override
        public R apply(P1 p1);

        public static <P1, R> L1R<P1, R> empty() {
            return (p1) -> null;
        }
    }

    /**
     * Lambda with 2 parameters and no result
     */
    public interface L2<P1, P2> extends BiConsumer<P1, P2> {

        public default void apply(P1 p1, P2 p2) {
            accept(p1, p2);
        }

        public static <P1, P2> L2<P1, P2> empty() {
            return (p1, p2) -> {
            };
        }

    }

    /**
     * Lambda with 2 parameters and result
     */
    public interface L2R<P1, P2, R> extends BiFunction<P1, P2, R> {

        @Override
        public R apply(P1 p1, P2 p2);

        public static <P1, P2, R> L2R<P1, P2, R> empty() {
            return (p1, p2) -> null;
        }
    }

    /**
     * Lambda with 3 parameters and no result
     */
    public interface L3<P1, P2, P3> {

        public void apply(P1 p1, P2 p2, P3 p3);

        public static <P1, P2, P3> L3<P1, P2, P3> empty() {
            return (p1, p2, p3) -> {
            };
        }
    }

    /**
     * Lambda with 3 parameters and result
     */
    public interface L3R<P1, P2, P3, R> {

        public R apply(P1 p1, P2 p2, P3 p3);

        public static <P1, P2, P3, R> L3R<P1, P2, P3, R> empty() {
            return (p1, p2, p3) -> null;
        }
    }

    /**
     * Lambda with 4 parameters and no result
     */
    public interface L4<P1, P2, P3, P4> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4);

        public static <P1, P2, P3, P4> L4<P1, P2, P3, P4> empty() {
            return (p1, p2, p3, p4) -> {
            };
        }
    }

    /**
     * Lambda with 4 parameters and result
     */
    public interface L4R<P1, P2, P3, P4, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4);

        public static <P1, P2, P3, P4, R> L4R<P1, P2, P3, P4, R> empty() {
            return (p1, p2, p3, p4) -> null;
        }
    }

    /**
     * Lambda with 5 parameters and no result
     */
    public interface L5<P1, P2, P3, P4, P5> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);

        public static <P1, P2, P3, P4, P5> L5<P1, P2, P3, P4, P5> empty() {
            return (p1, p2, p3, p4, p5) -> {
            };
        }
    }

    /**
     * Lambda with 5 parameters and result
     */
    public interface L5R<P1, P2, P3, P4, P5, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);

        public static <P1, P2, P3, P4, P5, R> L5R<P1, P2, P3, P4, P5, R> empty() {
            return (p1, p2, p3, p4, p5) -> null;
        }
    }

    /**
     * Lambda with 6 parameters and no result
     */
    public interface L6<P1, P2, P3, P4, P5, P6> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
        
        public static <P1, P2, P3, P4, P5, P6> L6<P1, P2, P3, P4, P5, P6> empty() {
            return (p1, p2, p3, p4, p5, p6) -> {
            };
        }
    }

    /**
     * Lambda with 6 parameters and result
     */
    public interface L6R<P1, P2, P3, P4, P5, P6, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);

        public static <P1, P2, P3, P4, P5, P6, R> L6R<P1, P2, P3, P4, P5, P6, R> empty() {
            return (p1, p2, p3, p4, p5, p6) -> null;
        }
    }

    /**
     * Lambda with 7 parameters and no result
     */
    public interface L7<P1, P2, P3, P4, P5, P6, P7> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7);
        
        public static <P1, P2, P3, P4, P5, P6, P7> L7<P1, P2, P3, P4, P5, P6, P7> empty() {
            return (p1, p2, p3, p4, p5, p6, p7) -> {
            };
        }
    }

    /**
     * Lambda with 7 parameters and result
     */
    public interface L7R<P1, P2, P3, P4, P5, P6, P7, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7);

        public static <P1, P2, P3, P4, P5, P6, P7, R> L7R<P1, P2, P3, P4, P5, P6, P7, R> empty() {
            return (p1, p2, p3, p4, p5, p6, p7) -> null;
        }
    }

    /**
     * Lambda with 8 parameters and no result
     */
    public interface L8<P1, P2, P3, P4, P5, P6, P7, P8> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8);
        
        public static <P1, P2, P3, P4, P5, P6, P7, P8> L8<P1, P2, P3, P4, P5, P6, P7, P8> empty() {
            return (p1, p2, p3, p4, p5, p6, p7, p8) -> {
            };
        }

    }

    /**
     * Lambda with 8 parameters and result
     */
    public interface L8R<P1, P2, P3, P4, P5, P6, P7, P8, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8);

        public static <P1, P2, P3, P4, P5, P6, P7, P8, R> L8R<P1, P2, P3, P4, P5, P6, P7, P8, R> empty() {
            return (p1, p2, p3, p4, p5, p6, p7, p8) -> null;
        }
    }

    /**
     * Lambda with 9 parameters and no result
     */
    public interface L9<P1, P2, P3, P4, P5, P6, P7, P8, P9> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9);
        
        public static <P1, P2, P3, P4, P5, P6, P7, P8, P9> L9<P1, P2, P3, P4, P5, P6, P7, P8, P9> empty() {
            return (p1, p2, p3, p4, p5, p6, p7, p8, p9) -> {
            };
        }
    }

    /**
     * Lambda with 9 parameters and result
     */
    public interface L9R<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9);

        public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R> L9R<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> empty() {
            return (p1, p2, p3, p4, p5, p6, p7, p8, p9) -> null;
        }
    }

    /**
     * Lambda with 2 parameters of same type and no result
     *
     * @param <T> parameter type
     */
    public interface L2S<T> extends L2<T, T> {
    }

    /**
     * Lambda with 3 parameters of same type and no result
     *
     * @param <T> parameter type
     */
    public interface L3S<T> extends L3<T, T, T> {
    }

    /**
     * Lambda with 4 parameters of same type and no result
     *
     * @param <T> parameter type
     */
    public interface L4S<T> extends L4<T, T, T, T> {
    }

    /**
     * Lambda with 5 parameters of same type and no result
     *
     * @param <T> parameter type
     */
    public interface L5S<T> extends L5<T, T, T, T, T> {
    }

    /**
     * Lambda with 6 parameters of same type and no result
     *
     * @param <T> parameter type
     */
    public interface L6S<T> extends L6<T, T, T, T, T, T> {
    }

    /**
     * Lambda with 7 parameters of same type and no result
     *
     * @param <T> parameter type
     */
    public interface L7S<T> extends L7<T, T, T, T, T, T, T> {
    }

    /**
     * Lambda with 8 parameters of same type and no result
     *
     * @param <T> parameter type
     */
    public interface L8S<T> extends L8<T, T, T, T, T, T, T, T> {
    }

    /**
     * Lambda with 9 parameters of same type and no result
     *
     * @param <T> parameter type
     */
    public interface L9S<T> extends L9<T, T, T, T, T, T, T, T, T> {
    }

    /**
     * Lambda with 2 parameters of same type and result
     *
     * @param <T> parameter type
     * @param <R> result type
     */
    public interface L2SR<T, R> extends L2R<T, T, R> {
    }

    /**
     * Lambda with 3 parameters of same type and result
     *
     * @param <T> parameter type
     * @param <R> result type
     */
    public interface L3SR<T, R> extends L3R<T, T, T, R> {
    }

    /**
     * Lambda with 4 parameters of same type and result
     *
     * @param <T> parameter type
     * @param <R> result type
     */
    public interface L4SR<T, R> extends L4R<T, T, T, T, R> {
    }

    /**
     * Lambda with 5 parameters of same type and result
     *
     * @param <T> parameter type
     * @param <R> result type
     */
    public interface L5SR<T, R> extends L5R<T, T, T, T, T, R> {
    }

    /**
     * Lambda with 6 parameters of same type and result
     *
     * @param <T> parameter type
     * @param <R> result type
     */
    public interface L6SR<T, R> extends L6R<T, T, T, T, T, T, R> {
    }

    /**
     * Lambda with 7 parameters of same type and result
     *
     * @param <T> parameter type
     * @param <R> result type
     */
    public interface L7SR<T, R> extends L7R<T, T, T, T, T, T, T, R> {
    }

    /**
     * Lambda with 8 parameters of same type and result
     *
     * @param <T> parameter type
     * @param <R> result type
     */
    public interface L8SR<T, R> extends L8R<T, T, T, T, T, T, T, T, R> {
    }

    /**
     * Lambda with 9 parameters of same type and result
     *
     * @param <T> parameter type
     * @param <R> result type
     */
    public interface L9SR<T, R> extends L9R<T, T, T, T, T, T, T, T, T, R> {
    }

    /**
     * Lambda with 1 parameter and result of same type
     *
     * @param <T> parameter and result type
     */
    public interface L1RS<T> extends L1R<T, T> {
    }

    /**
     * Lambda with 2 parameters and result of same type
     *
     * @param <T> parameter and result type
     */
    public interface L2RS<T> extends L2SR<T, T> {
    }

    /**
     * Lambda with 3 parameters and result of same type
     *
     * @param <T> parameter and result type
     */
    public interface L3RS<T> extends L3SR<T, T> {
    }

    /**
     * Lambda with 4 parameters and result of same type
     *
     * @param <T> parameter and result type
     */
    public interface L4RS<T> extends L4SR<T, T> {
    }

    /**
     * Lambda with 5 parameters and result of same type
     *
     * @param <T> parameter and result type
     */
    public interface L5RS<T> extends L5SR<T, T> {
    }

    /**
     * Lambda with 6 parameters and result of same type
     *
     * @param <T> parameter and result type
     */
    public interface L6RS<T> extends L6SR<T, T> {
    }

    /**
     * Lambda with 7 parameters and result of same type
     *
     * @param <T> parameter and result type
     */
    public interface L7RS<T> extends L7SR<T, T> {
    }

    /**
     * Lambda with 8 parameters and result of same type
     *
     * @param <T> parameter and result type
     */
    public interface L8RS<T> extends L8SR<T, T> {
    }

    /**
     * Lambda with 9 parameters and result of same type
     *
     * @param <T> parameter and result type
     */
    public interface L9RS<T> extends L9SR<T, T> {
    }

}
