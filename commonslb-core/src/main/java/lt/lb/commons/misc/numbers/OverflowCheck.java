package lt.lb.commons.misc.numbers;

/**
 * Simple number addition overflow check.
 *
 * @author laim0nas100
 */
public class OverflowCheck {

    public static boolean willOverflowIfAdd(float a, float b) {
        return willOverflowIfAdd(a, b, Float.MIN_VALUE, Float.MAX_VALUE);
    }

    public static boolean willOverflowIfAdd(double a, double b) {
        return willOverflowIfAdd(a, b, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public static boolean willOverflowIfAdd(double a, double b, double minValue, double maxValue) {
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("Invalid range [" + minValue + ";" + maxValue + "]");
        }
        if (a > 0 && b > 0) {//both positive
            return maxValue - a < b;
        } else if (a < 0 && b < 0) { // both negative
            return minValue - a > b;
        }
        return false;
    }

    public static boolean willOverflowIfAdd(byte a, byte b) {
        return willOverflowIfAdd(a, b, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public static boolean willOverflowIfAdd(short a, short b) {
        return willOverflowIfAdd(a, b, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public static boolean willOverflowIfAdd(int a, int b) {
        return willOverflowIfAdd(a, b, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static boolean willOverflowIfAdd(long a, long b) {
        return willOverflowIfAdd(a, b, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public static boolean willOverflowIfAdd(long a, long b, long minValue, long maxValue) {
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("Invalid range [" + minValue + ";" + maxValue + "]");
        }
        if (a > 0 && b > 0) {//both positive
            return maxValue - a < b;
        } else if (a < 0 && b < 0) { // both negative
            return minValue - a > b;
        }
        return false;
    }
}
