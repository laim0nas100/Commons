/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

import java.util.*;
import java.util.function.Supplier;
import lt.lb.commons.containers.Tuple;
import static lt.lb.commons.misc.F.swap;

/**
 *
 * @author Lemmin
 */
public interface RandomDistribution {

    public static RandomDistribution uniform(Supplier<Double> rnd) {
        return () -> rnd.get();
    }

    public static RandomDistribution dice(Supplier<Double> rnd, int diceAmount) {
        return () -> {
            double x = 0;
            for (int i = 0; i < diceAmount; i++) {
                x += rnd.get();
            }
            return x / diceAmount;
        };
    }

    public static RandomDistribution xPower(Supplier<Double> rnd, final double power) {
        return () -> Math.pow(rnd.get(), power);
    }

    public static RandomDistribution oneMinusXpower(Supplier<Double> rnd, final double power) {
        return () -> 1 - Math.pow(rnd.get(), power);
    }

    public static RandomDistribution diceToPower(Supplier<Double> rnd, final int dice, final double power) {

        return () -> {
            double x = 0;
            for (int i = 0; i < dice; i++) {
                x += rnd.get();
            }
            x = x / dice;
            return Math.pow(x, power);
        };
    }

    public static RandomDistribution extremes(Supplier<Double> rnd, final double power) {
        RandomDistribution xPower = xPower(rnd, power);

        return () -> {
            double x = xPower.nextDouble();
            return rnd.get() > 0.5 ? x : 1 - x;
        };
    }

    public Double nextDouble();

    public default Double nextDouble(Number lowerBound, Number upperBound) {
        double min = lowerBound.doubleValue();
        double max = upperBound.doubleValue();
        double diff = max - min;
        if (diff <= 0) {
            throw new IllegalArgumentException("Illegal random bounds:" + min + " " + max);
        }
        return min + (nextDouble() * diff);
    }

    public default Double nextDouble(Number upperBound) {
        return nextDouble(0, upperBound);
    }

    public default Long nextLong(Number lowerBound, Number upperBound) {
        return (long) Math.floor(nextDouble(lowerBound.doubleValue(), upperBound.doubleValue()));
    }

    public default Long nextLong(Number upperBound) {
        return (long) Math.floor(nextDouble(0, upperBound.doubleValue()));
    }

    public default Integer nextInt(Number lowerBound, Number upperBound) {
        return (int) Math.floor(nextDouble(lowerBound.doubleValue(), upperBound.doubleValue()));
    }

    public default Integer nextInt(Integer upperBound) {
        return nextInt(0, upperBound);
    }

    public default void shuffle(List list) {

        // copy from Collections.shuffle
        int size = list.size();
        if (size < 8 || list instanceof RandomAccess) {
            for (int i = size; i > 1; i--) {
                swap(list, i - 1, nextInt(i));
            }
        } else {
            Object arr[] = list.toArray();

            // Shuffle array
            for (int i = size; i > 1; i--) {
                swap(arr, i - 1, nextInt(i));
            }
            ListIterator it = list.listIterator();
            for (Object arr1 : arr) {
                it.next();
                it.set(arr1);
            }
        }

    }

    public default <T> LinkedList<T> pickRandomPreferLow(Collection<T> col, int amount, int startingAmount, int amountDecay) {

        int limit = Math.min(amount, col.size());
        ArrayList<Integer> indexArray = new ArrayList<>();
        for (int i = 0; i < col.size(); i++) {
            for (int indexAm = Math.max(1, startingAmount); indexAm > 0; indexAm--) {
                indexArray.add(i);
            }
            startingAmount -= amountDecay;
        }
        ArrayList<T> array = new ArrayList<>(col);
        LinkedList<T> result = new LinkedList<>();
        shuffle(indexArray);
        int last = indexArray.size() - 1;
        int first = last - limit;
        for (int i = last; i > first; i--) {
            result.add(array.get(indexArray.remove(i)));
        }
        return result;

    }

    public default <T> LinkedList<T> pickRandom(Collection<T> col, int amount) {

        int limit = Math.min(amount, col.size());
        ArrayList<Integer> indexArray = new ArrayList<>();
        for (int i = 0; i < col.size(); i++) {
            indexArray.add(i);
        }
        ArrayList<T> array = new ArrayList<>(col);
        LinkedList<T> result = new LinkedList<>();
        shuffle(indexArray);
        int last = indexArray.size() - 1;
        int first = last - limit;
        for (int i = last; i > first; i--) {
            result.add(array.get(indexArray.remove(i)));
        }
        return result;

    }

    public default <T> T pickRandom(List<T> col) {
        int i = nextInt(0, col.size());
        return col.get(i);
    }

    public default <T> T pickRandom(Collection<T> col) {
        return pickRandom(col, 1).getFirst();
    }

    public default <T> T removeRandom(Collection<T> col) {
        T pickRandom = pickRandom(col);
        col.remove(pickRandom);
        return pickRandom;
    }

    public default <T> LinkedList<T> pickRandomDistributed(int amount, Tuple<Integer, T>... tuples) {
        ArrayList<T> list = new ArrayList<>();

        F.iterate(tuples, (index, t) -> {
            for (int i = 0; i < t.g1; i++) {
                list.add(t.g2);
            }
        });
        return pickRandom(list, amount);
    }

    public default <T> LinkedList<T> pickRandomDistributed(int amount, Collection<Tuple<Integer, T>> tuples) {
        ArrayList<T> list = new ArrayList<>();

        F.iterate(tuples, (index, t) -> {
            for (int i = 0; i < t.g1; i++) {
                list.add(t.g2);
            }
        });
        return pickRandom(list, amount);
    }

}
