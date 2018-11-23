package lt.lb.commons.misc.numbers;

import lt.lb.commons.containers.tuples.Tuples;

/**
 *
 * @author laim0nas100
 */
public class NumberBiFunctions {

    public static class DefaultPlus extends TypedBiFunction<Number, Number, Number> {

        public DefaultPlus() {

            this.functions.put(Tuples.create(Long.class, Long.class), (Number p1, Number p2) -> (long) (p1.longValue() + p2.longValue()));
            this.functions.put(Tuples.create(Long.class, Integer.class), (Number p1, Number p2) -> (long) (p1.longValue() + p2.intValue()));
            this.functions.put(Tuples.create(Long.class, Byte.class), (Number p1, Number p2) -> (long) (p1.longValue() + p2.byteValue()));
            this.functions.put(Tuples.create(Long.class, Short.class), (Number p1, Number p2) -> (long) (p1.longValue() + p2.shortValue()));

            this.functions.put(Tuples.create(Integer.class, Long.class), (Number p1, Number p2) -> (int) (p1.longValue() + p2.longValue()));
            this.functions.put(Tuples.create(Integer.class, Integer.class), (Number p1, Number p2) -> (int) (p1.intValue() + p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Byte.class), (Number p1, Number p2) -> (int) (p1.intValue() + p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Short.class), (Number p1, Number p2) -> (int) (p1.intValue() + p2.shortValue()));

            this.functions.put(Tuples.create(Short.class, Long.class), (Number p1, Number p2) -> (short) (p1.shortValue() + p2.longValue()));
            this.functions.put(Tuples.create(Short.class, Integer.class), (Number p1, Number p2) -> (short) (p1.shortValue() + p2.intValue()));
            this.functions.put(Tuples.create(Short.class, Short.class), (Number p1, Number p2) -> (short) (p1.shortValue() + p2.shortValue()));
            this.functions.put(Tuples.create(Short.class, Byte.class), (Number p1, Number p2) -> (short) (p1.shortValue() + p2.byteValue()));

            this.functions.put(Tuples.create(Byte.class, Long.class), (Number p1, Number p2) -> (byte) (p1.byteValue() + p2.longValue()));
            this.functions.put(Tuples.create(Byte.class, Integer.class), (Number p1, Number p2) -> (byte) (p1.byteValue() + p2.intValue()));
            this.functions.put(Tuples.create(Byte.class, Short.class), (Number p1, Number p2) -> (byte) (p1.byteValue() + p2.shortValue()));
            this.functions.put(Tuples.create(Byte.class, Byte.class), (Number p1, Number p2) -> (byte) (p1.byteValue() + p2.byteValue()));

            this.functions.put(Tuples.create(Float.class, Long.class), (Number p1, Number p2) -> (float) (p1.floatValue() + p2.longValue()));
            this.functions.put(Tuples.create(Float.class, Integer.class), (Number p1, Number p2) -> (float) (p1.floatValue() + p2.intValue()));
            this.functions.put(Tuples.create(Float.class, Short.class), (Number p1, Number p2) -> (float) (p1.floatValue() + p2.shortValue()));
            this.functions.put(Tuples.create(Float.class, Byte.class), (Number p1, Number p2) -> (float) (p1.floatValue() + p2.byteValue()));
            this.functions.put(Tuples.create(Float.class, Float.class), (Number p1, Number p2) -> (float) (p1.floatValue() + p2.floatValue()));
            this.functions.put(Tuples.create(Float.class, Double.class), (Number p1, Number p2) -> (float) (p1.floatValue() + p2.doubleValue()));

            this.functions.put(Tuples.create(Double.class, Long.class), (Number p1, Number p2) -> (double) (p1.doubleValue() + p2.longValue()));
            this.functions.put(Tuples.create(Double.class, Integer.class), (Number p1, Number p2) -> (double) (p1.doubleValue() + p2.intValue()));
            this.functions.put(Tuples.create(Double.class, Short.class), (Number p1, Number p2) -> (double) (p1.doubleValue() + p2.shortValue()));
            this.functions.put(Tuples.create(Double.class, Byte.class), (Number p1, Number p2) -> (double) (p1.doubleValue() + p2.byteValue()));
            this.functions.put(Tuples.create(Double.class, Float.class), (Number p1, Number p2) -> (double) (p1.doubleValue() + p2.floatValue()));
            this.functions.put(Tuples.create(Double.class, Double.class), (Number p1, Number p2) -> (double) (p1.doubleValue() + p2.doubleValue()));

        }

    }

