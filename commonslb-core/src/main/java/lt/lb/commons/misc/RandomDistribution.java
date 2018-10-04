/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

import lt.lb.commons.F;
import java.util.*;
import java.util.function.Supplier;
import lt.lb.commons.containers.Tuple;

/**
 * RandomGenerator based on Double number generator
 *
 * @author laim0nas100
 */
public interface RandomDistribution {

    /**
     * Uniform distribution
     *
     * @param rnd
     * @return
     */
    public static RandomDistribution uniform(Supplier<Double> rnd) {
        return () -> rnd.get();
    }

    /**
     * Dice distribution (average of uniform distributions)
     *
     * @param rnd
     * @param diceAmount
     * @return
     */
    public static RandomDistribution dice(Supplier<Double> rnd, int diceAmount) {
        return () -> {
            double x = 0;
            for (int i = 0; i < diceAmount; i++) {
                x += rnd.get();
            }
            return x / diceAmount;
        };
    }

    /**
     * Uniform distribution but to the power. (Generally lower than uniform)
     *
     * @param rnd
     * @param power
     * @return
     */
    public static RandomDistribution xPower(Supplier<Double> rnd, final double power) {
        return () -> Math.pow(rnd.get(), power);
    }

    /**
     * Uniform distribution but to the power. (Generally higher than uniform)
     *
     * @param rnd
     * @param power
     * @return
     */
    public static RandomDistribution oneMinusXpower(Supplier<Double> rnd, final double power) {
        return () -> 1 - Math.pow(rnd.get(), power);
    }

    /**
     * Average of uniforms, before applied power.
     *
     * @param rnd
     * @param dice
     * @param power
     * @return
     */
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

    /**
     * Average of uniforms, after applied power.
     *
     * @param rnd
     * @param dice
     * @param power
     * @return
     */
    public static RandomDistribution powerToDice(Supplier<Double> rnd, final int dice, final double power) {

        return () -> {
            double x = 0;
            for (int i = 0; i < dice; i++) {
                x += Math.pow(rnd.get(), power);
            }
            x = x / dice;
            return x;
        };
    }

    /**
     * Extremes. Combines to xPower distributions.
     *
     * @param rnd
     * @param power
     * @return
     */
    public static RandomDistribution extremes(Supplier<Double> rnd, final double power) {
        RandomDistribution xPower = xPower(rnd, power);

        return () -> {
            double x = xPower.nextDouble();
            return rnd.get() > 0.5 ? x : 1 - x;
        };
    }

    /**
     * Base method.
     *
     * @return returns distributed Double
     */
    public Double nextDouble();

    public default Supplier<Boolean> getBooleanSupplier() {
        return () -> this.nextBoolean();
    }

    public default Supplier<Integer> getIntSupplier(Integer upperBound) {
        return () -> this.nextInt(upperBound);
    }

    public default Supplier<Integer> getIntegerSupplier(Integer lowerBound, Integer upperBound) {
        return () -> this.nextInt(lowerBound, upperBound);
    }

    public default Supplier<Double> getDoubleSupplier() {
        return () -> this.nextDouble();
    }

    public default Supplier<Double> getDoubleSupplier(Double upperBound) {
        return () -> this.nextDouble(upperBound);
    }

    public default Supplier<Double> getDoubleSupplier(Double lowerBound, Double upperBound) {
        return () -> this.nextDouble(lowerBound, upperBound);
    }

    public default Supplier<Long> getLongSupplier(Long upperBound) {
        return () -> this.nextLong(upperBound);
    }

    public default Supplier<Long> getLongSupplier(Long lowerBound, Long upperBound) {
        return () -> this.nextLong(lowerBound, upperBound);
    }

    /**
     * Default implementation calls nextInt(0,2) > 0
     *
     * @return returns boolean
     */
    public default Boolean nextBoolean() {
        return nextInt(0, 2) > 0;
    }

    public default Double nextDouble(Double lowerBound, Double upperBound) {
        double diff = upperBound - lowerBound;
        if (diff <= 0) {
            throw new IllegalArgumentException("Illegal random bounds:" + lowerBound + " " + upperBound);
        }
        return lowerBound + (nextDouble() * diff);
    }

    public default Double nextDouble(Double upperBound) {
        return nextDouble(0d, upperBound);
    }

    /**
     * Base method. Override for better performance. Default implementation
     * calls nextDouble(lowerBound, upperBound);
     *
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public default Long nextLong(Long lowerBound, Long upperBound) {
        return (long) Math.floor(nextDouble(lowerBound.doubleValue(), upperBound.doubleValue()));
    }

    public default Long nextLong(Long upperBound) {
        return nextLong(0L, upperBound);
    }

    /**
     * Base method. Default implementation calls nextDouble. Override for better
     * gains.
     *
     * @param lowerBound
     * @param upperBound
     * @return returns Integer in interval [loweBound, upperBound)
     */
    public default Integer nextInt(Integer lowerBound, Integer upperBound) {
        return (int) Math.floor(nextDouble(lowerBound.doubleValue(), upperBound.doubleValue()));
    }

    public default Integer nextInt(Integer upperBound) {
        return nextInt(0, upperBound);
    }

    /**
     * Shuffle list based on this RandomDistribution.
     *
     * @param list
     */
    public default void shuffle(List list) {

        // copy from Collections.shuffle
        int size = list.size();
        if (size < 8 || list instanceof RandomAccess) {
            for (int i = size; i > 1; i--) {
                F.swap(list, i - 1, nextInt(i));
            }
        } else {
            Object arr[] = list.toArray();
            // Shuffle array
            for (int i = size; i > 1; i--) {
                F.swap(arr, i - 1, nextInt(i));
            }
            ListIterator it = list.listIterator();
            for (Object arr1 : arr) {
                it.next();
                it.set(arr1);
            }
        }

    }

    /**
     * @param <T> type
     * @param col collection
     * @param amount to pick from collection
     * @param startingAmount amount to start with from index 0
     * @param amountDecay how much to decrease amount after each index in
     * collection until there's only 1 per index
     * @return
     */
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

    /**
     *
     * @param <T>
     * @param col collection to explore
     * @param amount how many elements to return
     * @return
     */
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

    /**
     *
     * @param <T>
     * @param col collection to explore
     * @return return random element
     */
    public default <T> T pickRandom(List<T> col) {
        int i = nextInt(0, col.size());
        return col.get(i);
    }

    /**
     *
     * @param <T>
     * @param col collection to explore
     * @return return random element
     */
    public default <T> T pickRandom(Collection<T> col) {
        return pickRandom(col, 1).getFirst();
    }

    /**
     *
     * @param <T>
     * @param col collection to modify
     * @return return random element and remove from collection
     */
    public default <T> T removeRandom(Collection<T> col) {
        T pickRandom = pickRandom(col);
        col.remove(pickRandom);
        return pickRandom;
    }

    /**
     *
     * @param <T>
     * @param amount how many elements to pick
     * @param tuples <Size,Object> distribution of elements
     * @return
     */
    public default <T> LinkedList<T> pickRandomDistributed(int amount, Tuple<Integer, T>... tuples) {
        ArrayList<T> list = new ArrayList<>();

        F.iterate(tuples, (index, t) -> {
            for (int i = 0; i < t.g1; i++) {
                list.add(t.g2);
            }
        });
        return pickRandom(list, amount);
    }

    /**
     *
     * @param <T>
     * @param amount how many elements to pick
     * @param tuples <Size,Object> distribution of elements
     * @return
     */
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
