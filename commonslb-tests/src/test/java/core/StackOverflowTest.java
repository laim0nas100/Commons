/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import lt.lb.commons.CallOrResult;
import lt.lb.commons.Log;
import lt.lb.commons.misc.ExtComparator;

/**
 *
 * @author laim0nas100
 */
public class StackOverflowTest {

    public abstract static class RecursionChain<T> implements Callable<T> {

        public RecursionChain next;

    }

    public static class RecursionBuilder {

        public static Integer ok(Integer n1, Integer n2) {
            if (n1 <= n2) {
                if (n1 % 1000 == 0) {
                    Log.print(n1);
                }

                return ok(n1 + 1, n2);
            } else {
                return 0;
            }

        }

        public static CallOrResult<Integer> okCall(Integer n1, Integer n2) {
            if (n1 <= n2) {
                if (n1 % 1000 == 0) {
                    Log.print(n1);
                }
                return new CallOrResult(() -> okCall(1 + n1, n2));
            } else {
                return new CallOrResult(0);
            }
        }

        public static BigInteger fibb(BigInteger f1, BigInteger f2, BigInteger limit) {
            ExtComparator<BigInteger> cmp = ExtComparator.ofComparable();
            BigInteger add = f1.add(f2);
            if (cmp.greaterThan(f1, limit)) {
                return f1;
            } else {
                return fibb(add, f1, limit);
            }
        }

        public static BigInteger fibb2(long seq) {
            if (seq == 0) {
                return BigInteger.ZERO;
            }
            if (seq == 1) {
                return BigInteger.ONE;
            }
            return fibb2(seq - 1).add(fibb2(seq - 2));
        }
        
        public static CallerTest<BigInteger> fibb2Caller(long seq){
            if (seq == 0) {
                return new CallerTest<>(BigInteger.ZERO);
            }
            if (seq == 1) {
                return new CallerTest<>(BigInteger.ONE);
            }
            CallerTest<BigInteger> dep1 = new CallerTest<>(args->fibb2Caller(seq-1));
            CallerTest<BigInteger> dep2 = new CallerTest<>(args->fibb2Caller(seq-2));
            return new CallerTest<>((args) -> new CallerTest<>(args.get(0).add(args.get(1))),dep1,dep2);
        }

        public static CallOrResult<BigInteger> fibbCall(BigInteger f1, BigInteger f2, BigInteger limit) {
            ExtComparator<BigInteger> cmp = ExtComparator.ofComparable();
            BigInteger add = f1.add(f2);
            if (cmp.greaterThan(f1, limit)) {
                return CallOrResult.returnValue(f1);
            } else {
                return CallOrResult.returnIntermediate(f1, () -> fibbCall(add, f1, limit));
            }
        }

        public static BigInteger ackermann(BigInteger m, BigInteger n) {
            if (m.equals(BigInteger.ZERO)) {
                return n.add(BigInteger.ONE);
            }
            ExtComparator<BigInteger> cmp = ExtComparator.ofComparable();
            if (cmp.greaterThan(m, BigInteger.ZERO) && cmp.equals(n, BigInteger.ZERO)) {
                return ackermann(m.subtract(BigInteger.ONE), BigInteger.ONE);
            }

            if (cmp.greaterThan(m, BigInteger.ZERO) && cmp.greaterThan(n, BigInteger.ZERO)) {
                return ackermann(m.subtract(BigInteger.ONE), ackermann(m, n.subtract(BigInteger.ONE)));
            }
            throw new IllegalStateException();

        }

        public static CallOrResult<BigInteger> ackermannCall(BigInteger m, BigInteger n) {
            if (m.equals(BigInteger.ZERO)) {
                return CallOrResult.returnValue(n.add(BigInteger.ONE));
            }
            ExtComparator<BigInteger> cmp = ExtComparator.ofComparable();
            if (cmp.greaterThan(m, BigInteger.ZERO) && cmp.equals(n, BigInteger.ZERO)) {
                return CallOrResult.returnCall(() -> ackermannCall(m.subtract(BigInteger.ONE), BigInteger.ONE));
            }

            if (cmp.greaterThan(m, BigInteger.ZERO) && cmp.greaterThan(n, BigInteger.ZERO)) {
                return CallOrResult.returnCall(() -> ackermannCall(m.subtract(BigInteger.ONE), CallOrResult.iterative(ackermannCall(m, n.subtract(BigInteger.ONE)))));
            }
            throw new IllegalStateException();

        }