    public static class DefaultMinus extends TypedBiFunction<Number, Number, Number> {

        public DefaultMinus() {

            this.functions.put(Tuples.create(Long.class, Long.class), (Number p1, Number p2) -> (long) (p1.longValue() - p2.longValue()));
            this.functions.put(Tuples.create(Long.class, Integer.class), (Number p1, Number p2) -> (long) (p1.longValue() - p2.intValue()));
            this.functions.put(Tuples.create(Long.class, Byte.class), (Number p1, Number p2) -> (long) (p1.longValue() - p2.byteValue()));
            this.functions.put(Tuples.create(Long.class, Short.class), (Number p1, Number p2) -> (long) (p1.longValue() - p2.shortValue()));

            this.functions.put(Tuples.create(Integer.class, Long.class), (Number p1, Number p2) -> (int) (p1.longValue() - p2.longValue()));
            this.functions.put(Tuples.create(Integer.class, Integer.class), (Number p1, Number p2) -> (int) (p1.intValue() - p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Byte.class), (Number p1, Number p2) -> (int) (p1.intValue() - p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Short.class), (Number p1, Number p2) -> (int) (p1.intValue() - p2.shortValue()));

            this.functions.put(Tuples.create(Short.class, Long.class), (Number p1, Number p2) -> (short) (p1.shortValue() - p2.longValue()));
            this.functions.put(Tuples.create(Short.class, Integer.class), (Number p1, Number p2) -> (short) (p1.shortValue() - p2.intValue()));
            this.functions.put(Tuples.create(Short.class, Short.class), (Number p1, Number p2) -> (short) (p1.shortValue() - p2.shortValue()));
            this.functions.put(Tuples.create(Short.class, Byte.class), (Number p1, Number p2) -> (short) (p1.shortValue() - p2.byteValue()));

            this.functions.put(Tuples.create(Byte.class, Long.class), (Number p1, Number p2) -> (byte) (p1.byteValue() - p2.longValue()));
            this.functions.put(Tuples.create(Byte.class, Integer.class), (Number p1, Number p2) -> (byte) (p1.byteValue() - p2.intValue()));
            this.functions.put(Tuples.create(Byte.class, Short.class), (Number p1, Number p2) -> (byte) (p1.byteValue() - p2.shortValue()));
            this.functions.put(Tuples.create(Byte.class, Byte.class), (Number p1, Number p2) -> (byte) (p1.byteValue() - p2.byteValue()));

            this.functions.put(Tuples.create(Float.class, Long.class), (Number p1, Number p2) -> (float) (p1.floatValue() - p2.longValue()));
            this.functions.put(Tuples.create(Float.class, Integer.class), (Number p1, Number p2) -> (float) (p1.floatValue() - p2.intValue()));
            this.functions.put(Tuples.create(Float.class, Short.class), (Number p1, Number p2) -> (float) (p1.floatValue() - p2.shortValue()));
            this.functions.put(Tuples.create(Float.class, Byte.class), (Number p1, Number p2) -> (float) (p1.floatValue() - p2.byteValue()));
            this.functions.put(Tuples.create(Float.class, Float.class), (Number p1, Number p2) -> (float) (p1.floatValue() - p2.floatValue()));
            this.functions.put(Tuples.create(Float.class, Double.class), (Number p1, Number p2) -> (float) (p1.floatValue() - p2.doubleValue()));

            this.functions.put(Tuples.create(Double.class, Long.class), (Number p1, Number p2) -> (double) (p1.doubleValue() - p2.longValue()));
            this.functions.put(Tuples.create(Double.class, Integer.class), (Number p1, Number p2) -> (double) (p1.doubleValue() - p2.intValue()));
            this.functions.put(Tuples.create(Double.class, Short.class), (Number p1, Number p2) -> (double) (p1.doubleValue() - p2.shortValue()));
            this.functions.put(Tuples.create(Double.class, Byte.class), (Number p1, Number p2) -> (double) (p1.doubleValue() - p2.byteValue()));
            this.functions.put(Tuples.create(Double.class, Float.class), (Number p1, Number p2) -> (double) (p1.doubleValue() - p2.floatValue()));
            this.functions.put(Tuples.create(Double.class, Double.class), (Number p1, Number p2) -> (double) (p1.doubleValue() - p2.doubleValue()));

        }

    }

