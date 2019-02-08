package lt.lb.commons.parsing;

import java.util.Optional;

/**
 *
 * @author laim0nas100
 */
public class NumberParsing {

    public static Optional<Long> parseLong(boolean unsigned, int radix, String str) {
        try {
            if (unsigned) {
                return Optional.of(Long.parseUnsignedLong(str, radix));
            } else {
                return Optional.of(Long.parseLong(str, radix));
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parseLong(String str) {
        return parseLong(false, 10, str);
    }

    public static Optional<Integer> parseInt(boolean unsigned, int radix, String str) {
        try {
            if (unsigned) {
                return Optional.of(Integer.parseInt(str, radix));
            } else {
                return Optional.of(Integer.parseUnsignedInt(str, radix));
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseInt(String str) {
        return parseInt(false, 10, str);
    }

    public static Optional<Double> parseDouble(String str) {
        try {
            return Optional.of(Double.parseDouble(str));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Float> parseFloat(String str) {
        try {
            return Optional.of(Float.parseFloat(str));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
