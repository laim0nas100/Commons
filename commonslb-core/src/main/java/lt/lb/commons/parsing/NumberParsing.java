package lt.lb.commons.parsing;

import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class NumberParsing {

    public static SafeOpt<Long> parseLong(boolean unsigned, int radix, String str) {
        return SafeOpt.ofNullable(str).map(s -> {
            if (unsigned) {
                return Long.parseUnsignedLong(s, radix);
            } else {
                return Long.parseLong(s, radix);
            }
        });
    }

    public static SafeOpt<Long> parseLong(String str) {
        return parseLong(false, 10, str);
    }

    public static SafeOpt<Integer> parseInt(boolean unsigned, int radix, String str) {
        return SafeOpt.ofNullable(str).map(s -> {
            if (unsigned) {
                return Integer.parseUnsignedInt(s, radix);
            } else {
                return Integer.parseInt(s, radix);
            }
        });
    }

    public static SafeOpt<Integer> parseInt(String str) {
        return parseInt(false, 10, str);
    }

    public static SafeOpt<Double> parseDouble(String str) {
        return SafeOpt.ofNullable(str).map(s -> Double.parseDouble(s));
    }

    public static SafeOpt<Float> parseFloat(String str) {
        return SafeOpt.ofNullable(str).map(s -> Float.parseFloat(s));
    }

}