        public static CallerTest<BigInteger> fibbCaller(BigInteger f1, BigInteger f2, BigInteger limit) {
            ExtComparator<BigInteger> cmp = ExtComparator.ofComparable();
            BigInteger add = f1.add(f2);
            if (cmp.greaterThan(f1, limit)) {
                return new CallerTest<>(f1);
            } else {
                return new CallerTest<>((args) -> fibbCaller(add, f1, limit));
            }
        }

        public static CallerTest<BigInteger> ackermannCaller(BigInteger m, BigInteger n) {
            if (m.equals(BigInteger.ZERO)) {
                return new CallerTest<>(n.add(BigInteger.ONE));
            }
            ExtComparator<BigInteger> cmp = ExtComparator.ofComparable();
            if (cmp.greaterThan(m, BigInteger.ZERO) && cmp.equals(n, BigInteger.ZERO)) {
                return new CallerTest<>(a -> ackermannCaller(m.subtract(BigInteger.ONE), BigInteger.ONE));
            }

            if (cmp.greaterThan(m, BigInteger.ZERO) && cmp.greaterThan(n, BigInteger.ZERO)) {
                CallerTest<BigInteger> dep = new CallerTest((args) -> ackermannCaller(m, n.subtract(BigInteger.ONE)));
                return new CallerTest<>((args) -> ackermannCaller(m.subtract(BigInteger.ONE), args.get(0)), dep);
            }
            throw new IllegalStateException();
        }

        public static CallerTest<Long> ackermannCaller(long m, long n) {
            if (m == 0) {
                return new CallerTest<>(n + 1);
            }
            ExtComparator<Long> cmp = ExtComparator.ofComparable();
            if (cmp.greaterThan(m, 0L) && cmp.equals(n, 0L)) {
                return new CallerTest<>(args -> ackermannCaller(m - 1, 1L));
            }

            if (cmp.greaterThan(m, 0L) && cmp.greaterThan(n, 0L)) {
                CallerTest<Long> dep = new CallerTest((args) -> ackermannCaller(m, n - 1));
                return new CallerTest<>(args -> ackermannCaller(m - 1, args.get(0)), dep);
            }
            throw new IllegalStateException();
        }
    }

    public static void main(String... args) throws Exception {
        Log.main().async = false;
//        CallOrResult<Integer> okCall = RecursionBuilder.okCall(1, 200000);
//        RecursionBuilder.iterative(okCall);
        BigInteger big = BigInteger.valueOf(100000000);
        Log.print(CallOrResult.iterative(RecursionBuilder.fibbCall(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(999))));
        BigInteger m = BigInteger.valueOf(3);
        BigInteger n = BigInteger.valueOf(8);
//        Log.print(RecursionBuilder.ackermann(m, n));
//        Log.print(CallOrResult.iterative(RecursionBuilder.ackermannCall(m, n)));
        Log.print(RecursionBuilder.fibb2(40));
        Log.print(RecursionBuilder.fibb2Caller(40).resolve());


        Log.print("############");
//        CallerTest<BigInteger> fibbCaller = RecursionBuilder.fibbCaller(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(999));
//        BigInteger resolve = fibbCaller.resolve();
//        Log.print(resolve);

//        CallerTest<Long> ackCaller = RecursionBuilder.ackermannCaller(3L, 5L);
//        Long resolved = ackCaller.resolve();
//        Log.print(resolved);
//        Log.print(RecursionBuilder.ackermann(m, n));
//        Log.print(CallOrResult.iterative(RecursionBuilder.ackermannCall(m, n)));
//        Log.print(RecursionBuilder.fibb(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(999)));
        Log.print("End");
        Log.close();
//        RecursionBuilder.okCall(0, 8000);
    }

}
