/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import static empiric.core.StackOverflowTest.RecursionBuilder.callCounter;
import static empiric.core.StackOverflowTest.RecursionBuilder.callerCounter;
import static empiric.core.StackOverflowTest.RecursionBuilder.recursiveCounter;
import static empiric.core.StackOverflowTest.RecursionBuilder.recursiveCounter2;
import static empiric.core.StackOverflowTest.RecursionBuilder.recursiveCounterCaller;
import static empiric.core.StackOverflowTest.RecursionBuilder.recursiveCounterCaller2;
import static empiric.core.StackOverflowTest.RecursionBuilder.recursiveCounterCaller3;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.Callable;
import lt.lb.commons.Log;
import lt.lb.commons.caller.Caller;
import lt.lb.commons.caller.CallerBuilder;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.misc.rng.RandomDistribution;

/**
 *
 * @author laim0nas100
 */
public class StackOverflowTest {

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

        private static RandomDistribution rng = RandomDistribution.uniform(new Random());

        public static Caller<BigInteger> fibb2Caller(long seq) {
            if (seq == 0) {
                return Caller.ofResult(BigInteger.ZERO);
            }
            if (seq == 1) {
                return Caller.ofResult(BigInteger.ONE);
            }

//            if(rng.nextInt(1000) >=999){
//                throw new Error("Whoopsie");
//            }
            Caller<BigInteger> toResultCall = new CallerBuilder<BigInteger>()
                    .withDependencyCall(a -> fibb2Caller(seq - 1))
                    .withDependencyCall(a -> fibb2Caller(seq - 2))
                    .toResultCall(args -> args.get(0).add(args.get(1)));

            return toResultCall;

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

        public static Caller<BigInteger> fibbCaller(BigInteger f1, BigInteger f2, BigInteger limit) {
            ExtComparator<BigInteger> cmp = ExtComparator.ofComparable();
            BigInteger add = f1.add(f2);
            if (cmp.greaterThan(f1, limit)) {
                return Caller.ofResult(f1);
            } else {
                return Caller.ofFunction((args) -> fibbCaller(add, f1, limit));
            }
        }

        public static Caller<BigInteger> ackermannCaller(BigInteger m, BigInteger n) {
            if (m.equals(BigInteger.ZERO)) {
                return Caller.ofResult(n.add(BigInteger.ONE));
            }
            ExtComparator<BigInteger> cmp = ExtComparator.ofComparable();
            if (cmp.greaterThan(m, BigInteger.ZERO) && cmp.equals(n, BigInteger.ZERO)) {
                return Caller.ofFunction(a -> ackermannCaller(m.subtract(BigInteger.ONE), BigInteger.ONE));
            }

            if (cmp.greaterThan(m, BigInteger.ZERO) && cmp.greaterThan(n, BigInteger.ZERO)) {

                return new CallerBuilder<BigInteger>()
                        .withDependencyCall(args -> ackermannCaller(m, n.subtract(BigInteger.ONE)))
                        .toCall(args -> ackermannCaller(m.subtract(BigInteger.ONE), args.get(0)));
            }
            throw new IllegalStateException();
        }

        public static Caller<Long> ackermannCaller(long m, long n) {
            if (m == 0) {
                return Caller.ofResult(n + 1);
            }
            ExtComparator<Long> cmp = ExtComparator.ofComparable();
            if (cmp.greaterThan(m, 0L) && cmp.equals(n, 0L)) {
                return Caller.ofFunction(args -> ackermannCaller(m - 1, 1L));
            }

            if (cmp.greaterThan(m, 0L) && cmp.greaterThan(n, 0L)) {

                return new CallerBuilder<Long>()
                        .withDependencyCall(args -> ackermannCaller(m, n - 1))
                        .toCall(args -> ackermannCaller(m - 1, args.get(0)));
            }
            throw new IllegalStateException();
        }

        public static long callCounter = 0;
        public static long callerCounter = 0;

        public static long recursiveCounter(long c1, long c2, long c3) {
            Log.print("REGULAR", c1, c2, c3);
            callCounter++;
            if (c1 <= 0) {
                return 0;
            } else if (c2 <= 0) {
                c2 = c1;
                c1--;
            } else if (c3 <= 0) {
                c3 = c2;
                c2--;
            } else {
                c3--;
            }

            return recursiveCounter(
                    recursiveCounter(c1, recursiveCounter(c1, c2, recursiveCounter(c1, c2, c3)), recursiveCounter(c1, c2, c3)),
                    recursiveCounter(c1, c2, recursiveCounter(c1, c2, c3)),
                    recursiveCounter(c1, c2, c3)
            );
        }

        public static Caller<Long> recursiveCounterCaller(long c1, long c2, long c3) {
            callerCounter++;
            Log.print("CALLER", c1, c2, c3);
            if (c1 <= 0) {
                return Caller.ofResult(0L);
            } else if (c2 <= 0) {
                c2 = c1;
                c1--;
            } else if (c3 <= 0) {
                c3 = c2;
                c2--;
            } else {
                c3--;
            }

            final long fc1 = c1;
            final long fc2 = c2;
            final long fc3 = c3;
            Caller<Long> call_1 = Caller.ofFunction(args -> recursiveCounterCaller(fc1, fc2, fc3));
            Caller<Long> call_2 = new CallerBuilder<Long>().withDependency(call_1).toCall(args -> recursiveCounterCaller(fc1, fc2, args.get(0)));
            Caller<Long> call_3 = new CallerBuilder<Long>().withDependency(call_1).withDependency(call_2).toCall(args -> recursiveCounterCaller(fc1, args.get(0), args.get(1)));
            return new CallerBuilder<Long>().withDependency(call_3).withDependency(call_2).withDependency(call_1).toCall(a -> recursiveCounterCaller(a.get(0), a.get(1), a.get(2)));

        }

        public static long recursiveCounter2(long c1, long c2, long c3) {
            callCounter++;
            Log.print("REGULAR", c1, c2, c3, callCounter);
            
            if (c1 <= 0) {
                return 0;
            } else if (c2 <= 0) {
                c2 = c1;
                c1--;
            } else if (c3 <= 0) {
                c3 = c2;
                c2--;
            } else {
                c3--;
            }

            c3 = recursiveCounter2(c1, c2, c3);
            c2 = recursiveCounter2(c1, c2, c3);
            c1 = recursiveCounter2(c1, c2, c3);
            return recursiveCounter2(c1, c2, c3);
        }

        public static Caller<Long> recursiveCounterCaller2(long c1, long c2, long c3) {
            callerCounter++;
            Log.print("CALLER", c1, c2, c3);
            if (c1 <= 0) {
                return Caller.ofResult(0L);
            } else if (c2 <= 0) {
                c2 = c1;
                c1--;
            } else if (c3 <= 0) {
                c3 = c2;
                c2--;
            } else {
                c3--;
            }

            final long fc1 = c1;
            final long fc2 = c2;
            final long fc3 = c3;
            Caller<Long> call_1 = Caller.ofFunction(args -> recursiveCounterCaller(fc1, fc2, fc3));
            Caller<Long> call_2 = new CallerBuilder<Long>().withDependency(call_1).toCall(args -> recursiveCounterCaller2(fc1, fc2, args.get(0)));
            Caller<Long> call_3 = new CallerBuilder<Long>().withDependency(call_1).withDependency(call_2).toCall(args -> recursiveCounterCaller2(fc1, args.get(0), args.get(1)));
            return new CallerBuilder<Long>().withDependency(call_3).withDependency(call_2).withDependency(call_1).toCall(a -> recursiveCounterCaller2(a.get(0), a.get(1), a.get(2)));

        }
        
        public static Caller<Long> recursiveCounterCaller3(long c1, long c2, long c3) {
            callerCounter++;
            Log.print("CALLER", c1, c2, c3, callerCounter);
            if (c1 <= 0) {
                return Caller.ofResult(0L);
            } else if (c2 <= 0) {
                c2 = c1;
                c1--;
            } else if (c3 <= 0) {
                c3 = c2;
                c2--;
            } else {
                c3--;
            }

            final long fc1 = c1;
            final long fc2 = c2;
            final long fc3 = c3;
            Caller<Long> call_1 = Caller.ofFunction(args -> recursiveCounterCaller3(fc1, fc2, fc3));
            Caller<Long> call_2 = new CallerBuilder<Long>().withDependency(call_1).withCurry().toCall(args -> recursiveCounterCaller3(fc1, fc2, args.get(0)));
            Caller<Long> call_3 = new CallerBuilder<Long>().withDependency(call_2).withCurry().toCall(args -> recursiveCounterCaller3(fc1, args.get(0), args.get(1)));
            return new CallerBuilder<Long>().withDependency(call_3).withCurry().toCall(a -> recursiveCounterCaller3(a.get(0), a.get(1), a.get(2)));

        }
    }
    
    

