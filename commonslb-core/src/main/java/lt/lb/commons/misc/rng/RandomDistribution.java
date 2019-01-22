package lt.lb.commons.misc.rng;

import lt.lb.commons.F;
import java.util.*;
import java.util.function.Supplier;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.misc.numbers.OverflowCheck;

/**
 * Pseudo-random number generator based on Double number generator
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
     * Uniform distribution. Use faster methods.
     *
     * @param rnd
     * @return
     */
    public static RandomDistribution uniform(Random rnd) {
        return new RandomDistribution() {
            @Override
            public Long nextLong() {
                return rnd.nextLong();
            }

            @Override
            public Boolean nextBoolean() {
                return rnd.nextBoolean();
            }

            @Override
            public Double nextDouble() {
                return rnd.nextDouble();
            }

            @Override
            public Integer nextInt() {
                return rnd.nextInt();
            }

            @Override
            public Integer nextInt(Integer upperbound) {
                return rnd.nextInt(upperbound);
            }

        };
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

    public default Supplier<Integer> getIntegerSupplier(Integer upperBound) {
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
        return nextInt() % 2 == 0;
    }

    public default Double nextDouble(Double lowerBound, Double upperBound) {
        if (upperBound <= lowerBound) {
            throw new IllegalArgumentException("Illegal random bounds:" + lowerBound + " " + upperBound);
        }
        Double d = nextDouble();
        return lowerBound + (d * upperBound - d * lowerBound);
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
        if (upperBound <= lowerBound) {
            throw new IllegalArgumentException("Illegal random bounds:" + lowerBound + " " + upperBound);
        }
        boolean overflowable = OverflowCheck.willOverflowIfAdd(lowerBound, -upperBound);
        long nextLong = nextLong();
        if (overflowable) {
            boolean inLower = nextLong >= lowerBound;
            boolean inUpper = nextLong <= upperBound;

            if (inLower && inUpper) {
                return nextLong;
            } else {
                return nextLong += Long.MAX_VALUE;
            }

        } else {
            long diff = upperBound - lowerBound;
            int sign = Long.signum(nextLong);
            long mod = (nextLong % diff);
            return lowerBound + sign * (mod);
        }
    }

    public default Long nextLong(Long upperBound) {
        return nextLong(0L, upperBound);
    }

    /**
     * Override this for performance gains. Returns Long value.
     *
     * @return
     */
    public default Long nextLong() {
        double next = nextDouble(-4d, 4d);
        long plus = (long) Math.abs(next) % 2 == 1 ? 1L : 0L;
        return (long) (next * (Long.MAX_VALUE / 4)) + plus;
    }

    /**
     *
     * @param lowerBound
     * @param upperBound
     * @return returns Integer in interval [loweBound, upperBound)
     */
    public default Integer nextInt(Integer lowerBound, Integer upperBound) {
        if (upperBound <= lowerBound) {
            throw new IllegalArgumentException("Illegal random bounds:" + lowerBound + " " + upperBound);
        }
        boolean overflowable = OverflowCheck.willOverflowIfAdd(lowerBound, -upperBound);
        int nextInt = nextInt();
        if (overflowable) {
            boolean inLower = nextInt >= lowerBound;
            boolean inUpper = nextInt <= upperBound;

            if (inLower && inUpper) {
                return nextInt;
            } else {
                return nextInt += Integer.MAX_VALUE;
            }

        } else {
            int diff = upperBound - lowerBound;
            int sign = Integer.signum(nextInt);
            int mod = (nextInt % diff);
            return lowerBound + sign * (mod);
        }
    }

    public default Integer nextInt(Integer upperBound) {
        return nextInt(0, upperBound);
    }

    /**
     * Override this for performance gains. Returns Integer value.
     *
     * @return
     */
    public default Integer nextInt() {
        double next = nextDouble(-4d, 4d);
        int plus = (int) Math.abs(next) % 2 == 1 ? 1 : 0;
        return (int) (next * (Integer.MAX_VALUE / 4)) + plus;
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

        ArrayList<Tuple<Double, T>> tuples = new ArrayList<>(amount);
        for (T item : col) {
            tuples.add(new Tuple<>((double) startingAmount, item));
            startingAmount = Math.max(startingAmount - amountDecay, 1);
        }
        return this.pickRandomDistributed(amount, tuples);

    }

    /**
     *
     * @param <T>
     * @param col collection to explore
     * @param amount how many elements to return
     * @return
     */
    public default <T> LinkedList<T> pickRandom(Collection<T> col, int amount) {

        ArrayList<RandomRange<T>> rrList = new ArrayList<>();
        F.iterate(col, (i, item) -> {
            rrList.add(new RandomRange(item, 1d));
        });
        RandomRanges<T> rrr = new RandomRanges(rrList);
        ArrayList<T> pickRandom = rrr.pickRandom(amount, () -> this.nextDouble(rrr.getLimit()));
        return new LinkedList<>(pickRandom);

    }

    /**
     *
     * @param <T>
     * @param col list to explore
     * @return return random element
     */
    public default <T> T pickRandom(List<T> col) {
        return col.get(nextInt(col.size()));
    }

    /**
     *
     * @param <T>
     * @param col collection to explore
     * @return return random element
     */
    public default <T> T pickRandom(Collection<T> col) {
        return F.find(col, nextInt(col.size()), (i, item) -> true).get().getG2();
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
    public default <T> LinkedList<T> pickRandomDistributed(int amount, Tuple<Double, T>... tuples) {
        return pickRandomDistributed(amount, Arrays.asList(tuples));
    }
    /**
     *
     * @param <T>
     * @param amount how many elements to pick
     * @param tuples <Size,Object> distribution of elements
     * @return
     */
    public default <T> LinkedList<T> pickRandomDistributed(int amount, Collection<Tuple<Double, T>> tuples) {
        ArrayList<RandomRange<T>> rrList = new ArrayList<>(tuples.size());
        F.iterate(tuples, (i, item) -> {
            rrList.add(new RandomRange(item.getG2(), item.g1));
        });
        RandomRanges<T> rrr = new RandomRanges(rrList);
        ArrayList<T> pickRandom = rrr.pickRandom(amount, () -> this.nextDouble(rrr.getLimit()));
        return new LinkedList<>(pickRandom);
    }
    
    
    public static Random asRandom(RandomDistribution rng){
        return new Random(){
            @Override
            protected int next(int bits) {
                return rng.nextInt();
            }

            @Override
            public double nextDouble() {
                return rng.nextDouble(); 
            }

            @Override
            public boolean nextBoolean() {
                return rng.nextBoolean(); 
            }

            @Override
            public long nextLong() {
                return rng.nextLong(); 
            }

            @Override
            public int nextInt() {
                return rng.nextInt(); 
            }

            @Override
            public synchronized void setSeed(long seed) {
                throw new UnsupportedOperationException();
            }

            @Override
            public float nextFloat() {
                return rng.nextDouble().floatValue();
            }

            @Override
            public int nextInt(int bound) {
                return rng.nextInt(bound);
            }
            
            
        };
    }

}
