package lt.lb.commons;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.misc.RandomDistribution;

/**
 *
 * Explicit type Lambdas for everybody.
 * Simple way to reference any reasonable function.
 * Supports up to 9 parameters. Optional result type.
 *
 * @author laim0nas100
 */
public class Lambda {

    public static L0 of(L0 l) {
        return l;
    }

    public static <R1> L1<R1> of(L1<R1> l) {
        return l;
    }

    public static <T1, T2> L2<T1, T2> of(L2<T1, T2> l) {
        return l;
    }

    public static <T1, T2, T3> L3<T1, T2, T3> of(L3<T1, T2, T3> l) {
        return l;
    }

    public static <T1, T2, T3, T4> L4<T1, T2, T3, T4> of(L4<T1, T2, T3, T4> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5> L5<T1, T2, T3, T4, T5> of(L5<T1, T2, T3, T4, T5> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5, T6> L6<T1, T2, T3, T4, T5, T6> of(L6<T1, T2, T3, T4, T5, T6> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5, T6, T7> L7<T1, T2, T3, T4, T5, T6, T7> of(L7<T1, T2, T3, T4, T5, T6, T7> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> L8<T1, T2, T3, T4, T5, T6, T7, T8> of(L8<T1, T2, T3, T4, T5, T6, T7, T8> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> L9<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(L9<T1, T2, T3, T4, T5, T6, T7, T8, T9> l) {
        return l;
    }

    public static <T1> L0R<T1> of(L0R<T1> l) {
        return l;
    }

    public static <T1, R1> L1R<T1, R1> of(L1R<T1, R1> l) {
        return l;
    }

    public static <T1, T2, R1> L2R<T1, T2, R1> of(L2R<T1, T2, R1> l) {
        return l;
    }

    public static <T1, T2, T3, R1> L3R<T1, T2, T3, R1> of(L3R<T1, T2, T3, R1> l) {
        return l;
    }

    public static <T1, T2, T3, T4, R1> L4R<T1, T2, T3, T4, R1> of(L4R<T1, T2, T3, T4, R1> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5, R1> L5R<T1, T2, T3, T4, T5, R1> of(L5R<T1, T2, T3, T4, T5, R1> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5, T6, R1> L6R<T1, T2, T3, T4, T5, T6, R1> of(L6R<T1, T2, T3, T4, T5, T6, R1> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R1> L7R<T1, T2, T3, T4, T5, T6, T7, R1> of(L7R<T1, T2, T3, T4, T5, T6, T7, R1> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R1> L8R<T1, T2, T3, T4, T5, T6, T7, T8, R1> of(L8R<T1, T2, T3, T4, T5, T6, T7, T8, R1> l) {
        return l;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R1> L9R<T1, T2, T3, T4, T5, T6, T7, T8, T9, R1> of(L9R<T1, T2, T3, T4, T5, T6, T7, T8, T9, R1> l) {
        return l;
    }

    public interface L0 extends Runnable{

        public default void apply(){
            run();
        }
    }

    public interface L0R<R> extends Supplier<R>{

        public default R apply(){
            return get();
        }
    }

    public interface L1<P1> extends Consumer<P1>{

        public default void apply(P1 p1){
            accept(p1);
        }
    }

    public interface L1R<P1, R> extends Function<P1,R>{
        @Override
        public R apply(P1 p1);
    }

    public interface L2<P1, P2> extends BiConsumer<P1,P2> {

        public default void apply(P1 p1, P2 p2){
            accept(p1,p2);
        }
    }

    public interface L2R<P1, P2, R> extends BiFunction<P1,P2,R>{
        
        @Override
        public R apply(P1 p1, P2 p2);
    }

    public interface L3<P1, P2, P3> {

        public void apply(P1 p1, P2 p2, P3 p3);
    }

    public interface L3R<P1, P2, P3, R> {

        public R apply(P1 p1, P2 p2, P3 p3);
    }

    public interface L4<P1, P2, P3, P4> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    public interface L4R<P1, P2, P3, P4, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    public interface L5<P1, P2, P3, P4, P5> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    public interface L5R<P1, P2, P3, P4, P5, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    public interface L6<P1, P2, P3, P4, P5, P6> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
    }

    public interface L6R<P1, P2, P3, P4, P5, P6, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
    }

    public interface L7<P1, P2, P3, P4, P5, P6, P7> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7);
    }

    public interface L7R<P1, P2, P3, P4, P5, P6, P7, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7);
    }

    public interface L8<P1, P2, P3, P4, P5, P6, P7, P8> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8);
    }

    public interface L8R<P1, P2, P3, P4, P5, P6, P7, P8, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8);
    }

    public interface L9<P1, P2, P3, P4, P5, P6, P7, P8, P9> {

        public void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9);
    }

    public interface L9R<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> {

        public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9);
    }
    
}