    public static class DefaultMult extends TypedBiFunction<Number, Number, Number> {

        public DefaultMult() {

            this.functions.put(Tuples.create(Long.class, Long.class), (Number p1, Number p2) -> (long) (p1.longValue() * p2.longValue()));
            this.functions.put(Tuples.create(Long.class, Integer.class), (Number p1, Number p2) -> (long) (p1.longValue() * p2.intValue()));
            this.functions.put(Tuples.create(Long.class, Byte.class), (Number p1, Number p2) -> (long) (p1.longValue() * p2.byteValue()));
            this.functions.put(Tuples.create(Long.class, Short.class), (Number p1, Number p2) -> (long) (p1.longValue() * p2.shortValue()));

            this.functions.put(Tuples.create(Integer.class, Long.class), (Number p1, Number p2) -> (int) (p1.longValue() * p2.longValue()));
            this.functions.put(Tuples.create(Integer.class, Integer.class), (Number p1, Number p2) -> (int) (p1.intValue() * p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Byte.class), (Number p1, Number p2) -> (int) (p1.intValue() * p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Short.class), (Number p1, Number p2) -> (int) (p1.intValue() * p2.shortValue()));

            this.functions.put(Tuples.create(Short.class, Long.class), (Number p1, Number p2) -> (short) (p1.shortValue() * p2.longValue()));
            this.functions.put(Tuples.create(Short.class, Integer.class), (Number p1, Number p2) -> (short) (p1.shortValue() * p2.intValue()));
            this.functions.put(Tuples.create(Short.class, Short.class), (Number p1, Number p2) -> (short) (p1.shortValue() * p2.shortValue()));
            this.functions.put(Tuples.create(Short.class, Byte.class), (Number p1, Number p2) -> (short) (p1.shortValue() * p2.byteValue()));

            this.functions.put(Tuples.create(Byte.class, Long.class), (Number p1, Number p2) -> (byte) (p1.byteValue() * p2.longValue()));
            this.functions.put(Tuples.create(Byte.class, Integer.class), (Number p1, Number p2) -> (byte) (p1.byteValue() * p2.intValue()));
            this.functions.put(Tuples.create(Byte.class, Short.class), (Number p1, Number p2) -> (byte) (p1.byteValue() * p2.shortValue()));
            this.functions.put(Tuples.create(Byte.class, Byte.class), (Number p1, Number p2) -> (byte) (p1.byteValue() * p2.byteValue()));

            this.functions.put(Tuples.create(Float.class, Long.class), (Number p1, Number p2) -> (float) (p1.floatValue() * p2.longValue()));
            this.functions.put(Tuples.create(Float.class, Integer.class), (Number p1, Number p2) -> (float) (p1.floatValue() * p2.intValue()));
            this.functions.put(Tuples.create(Float.class, Short.class), (Number p1, Number p2) -> (float) (p1.floatValue() * p2.shortValue()));
            this.functions.put(Tuples.create(Float.class, Byte.class), (Number p1, Number p2) -> (float) (p1.floatValue() * p2.byteValue()));
            this.functions.put(Tuples.create(Float.class, Float.class), (Number p1, Number p2) -> (float) (p1.floatValue() * p2.floatValue()));
            this.functions.put(Tuples.create(Float.class, Double.class), (Number p1, Number p2) -> (float) (p1.floatValue() * p2.doubleValue()));

            this.functions.put(Tuples.create(Double.class, Long.class), (Number p1, Number p2) -> (double) (p1.doubleValue() * p2.longValue()));
            this.functions.put(Tuples.create(Double.class, Integer.class), (Number p1, Number p2) -> (double) (p1.doubleValue() * p2.intValue()));
            this.functions.put(Tuples.create(Double.class, Short.class), (Number p1, Number p2) -> (double) (p1.doubleValue() * p2.shortValue()));
            this.functions.put(Tuples.create(Double.class, Byte.class), (Number p1, Number p2) -> (double) (p1.doubleValue() * p2.byteValue()));
            this.functions.put(Tuples.create(Double.class, Float.class), (Number p1, Number p2) -> (double) (p1.doubleValue() * p2.floatValue()));
            this.functions.put(Tuples.create(Double.class, Double.class), (Number p1, Number p2) -> (double) (p1.doubleValue() * p2.doubleValue()));

        }

    }

