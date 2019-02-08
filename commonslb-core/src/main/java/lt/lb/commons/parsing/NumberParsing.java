package lt.lb.commons.parsing;

import java.util.Optional;

/**
 *
 * @author laim0nas100
 */
public class NumberParsing {

    public Optional<Long> parseLong(boolean unsigned, int radix, String str) {
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

    public Optional<Long> parseLong(String str) {
        return parseLong(false, 10, str);
    }

    public Optional<Integer> parseInt(boolean unsigned, int radix, String str) {
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

    public Optional<Integer> parseInt(String str) {
        return parseInt(false, 10, str);
    }

    public Optional<Double> parseDouble(String str) {
        try {
            return Optional.of(Double.parseDouble(str));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Float> parseFloat(String str) {
        try {
            return Optional.of(Float.parseFloat(str));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
