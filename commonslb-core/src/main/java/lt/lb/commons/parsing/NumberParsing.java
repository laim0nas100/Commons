package lt.lb.commons.parsing;

import java.util.Optional;
import lt.lb.commons.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class NumberParsing {

    public static Optional<Long> parseLong(boolean unsigned, int radix, String str) {
        return SafeOpt.ofNullable(str).map(s ->{
            if(unsigned){
                return Long.parseUnsignedLong(s, radix);
            }else{
                return Long.parseLong(s);
            }
        }).asOptional();
    }

    public static Optional<Long> parseLong(String str) {
        return parseLong(false, 10, str);
    }

    public static Optional<Integer> parseInt(boolean unsigned, int radix, String str) {
        return SafeOpt.ofNullable(str).map(s ->{
            if (unsigned) {
                return Integer.parseUnsignedInt(str, radix);
            } else {
                return Integer.parseInt(str, radix);
            }
        }).asOptional();
    }

    public static Optional<Integer> parseInt(String str) {
        return parseInt(false, 10, str);
    }

    public static Optional<Double> parseDouble(String str) {
        return SafeOpt.ofNullable(str).map(s -> Double.parseDouble(s)).asOptional();
    }

    public static Optional<Float> parseFloat(String str) {
        return SafeOpt.ofNullable(str).map(s -> Float.parseFloat(s)).asOptional();
    }

}
