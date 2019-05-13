/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import lt.lb.commons.CallOrResult;
import lt.lb.commons.F;
import lt.lb.commons.Lambda;
import lt.lb.commons.Log;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.misc.NestedException;

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
    }

    public static void main(String... args) throws Exception {
//        CallOrResult<Integer> okCall = RecursionBuilder.okCall(1, 200000);
//        RecursionBuilder.iterative(okCall);
        BigInteger big = BigInteger.valueOf(100000000);
        Log.print(CallOrResult.iterative(50000, RecursionBuilder.fibbCall(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(999))).get());
        BigInteger m = BigInteger.valueOf(3);
        BigInteger n = BigInteger.valueOf(12);
//        Log.print(RecursionBuilder.ackermann(m, n));
        Log.print(CallOrResult.iterative(RecursionBuilder.ackermannCall(m, n)));
//        Log.print(RecursionBuilder.ackermann(m, n));
//        Log.print(CallOrResult.iterative(RecursionBuilder.ackermannCall(m, n)));
//        Log.print(RecursionBuilder.fibb(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(999)));
        Log.print("End");
        Log.close();
//        RecursionBuilder.okCall(0, 8000);
    }
}
