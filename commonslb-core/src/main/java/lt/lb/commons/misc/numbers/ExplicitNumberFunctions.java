/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc.numbers;

/**
 *
 * @author laim0nas100
 */
public class ExplicitNumberFunctions {

    public static class BiFunctionsInteger {

        public static Integer plus(Integer i, Number n) {
            if (n instanceof Integer) {
                return (int) (i + n.intValue());
            }
            if (n instanceof Long) {
                return (int) (i + n.longValue());
            }
            if (n instanceof Short) {
                return (int) (i + n.shortValue());
            }
            if (n instanceof Byte) {
                return (int) (i + n.byteValue());
            }
            if (n instanceof Double) {
                return (int) (i + n.doubleValue());
            }
            if (n instanceof Float) {
                return (int) (i + n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Integer minus(Integer i, Number n) {
            if (n instanceof Integer) {
                return (int) (i - n.intValue());
            }
            if (n instanceof Long) {
                return (int) (i - n.longValue());
            }
            if (n instanceof Short) {
                return (int) (i - n.shortValue());
            }
            if (n instanceof Byte) {
                return (int) (i - n.byteValue());
            }
            if (n instanceof Double) {
                return (int) (i - n.doubleValue());
            }
            if (n instanceof Float) {
                return (int) (i - n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Integer multiply(Integer i, Number n) {
            if (n instanceof Integer) {
                return (int) (i * n.intValue());
            }
            if (n instanceof Long) {
                return (int) (i * n.longValue());
            }
            if (n instanceof Short) {
                return (int) (i * n.shortValue());
            }
            if (n instanceof Byte) {
                return (int) (i * n.byteValue());
            }
            if (n instanceof Double) {
                return (int) (i * n.doubleValue());
            }
            if (n instanceof Float) {
                return (int) (i * n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Integer divide(Integer i, Number n) {
            if (n instanceof Integer) {
                return (int) (i / n.intValue());
            }
            if (n instanceof Long) {
                return (int) (i / n.longValue());
            }
            if (n instanceof Short) {
                return (int) (i / n.shortValue());
            }
            if (n instanceof Byte) {
                return (int) (i / n.byteValue());
            }
            if (n instanceof Double) {
                return (int) (i / n.doubleValue());
            }
            if (n instanceof Float) {
                return (int) (i / n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Integer mod(Integer i, Number n) {
            if (n instanceof Integer) {
                return (int) (i % n.intValue());
            }
            if (n instanceof Long) {
                return (int) (i % n.longValue());
            }
            if (n instanceof Short) {
                return (int) (i % n.shortValue());
            }
            if (n instanceof Byte) {
                return (int) (i % n.byteValue());
            }
            if (n instanceof Double) {
                return (int) (i % n.doubleValue());
            }
            if (n instanceof Float) {
                return (int) (i % n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }
    }

    public static class BiFunctionsLong {

        public static Long plus(Long i, Number n) {
            if (n instanceof Long) {
                return (long) (i + n.longValue());
            }

            if (n instanceof Integer) {
                return (long) (i + n.intValue());
            }

            if (n instanceof Short) {
                return (long) (i + n.shortValue());
            }
            if (n instanceof Byte) {
                return (long) (i + n.byteValue());
            }
            if (n instanceof Double) {
                return (long) (i + n.doubleValue());
            }
            if (n instanceof Float) {
                return (long) (i + n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Long minus(Long i, Number n) {
            if (n instanceof Long) {
                return (long) (i - n.longValue());
            }
            if (n instanceof Integer) {
                return (long) (i - n.intValue());
            }

            if (n instanceof Short) {
                return (long) (i - n.shortValue());
            }
            if (n instanceof Byte) {
                return (long) (i - n.byteValue());
            }
            if (n instanceof Double) {
                return (long) (i - n.doubleValue());
            }
            if (n instanceof Float) {
                return (long) (i - n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Long multiply(Long i, Number n) {
            if (n instanceof Long) {
                return (long) (i * n.longValue());
            }
            if (n instanceof Integer) {
                return (long) (i * n.intValue());
            }

            if (n instanceof Short) {
                return (long) (i * n.shortValue());
            }
            if (n instanceof Byte) {
                return (long) (i * n.byteValue());
            }
            if (n instanceof Double) {
                return (long) (i * n.doubleValue());
            }
            if (n instanceof Float) {
                return (long) (i * n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Long divide(Long i, Number n) {
            if (n instanceof Long) {
                return (long) (i / n.longValue());
            }

            if (n instanceof Integer) {
                return (long) (i / n.intValue());
            }

            if (n instanceof Short) {
                return (long) (i / n.shortValue());
            }
            if (n instanceof Byte) {
                return (long) (i / n.byteValue());
            }
            if (n instanceof Double) {
                return (long) (i / n.doubleValue());
            }
            if (n instanceof Float) {
                return (long) (i / n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Long mod(Long i, Number n) {
            if (n instanceof Long) {
                return (long) (i % n.longValue());
            }
            if (n instanceof Integer) {
                return (long) (i % n.intValue());
            }

            if (n instanceof Short) {
                return (long) (i % n.shortValue());
            }
            if (n instanceof Byte) {
                return (long) (i % n.byteValue());
            }
            if (n instanceof Double) {
                return (long) (i % n.doubleValue());
            }
            if (n instanceof Float) {
                return (long) (i % n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }
    }

    public static class BiFunctionsDouble {

        public static Double plus(Double i, Number n) {
            if (n instanceof Double) {
                return (double) (i + n.doubleValue());
            }
            if (n instanceof Integer) {
                return (double) (i + n.intValue());
            }
            if (n instanceof Long) {
                return (double) (i + n.longValue());
            }
            if (n instanceof Short) {
                return (double) (i + n.shortValue());
            }
            if (n instanceof Byte) {
                return (double) (i + n.byteValue());
            }

            if (n instanceof Float) {
                return (double) (i + n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Double minus(Double i, Number n) {
            if (n instanceof Double) {
                return (double) (i - n.doubleValue());
            }
            if (n instanceof Integer) {
                return (double) (i - n.intValue());
            }
            if (n instanceof Long) {
                return (double) (i - n.longValue());
            }
            if (n instanceof Short) {
                return (double) (i - n.shortValue());
            }
            if (n instanceof Byte) {
                return (double) (i - n.byteValue());
            }

            if (n instanceof Float) {
                return (double) (i - n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Double multiply(Double i, Number n) {
            if (n instanceof Double) {
                return (double) (i * n.doubleValue());
            }
            if (n instanceof Integer) {
                return (double) (i * n.intValue());
            }
            if (n instanceof Long) {
                return (double) (i * n.longValue());
            }
            if (n instanceof Short) {
                return (double) (i * n.shortValue());
            }
            if (n instanceof Byte) {
                return (double) (i * n.byteValue());
            }

            if (n instanceof Float) {
                return (double) (i * n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Double divide(Double i, Number n) {
            if (n instanceof Double) {
                return (double) (i / n.doubleValue());
            }
            if (n instanceof Integer) {
                return (double) (i / n.intValue());
            }
            if (n instanceof Long) {
                return (double) (i / n.longValue());
            }
            if (n instanceof Short) {
                return (double) (i / n.shortValue());
            }
            if (n instanceof Byte) {
                return (double) (i / n.byteValue());
            }

            if (n instanceof Float) {
                return (double) (i / n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Double mod(Double i, Number n) {
            if (n instanceof Double) {
                return (double) (i % n.doubleValue());
            }
            if (n instanceof Integer) {
                return (double) (i % n.intValue());
            }
            if (n instanceof Long) {
                return (double) (i % n.longValue());
            }
            if (n instanceof Short) {
                return (double) (i % n.shortValue());
            }
            if (n instanceof Byte) {
                return (double) (i % n.byteValue());
            }

            if (n instanceof Float) {
                return (double) (i % n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }
    }

    public static class BiFunctionsFloat {

        public static Float plus(Float i, Number n) {
            if (n instanceof Float) {
                return (float) (i + n.floatValue());
            }
            if (n instanceof Integer) {
                return (float) (i + n.intValue());
            }
            if (n instanceof Long) {
                return (float) (i + n.longValue());
            }
            if (n instanceof Short) {
                return (float) (i + n.shortValue());
            }
            if (n instanceof Byte) {
                return (float) (i + n.byteValue());
            }
            if (n instanceof Double) {
                return (float) (i + n.doubleValue());
            }

            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Float minus(Float i, Number n) {
            if (n instanceof Float) {
                return (float) (i - n.floatValue());
            }
            if (n instanceof Integer) {
                return (float) (i - n.intValue());
            }
            if (n instanceof Long) {
                return (float) (i - n.longValue());
            }
            if (n instanceof Short) {
                return (float) (i - n.shortValue());
            }
            if (n instanceof Byte) {
                return (float) (i - n.byteValue());
            }
            if (n instanceof Double) {
                return (float) (i - n.doubleValue());
            }

            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Float multiply(Float i, Number n) {
            if (n instanceof Float) {
                return (float) (i * n.floatValue());
            }
            if (n instanceof Integer) {
                return (float) (i * n.intValue());
            }
            if (n instanceof Long) {
                return (float) (i * n.longValue());
            }
            if (n instanceof Short) {
                return (float) (i * n.shortValue());
            }
            if (n instanceof Byte) {
                return (float) (i * n.byteValue());
            }
            if (n instanceof Double) {
                return (float) (i * n.doubleValue());
            }

            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Float divide(Float i, Number n) {
            if (n instanceof Float) {
                return (float) (i / n.floatValue());
            }
            if (n instanceof Integer) {
                return (float) (i / n.intValue());
            }
            if (n instanceof Long) {
                return (float) (i / n.longValue());
            }
            if (n instanceof Short) {
                return (float) (i / n.shortValue());
            }
            if (n instanceof Byte) {
                return (float) (i / n.byteValue());
            }
            if (n instanceof Double) {
                return (float) (i / n.doubleValue());
            }

            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Float mod(Float i, Number n) {
            if (n instanceof Float) {
                return (float) (i % n.floatValue());
            }
            if (n instanceof Integer) {
                return (float) (i % n.intValue());
            }
            if (n instanceof Long) {
                return (float) (i % n.longValue());
            }
            if (n instanceof Short) {
                return (float) (i % n.shortValue());
            }
            if (n instanceof Byte) {
                return (float) (i % n.byteValue());
            }
            if (n instanceof Double) {
                return (float) (i % n.doubleValue());
            }

            throw new IllegalArgumentException("Unrecognized number type");
        }
    }

    public static class BiFunctionsShort {

        public static Short plus(Short i, Number n) {
            if (n instanceof Short) {
                return (short) (i + n.shortValue());
            }
            if (n instanceof Integer) {
                return (short) (i + n.intValue());
            }
            if (n instanceof Long) {
                return (short) (i + n.longValue());
            }

            if (n instanceof Byte) {
                return (short) (i + n.byteValue());
            }
            if (n instanceof Double) {
                return (short) (i + n.doubleValue());
            }
            if (n instanceof Float) {
                return (short) (i + n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Short minus(Short i, Number n) {
            if (n instanceof Short) {
                return (short) (i - n.shortValue());
            }
            if (n instanceof Integer) {
                return (short) (i - n.intValue());
            }
            if (n instanceof Long) {
                return (short) (i - n.longValue());
            }

            if (n instanceof Byte) {
                return (short) (i - n.byteValue());
            }
            if (n instanceof Double) {
                return (short) (i - n.doubleValue());
            }
            if (n instanceof Float) {
                return (short) (i - n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Short multiply(Short i, Number n) {
            if (n instanceof Short) {
                return (short) (i * n.shortValue());
            }
            if (n instanceof Integer) {
                return (short) (i * n.intValue());
            }
            if (n instanceof Long) {
                return (short) (i * n.longValue());
            }

            if (n instanceof Byte) {
                return (short) (i * n.byteValue());
            }
            if (n instanceof Double) {
                return (short) (i * n.doubleValue());
            }
            if (n instanceof Float) {
                return (short) (i * n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Short divide(Short i, Number n) {
            if (n instanceof Short) {
                return (short) (i / n.shortValue());
            }
            if (n instanceof Integer) {
                return (short) (i / n.intValue());
            }
            if (n instanceof Long) {
                return (short) (i / n.longValue());
            }

            if (n instanceof Byte) {
                return (short) (i / n.byteValue());
            }
            if (n instanceof Double) {
                return (short) (i / n.doubleValue());
            }
            if (n instanceof Float) {
                return (short) (i / n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Short mod(Short i, Number n) {
            if (n instanceof Short) {
                return (short) (i % n.shortValue());
            }
            if (n instanceof Integer) {
                return (short) (i % n.intValue());
            }
            if (n instanceof Long) {
                return (short) (i % n.longValue());
            }

            if (n instanceof Byte) {
                return (short) (i % n.byteValue());
            }
            if (n instanceof Double) {
                return (short) (i % n.doubleValue());
            }
            if (n instanceof Float) {
                return (short) (i % n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }
    }

    public static class BiFunctionsByte {

        public static Byte plus(Byte i, Number n) {
            if (n instanceof Byte) {
                return (byte) (i + n.byteValue());
            }
            if (n instanceof Integer) {
                return (byte) (i + n.intValue());
            }
            if (n instanceof Long) {
                return (byte) (i + n.longValue());
            }
            if (n instanceof Short) {
                return (byte) (i + n.shortValue());
            }

            if (n instanceof Double) {
                return (byte) (i + n.doubleValue());
            }
            if (n instanceof Float) {
                return (byte) (i + n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Byte minus(Byte i, Number n) {
            if (n instanceof Byte) {
                return (byte) (i - n.byteValue());
            }
            if (n instanceof Integer) {
                return (byte) (i - n.intValue());
            }
            if (n instanceof Long) {
                return (byte) (i - n.longValue());
            }
            if (n instanceof Short) {
                return (byte) (i - n.shortValue());
            }

            if (n instanceof Double) {
                return (byte) (i - n.doubleValue());
            }
            if (n instanceof Float) {
                return (byte) (i - n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Byte multiply(Byte i, Number n) {
            if (n instanceof Byte) {
                return (byte) (i * n.byteValue());
            }
            if (n instanceof Integer) {
                return (byte) (i * n.intValue());
            }
            if (n instanceof Long) {
                return (byte) (i * n.longValue());
            }
            if (n instanceof Short) {
                return (byte) (i * n.shortValue());
            }

            if (n instanceof Double) {
                return (byte) (i * n.doubleValue());
            }
            if (n instanceof Float) {
                return (byte) (i * n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Byte divide(Byte i, Number n) {
            if (n instanceof Byte) {
                return (byte) (i / n.byteValue());
            }
            if (n instanceof Integer) {
                return (byte) (i / n.intValue());
            }
            if (n instanceof Long) {
                return (byte) (i / n.longValue());
            }
            if (n instanceof Short) {
                return (byte) (i / n.shortValue());
            }

            if (n instanceof Double) {
                return (byte) (i / n.doubleValue());
            }
            if (n instanceof Float) {
                return (byte) (i / n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }

        public static Byte mod(Byte i, Number n) {
            if (n instanceof Byte) {
                return (byte) (i % n.byteValue());
            }
            if (n instanceof Integer) {
                return (byte) (i % n.intValue());
            }
            if (n instanceof Long) {
                return (byte) (i % n.longValue());
            }
            if (n instanceof Short) {
                return (byte) (i % n.shortValue());
            }

            if (n instanceof Double) {
                return (byte) (i % n.doubleValue());
            }
            if (n instanceof Float) {
                return (byte) (i % n.floatValue());
            }
            throw new IllegalArgumentException("Unrecognized number type");
        }
    }
}
