/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import static empiric.core.StackOverflowTest.RecursionBuilder.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.caller.Caller;
import lt.lb.caller.CallerBuilder;
import lt.lb.caller.SharedCallerBuilder;
import lt.lb.commons.misc.compare.ExtComparator;
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
            Caller<BigInteger> toResultCall = new CallerBuilder<BigInteger>(2)
                    .withDependencyCall(a -> fibb2Caller(seq - 1))
                    .withDependencyCall(a -> fibb2Caller(seq - 2))
                    .toResultCall(args -> args._0.add(args._1));

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
                        .toCall(args -> ackermannCaller(m.subtract(BigInteger.ONE), args._0));
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
                        .toCall(args -> ackermannCaller(m - 1, args._0));
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
            Caller<Long> call_2 = new CallerBuilder<Long>().with(call_1).toCall(args -> recursiveCounterCaller(fc1, fc2, args.get(0)));
            Caller<Long> call_3 = new CallerBuilder<Long>().with(call_1, call_2).toCall(args -> recursiveCounterCaller(fc1, args.get(0), args.get(1)));
            return new CallerBuilder<Long>().with(call_3, call_2, call_1).toCall(a -> recursiveCounterCaller(a.get(0), a.get(1), a.get(2)));

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
                c2--;
            }

            c3 = 0L;
            c2 = recursiveCounter2(c1, c2, c3);
            c1 = recursiveCounter2(c1, c2, c3);
            return recursiveCounter2(c1, c2, c3);
        }

        public static Caller<Long> recursiveCounterCaller3(long c1, long c2, long c3, String st) {
            callerCounter++;
            Log.print(st + "CALLER", c1, c2, c3, callerCounter);
            if (c1 <= 0) {
                return Caller.ofResult(0L);
            } else if (c2 <= 0) {
                c2 = c1;
                c1--;
            } else if (c3 <= 0) {
                c2--;
            }

            final long fc1 = c1;
            final long fc2 = c2;
            final long fc3 = 0L;
            Caller<Long> call_2 = new SharedCallerBuilder<Long>().with(
                    Caller.ofResult(fc1),
                    Caller.ofResult(fc2),
                    Caller.ofResult(fc3)
            )
                    .toCall(a -> {
                        Log.print("Caller 1", a);
                        return recursiveCounterCaller3(a._0, a._1, a._2, st + ".");
                    });
            Caller<Long> call_1 = new SharedCallerBuilder<Long>()
                    .with(
                            Caller.ofResult(fc1),
                            call_2,
                            Caller.ofResult(fc3)
                    )
                    .toCall(a -> {
                        Log.print("Caller 2", a);
                        return recursiveCounterCaller3(a._0, a._1, a._2, st + ".");
                    });

            return new CallerBuilder<Long>().with(call_1, call_2, Caller.ofResult(fc3))
                    .toCall(a -> {
                        Log.print("Caller 3", a);
                        return recursiveCounterCaller3(a._0, a._1, a._2, st + ".");
                    });

        }

        public static List<Long> list1 = new ArrayList<>();
        public static List<Long> list2 = new ArrayList<>();

        public static long rec2(long numb) {
            if (numb <= 0) {
                return 0;
            }
            list1.add(numb);

            numb--;
            long n1 = rec2(numb);
            long n2 = rec2(numb - n1);
            return rec2(n1 - n2);
        }

        public static Caller<Long> rec2Caller(long numb) {
            if (numb <= 0) {
                return Caller.ofResult(0L);
            }
            list2.add(numb);
            final long n = numb - 1;

            Caller<Long> toCall = new SharedCallerBuilder<Long>().toCall(arg -> rec2Caller(n));

            Caller<Long> toCall1 = new CallerBuilder<Long>().with(toCall).toCall(args -> rec2Caller(n - args.get(0)));

            return new CallerBuilder<Long>().with(toCall, toCall1).toCall(args -> rec2Caller(args.get(0) - args.get(1)));
        }

        public static Long recBoi(long number, AtomicLong counter) {
            counter.incrementAndGet();
            if (number % 4 == 0) {
                return 0L;
            } else {
                if (number > 5000) {
                    return number;
                } else {
//                    safeSleep(number);
                    long n1 = recBoi(number * 3, counter);
                    long n2 = recBoi(n1 + 1, counter);
                    long n3 = recBoi(n1 + 2, counter);
                    return recBoi(n1 + n2 + n3, counter);
                }
            }
        }
        static CallerBuilder<Long> sharedBuilder = new SharedCallerBuilder<Long>();

        public static Caller<Long> recBoiCaller(long number, AtomicLong counter) {
            counter.incrementAndGet();
            if (number % 4 == 0) {
                return Caller.ofResult(0L);
            } else {
                if (number > 5000) {
                    return Caller.ofResult(number);
                } else {
//                    safeSleep(number);
                    Caller<Long> n1 = sharedBuilder.toCall(a -> recBoiCaller(number * 3, counter));
                    CallerBuilder<Long> builder = new CallerBuilder<Long>().with(n1);
                    Caller<Long> n2 = builder.toCall(a -> recBoiCaller(a._0 + 1, counter));
                    Caller<Long> n3 = builder.toCall(a -> recBoiCaller(a._0 + 2, counter));
                    return new CallerBuilder<Long>()
                            .with(n1, n2, n3)
                            .toCall(a -> recBoiCaller(a._0 + a._1 + a._2, counter));
                }
            }
        }

        public static void safeSleep(long sleepy) {
            long s = (long) (sleepy / 500d);
            F.unsafeRun(() -> Thread.sleep(s));
        }
    }

    public static void main(String... args) throws Exception {
        Log.main().async = false;

        Log.print("Start");
        long a = 1;
        long b = 1;
        long c = 1;

        long d = 5;
//        rec2(d);
        Log.print("OK");
//        rec2Caller(d).resolve();

//        long recursiveCounter1 = recursiveCounter2(a, b, c);
////
////        Log.print(callCounter, recursiveCounter1);
//        long recursiveCounter2 = recursiveCounterCaller3(a, b, c, "").resolve();
//        Log.print(callerCounter, callCounter);
//        Log.print(list1.equals(list2));
        AtomicLong c1 = new AtomicLong();
        AtomicLong c2 = new AtomicLong();
        Log.print(recBoi(3L, c1));
        Log.print(recBoiCaller(3L, c2).resolve());
        Log.print(c1.get(), c2.get());
//        NestedException.nestedThrow(new Error("Quit"));
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
        Log.print(RecursionBuilder.fibb2Caller(30).resolveThreaded());
        Log.print(RecursionBuilder.fibb2Caller(30).resolve());
        Log.print(RecursionBuilder.fibb2Caller(30).resolveThreaded());

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