    public static void main(String... args) throws Exception {
        Log.main().async = false;

        Log.print("Start");
        long a = 1;
        long b = 5;
        long c = 5;
        long recursiveCounter1 = recursiveCounter2(a, b, c);

        Log.print(callCounter, recursiveCounter1);
        long recursiveCounter2 = recursiveCounterCaller3(a, b, c).resolve();
        Log.print(callerCounter, recursiveCounter2);
        NestedException.nestedThrow(new Error("Quit"));
//        CallOrResult<Integer> okCall = RecursionBuilder.okCall(1, 200000);
//        RecursionBuilder.iterative(okCall);
        BigInteger big = BigInteger.valueOf(100000000);
        BigInteger m = BigInteger.valueOf(3);
        BigInteger n = BigInteger.valueOf(8);
//        Log.print(RecursionBuilder.ackermann(m, n));
//        Log.print(CallOrResult.iterative(RecursionBuilder.ackermannCall(m, n)));

        int exp = 20;
//        Log.print(RecursionBuilder.fibb(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(exp)));
//        Log.print(RecursionBuilder.fibbCaller(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(exp)).resolve());
//
//        Log.print(RecursionBuilder.fibb2(35));
//        Log.print(Caller.resolve(RecursionBuilder.fibb2Caller(35), Optional.empty(), Optional.of(50L)));
//        Log.print(RecursionBuilder.fibb2Caller(40).resolve());
        Log.print(RecursionBuilder.fibb2Caller(40).resolveThreaded());
        Log.print(RecursionBuilder.fibb2Caller(40).resolve());
        Log.print(RecursionBuilder.fibb2Caller(40).resolveThreaded());

        Log.print("############");
//        Caller<BigInteger> fibbCaller = RecursionBuilder.fibbCaller(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(999));
//        BigInteger resolve = fibbCaller.resolve();
//        Log.print(resolve);

//        Caller<Long> ackCaller = RecursionBuilder.ackermannCaller(3L, 5L);
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