    public static class DefaultDiv extends TypedBiFunction<Number, Number, Number> {

        public DefaultDiv() {

            this.functions.put(Tuples.create(Long.class, Long.class), (Number p1, Number p2) -> (long) (p1.longValue() / p2.longValue()));
            this.functions.put(Tuples.create(Long.class, Integer.class), (Number p1, Number p2) -> (long) (p1.longValue() / p2.intValue()));
            this.functions.put(Tuples.create(Long.class, Byte.class), (Number p1, Number p2) -> (long) (p1.longValue() / p2.byteValue()));
            this.functions.put(Tuples.create(Long.class, Short.class), (Number p1, Number p2) -> (long) (p1.longValue() / p2.shortValue()));

            this.functions.put(Tuples.create(Integer.class, Long.class), (Number p1, Number p2) -> (int) (p1.longValue() / p2.longValue()));
            this.functions.put(Tuples.create(Integer.class, Integer.class), (Number p1, Number p2) -> (int) (p1.intValue() / p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Byte.class), (Number p1, Number p2) -> (int) (p1.intValue() / p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Short.class), (Number p1, Number p2) -> (int) (p1.intValue() / p2.shortValue()));

            this.functions.put(Tuples.create(Short.class, Long.class), (Number p1, Number p2) -> (short) (p1.shortValue() / p2.longValue()));
            this.functions.put(Tuples.create(Short.class, Integer.class), (Number p1, Number p2) -> (short) (p1.shortValue() / p2.intValue()));
            this.functions.put(Tuples.create(Short.class, Short.class), (Number p1, Number p2) -> (short) (p1.shortValue() / p2.shortValue()));
            this.functions.put(Tuples.create(Short.class, Byte.class), (Number p1, Number p2) -> (short) (p1.shortValue() / p2.byteValue()));

            this.functions.put(Tuples.create(Byte.class, Long.class), (Number p1, Number p2) -> (byte) (p1.byteValue() / p2.longValue()));
            this.functions.put(Tuples.create(Byte.class, Integer.class), (Number p1, Number p2) -> (byte) (p1.byteValue() / p2.intValue()));
            this.functions.put(Tuples.create(Byte.class, Short.class), (Number p1, Number p2) -> (byte) (p1.byteValue() / p2.shortValue()));
            this.functions.put(Tuples.create(Byte.class, Byte.class), (Number p1, Number p2) -> (byte) (p1.byteValue() / p2.byteValue()));

            this.functions.put(Tuples.create(Float.class, Long.class), (Number p1, Number p2) -> (float) (p1.floatValue() / p2.longValue()));
            this.functions.put(Tuples.create(Float.class, Integer.class), (Number p1, Number p2) -> (float) (p1.floatValue() / p2.intValue()));
            this.functions.put(Tuples.create(Float.class, Short.class), (Number p1, Number p2) -> (float) (p1.floatValue() / p2.shortValue()));
            this.functions.put(Tuples.create(Float.class, Byte.class), (Number p1, Number p2) -> (float) (p1.floatValue() / p2.byteValue()));
            this.functions.put(Tuples.create(Float.class, Float.class), (Number p1, Number p2) -> (float) (p1.floatValue() / p2.floatValue()));
            this.functions.put(Tuples.create(Float.class, Double.class), (Number p1, Number p2) -> (float) (p1.floatValue() / p2.doubleValue()));

            this.functions.put(Tuples.create(Double.class, Long.class), (Number p1, Number p2) -> (double) (p1.doubleValue() / p2.longValue()));
            this.functions.put(Tuples.create(Double.class, Integer.class), (Number p1, Number p2) -> (double) (p1.doubleValue() / p2.intValue()));
            this.functions.put(Tuples.create(Double.class, Short.class), (Number p1, Number p2) -> (double) (p1.doubleValue() / p2.shortValue()));
            this.functions.put(Tuples.create(Double.class, Byte.class), (Number p1, Number p2) -> (double) (p1.doubleValue() / p2.byteValue()));
            this.functions.put(Tuples.create(Double.class, Float.class), (Number p1, Number p2) -> (double) (p1.doubleValue() / p2.floatValue()));
            this.functions.put(Tuples.create(Double.class, Double.class), (Number p1, Number p2) -> (double) (p1.doubleValue() / p2.doubleValue()));

        }

    }
    
    public static class DefaultMod extends TypedBiFunction<Number, Number, Number> {

        public DefaultMod() {

            this.functions.put(Tuples.create(Long.class, Long.class), (Number p1, Number p2) -> (long) (p1.longValue() % p2.longValue()));
            this.functions.put(Tuples.create(Long.class, Integer.class), (Number p1, Number p2) -> (long) (p1.longValue() % p2.intValue()));
            this.functions.put(Tuples.create(Long.class, Byte.class), (Number p1, Number p2) -> (long) (p1.longValue() % p2.byteValue()));
            this.functions.put(Tuples.create(Long.class, Short.class), (Number p1, Number p2) -> (long) (p1.longValue() % p2.shortValue()));

            this.functions.put(Tuples.create(Integer.class, Long.class), (Number p1, Number p2) -> (int) (p1.longValue() % p2.longValue()));
            this.functions.put(Tuples.create(Integer.class, Integer.class), (Number p1, Number p2) -> (int) (p1.intValue() % p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Byte.class), (Number p1, Number p2) -> (int) (p1.intValue() % p2.intValue()));
            this.functions.put(Tuples.create(Integer.class, Short.class), (Number p1, Number p2) -> (int) (p1.intValue() % p2.shortValue()));

            this.functions.put(Tuples.create(Short.class, Long.class), (Number p1, Number p2) -> (short) (p1.shortValue() % p2.longValue()));
            this.functions.put(Tuples.create(Short.class, Integer.class), (Number p1, Number p2) -> (short) (p1.shortValue() % p2.intValue()));
            this.functions.put(Tuples.create(Short.class, Short.class), (Number p1, Number p2) -> (short) (p1.shortValue() % p2.shortValue()));
            this.functions.put(Tuples.create(Short.class, Byte.class), (Number p1, Number p2) -> (short) (p1.shortValue() % p2.byteValue()));

            this.functions.put(Tuples.create(Byte.class, Long.class), (Number p1, Number p2) -> (byte) (p1.byteValue() % p2.longValue()));
            this.functions.put(Tuples.create(Byte.class, Integer.class), (Number p1, Number p2) -> (byte) (p1.byteValue() % p2.intValue()));
            this.functions.put(Tuples.create(Byte.class, Short.class), (Number p1, Number p2) -> (byte) (p1.byteValue() % p2.shortValue()));
            this.functions.put(Tuples.create(Byte.class, Byte.class), (Number p1, Number p2) -> (byte) (p1.byteValue() % p2.byteValue()));

            this.functions.put(Tuples.create(Float.class, Long.class), (Number p1, Number p2) -> (float) (p1.floatValue() % p2.longValue()));
            this.functions.put(Tuples.create(Float.class, Integer.class), (Number p1, Number p2) -> (float) (p1.floatValue() % p2.intValue()));
            this.functions.put(Tuples.create(Float.class, Short.class), (Number p1, Number p2) -> (float) (p1.floatValue() % p2.shortValue()));
            this.functions.put(Tuples.create(Float.class, Byte.class), (Number p1, Number p2) -> (float) (p1.floatValue() % p2.byteValue()));
            this.functions.put(Tuples.create(Float.class, Float.class), (Number p1, Number p2) -> (float) (p1.floatValue() % p2.floatValue()));
            this.functions.put(Tuples.create(Float.class, Double.class), (Number p1, Number p2) -> (float) (p1.floatValue() % p2.doubleValue()));

            this.functions.put(Tuples.create(Double.class, Long.class), (Number p1, Number p2) -> (double) (p1.doubleValue() % p2.longValue()));
            this.functions.put(Tuples.create(Double.class, Integer.class), (Number p1, Number p2) -> (double) (p1.doubleValue() % p2.intValue()));
            this.functions.put(Tuples.create(Double.class, Short.class), (Number p1, Number p2) -> (double) (p1.doubleValue() % p2.shortValue()));
            this.functions.put(Tuples.create(Double.class, Byte.class), (Number p1, Number p2) -> (double) (p1.doubleValue() % p2.byteValue()));
            this.functions.put(Tuples.create(Double.class, Float.class), (Number p1, Number p2) -> (double) (p1.doubleValue() % p2.floatValue()));
            this.functions.put(Tuples.create(Double.class, Double.class), (Number p1, Number p2) -> (double) (p1.doubleValue() % p2.doubleValue()));

        }

    }
}
