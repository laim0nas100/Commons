package lt.lb.commons.parsing.numbers;

/**
 *
 * @author laim0nas100 number parsing with no exceptions.
 */
public class FastParse {

    public static Integer parseInt(CharSequence s) {
        return parseInt(s, 10);
    }

    /**
     * Same as {@link Integer#parseInt(java.lang.String) } but with no
     * exceptions, so much faster during failures
     *
     * @param s
     * @param radix
     * @return parsed int or null
     */
    public static Integer parseInt(CharSequence s, int radix) {

        if (s == null) {
            return null;
        }

        if (radix < Character.MIN_RADIX) {
            return null;
        }

        if (radix > Character.MAX_RADIX) {
            return null;
        }

        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    return null;
                }

                if (len == 1) { // Cannot have lone "+" or "-"
                    return null;
                }
                i++;
            }
            int multmin = limit / radix;
            int result = 0;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                int digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0 || result < multmin) {
                    return null;
                }
                result *= radix;
                if (result < limit + digit) {
                    return null;
                }
                result -= digit;
            }
            return negative ? result : -result;
        } else {
            return null;
        }
    }

    public static Integer parseInt(CharSequence s, int beginIndex, int endIndex, int radix) {
        if (s == null) {
            return null;
        }

        if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length()) {
            return null;
        }
        if (radix < Character.MIN_RADIX) {
            return null;
        }
        if (radix > Character.MAX_RADIX) {
            return null;
        }

        boolean negative = false;
        int i = beginIndex;
        int limit = -Integer.MAX_VALUE;

        if (i < endIndex) {
            char firstChar = s.charAt(i);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    return null;
                }
                i++;
                if (i == endIndex) { // Cannot have lone "+" or "-"
                    return null;
                }
            }
            int multmin = limit / radix;
            int result = 0;
            while (i < endIndex) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                int digit = Character.digit(s.charAt(i), radix);
                if (digit < 0 || result < multmin) {
                    return null;
                }
                result *= radix;
                if (result < limit + digit) {
                    return null;
                }
                i++;
                result -= digit;
            }
            return negative ? result : -result;
        } else {
            return null;
        }
    }

    public static Integer parseUnsignedInt(CharSequence s) {
        return parseUnsignedInt(s, 10);
    }

    public static Integer parseUnsignedInt(CharSequence s, int radix) {
        if (s == null) {
            return null;
        }

        int len = s.length();
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                return null;
            } else {
                if (len <= 5
                        || // Integer.MAX_VALUE in Character.MAX_RADIX is 6 digits
                        (radix == 10 && len <= 9)) { // Integer.MAX_VALUE in base 10 is 10 digits
                    return parseInt(s, radix);
                } else {
                    Long ell = parseLong(s, radix);
                    if (ell == null) {
                        return null;
                    }
                    if ((ell & 0xffff_ffff_0000_0000L) == 0) {
                        return ell.intValue();
                    } else {
                        return null;
                    }
                }
            }
        } else {
            return null;
        }
    }

    public static Integer parseUnsignedInt(CharSequence s, int beginIndex, int endIndex, int radix) {
        if (s == null) {
            return null;
        }

        if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length()) {
            return null;
        }
        int start = beginIndex, len = endIndex - beginIndex;

        if (len > 0) {
            char firstChar = s.charAt(start);
            if (firstChar == '-') {
                return null;
            } else {
                if (len <= 5
                        || // Integer.MAX_VALUE in Character.MAX_RADIX is 6 digits
                        (radix == 10 && len <= 9)) { // Integer.MAX_VALUE in base 10 is 10 digits
                    return parseInt(s, start, start + len, radix);
                } else {
                    Long ell = parseLong(s, start, start + len, radix);
                    if (ell == null) {
                        return null;
                    }
                    if ((ell & 0xffff_ffff_0000_0000L) == 0) {
                        return ell.intValue();
                    } else {
                        return null;
                    }
                }
            }
        } else {
            return null;
        }
    }

    public static Long parseLong(CharSequence s) {
        return parseLong(s, 10);
    }

    /**
     * Same as {@link Long#parseLong(java.lang.String) } but with no exceptions,
     * so much faster during failures
     *
     * @param s
     * @param radix
     * @return parsed long or null
     */
    public static Long parseLong(CharSequence s, int radix) {
        if (s == null) {
            return null;
        }
        if (radix < Character.MIN_RADIX) {
            return null;
        }

        if (radix > Character.MAX_RADIX) {
            return null;
        }

        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+') {
                    return null;
                }

                if (len == 1) { // Cannot have lone "+" or "-"
                    return null;
                }
                i++;
            }
            long multmin = limit / radix;
            long result = 0;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                int digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0 || result < multmin) {
                    return null;
                }
                result *= radix;
                if (result < limit + digit) {
                    return null;
                }
                result -= digit;
            }
            return negative ? result : -result;
        } else {
            return null;
        }
    }

    public static Long parseLong(CharSequence s, int beginIndex, int endIndex, int radix) {
        if (s == null) {
            return null;
        }

        if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length()) {
            return null;
        }
        if (radix < Character.MIN_RADIX) {
            return null;
        }
        if (radix > Character.MAX_RADIX) {
            return null;
        }

        boolean negative = false;
        int i = beginIndex;
        long limit = -Long.MAX_VALUE;

        if (i < endIndex) {
            char firstChar = s.charAt(i);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+') {
                    return null;
                }
                i++;
            }
            if (i >= endIndex) { // Cannot have lone "+", "-" or ""
                return null;
            }
            long multmin = limit / radix;
            long result = 0;
            while (i < endIndex) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                int digit = Character.digit(s.charAt(i), radix);
                if (digit < 0 || result < multmin) {
                    return null;
                }
                result *= radix;
                if (result < limit + digit) {
                    return null;
                }
                i++;
                result -= digit;
            }
            return negative ? result : -result;
        } else {
            return null;
        }
    }

    public static Long parseUnsignedLong(CharSequence s) {
        return parseUnsignedLong(s, 10);
    }

    public static Long parseUnsignedLong(CharSequence s, int radix) {
        if (s == null) {
            return null;
        }

        int len = s.length();
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                return null;
            } else {
                if (len <= 12
                        || // Long.MAX_VALUE in Character.MAX_RADIX is 13 digits
                        (radix == 10 && len <= 18)) { // Long.MAX_VALUE in base 10 is 19 digits
                    return parseLong(s, radix);
                }

                // No need for range checks on len due to testing above.
                Long first = parseLong(s, 0, len - 1, radix);
                if (first == null) {
                    return null;
                }
                int second = Character.digit(s.charAt(len - 1), radix);
                if (second < 0) {
                    return null;
                }
                long result = first * radix + second;

                /*
                 * Test leftmost bits of multiprecision extension of first*radix
                 * for overflow. The number of bits needed is defined by
                 * GUARD_BIT = ceil(log2(Character.MAX_RADIX)) + 1 = 7. Then
                 * int guard = radix*(int)(first >>> (64 - GUARD_BIT)) and
                 * overflow is tested by splitting guard in the ranges
                 * guard < 92, 92 <= guard < 128, and 128 <= guard, where
                 * 92 = 128 - Character.MAX_RADIX. Note that guard cannot take
                 * on a value which does not include a prime factor in the legal
                 * radix range.
                 */
                int guard = radix * (int) (first >>> 57);
                if (guard >= 128
                        || (result >= 0 && guard >= 128 - Character.MAX_RADIX)) {
                    /*
                     * For purposes of exposition, the programmatic statements
                     * below should be taken to be multi-precision, i.e., not
                     * subject to overflow.
                     *
                     * A) Condition guard >= 128:
                     * If guard >= 128 then first*radix >= 2^7 * 2^57 = 2^64
                     * hence always overflow.
                     *
                     * B) Condition guard < 92:
                     * Define left7 = first >>> 57.
                     * Given first = (left7 * 2^57) + (first & (2^57 - 1)) then
                     * result <= (radix*left7)*2^57 + radix*(2^57 - 1) + second.
                     * Thus if radix*left7 < 92, radix <= 36, and second < 36,
                     * then result < 92*2^57 + 36*(2^57 - 1) + 36 = 2^64 hence
                     * never overflow.
                     *
                     * C) Condition 92 <= guard < 128:
                     * first*radix + second >= radix*left7*2^57 + second
                     * so that first*radix + second >= 92*2^57 + 0 > 2^63
                     *
                     * D) Condition guard < 128:
                     * radix*first <= (radix*left7) * 2^57 + radix*(2^57 - 1)
                     * so
                     * radix*first + second <= (radix*left7) * 2^57 + radix*(2^57 - 1) + 36
                     * thus
                     * radix*first + second < 128 * 2^57 + 36*2^57 - radix + 36
                     * whence
                     * radix*first + second < 2^64 + 2^6*2^57 = 2^64 + 2^63
                     *
                     * E) Conditions C, D, and result >= 0:
                     * C and D combined imply the mathematical result
                     * 2^63 < first*radix + second < 2^64 + 2^63. The lower
                     * bound is therefore negative as a signed long, but the
                     * upper bound is too small to overflow again after the
                     * signed long overflows to positive above 2^64 - 1. Hence
                     * result >= 0 implies overflow given C and D.
                     */
                    return null;
                }
                return result;
            }
        } else {
            return null;
        }
    }

    public static Long parseUnsignedLong(CharSequence s, int beginIndex, int endIndex, int radix){
        if (s == null) {
            return null;
        }
        if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length()) {
            return null;
        }
        int start = beginIndex, len = endIndex - beginIndex;

        if (len > 0) {
            char firstChar = s.charAt(start);
            if (firstChar == '-') {
                return null;
            } else {
                if (len <= 12
                        || // Long.MAX_VALUE in Character.MAX_RADIX is 13 digits
                        (radix == 10 && len <= 18)) { // Long.MAX_VALUE in base 10 is 19 digits
                    return parseLong(s, start, start + len, radix);
                }

                // No need for range checks on end due to testing above.
                long first = parseLong(s, start, start + len - 1, radix);
                int second = Character.digit(s.charAt(start + len - 1), radix);
                if (second < 0) {
                    return null;
                }
                long result = first * radix + second;

                /*
                 * Test leftmost bits of multiprecision extension of first*radix
                 * for overflow. The number of bits needed is defined by
                 * GUARD_BIT = ceil(log2(Character.MAX_RADIX)) + 1 = 7. Then
                 * int guard = radix*(int)(first >>> (64 - GUARD_BIT)) and
                 * overflow is tested by splitting guard in the ranges
                 * guard < 92, 92 <= guard < 128, and 128 <= guard, where
                 * 92 = 128 - Character.MAX_RADIX. Note that guard cannot take
                 * on a value which does not include a prime factor in the legal
                 * radix range.
                 */
                int guard = radix * (int) (first >>> 57);
                if (guard >= 128
                        || (result >= 0 && guard >= 128 - Character.MAX_RADIX)) {
                    /*
                     * For purposes of exposition, the programmatic statements
                     * below should be taken to be multi-precision, i.e., not
                     * subject to overflow.
                     *
                     * A) Condition guard >= 128:
                     * If guard >= 128 then first*radix >= 2^7 * 2^57 = 2^64
                     * hence always overflow.
                     *
                     * B) Condition guard < 92:
                     * Define left7 = first >>> 57.
                     * Given first = (left7 * 2^57) + (first & (2^57 - 1)) then
                     * result <= (radix*left7)*2^57 + radix*(2^57 - 1) + second.
                     * Thus if radix*left7 < 92, radix <= 36, and second < 36,
                     * then result < 92*2^57 + 36*(2^57 - 1) + 36 = 2^64 hence
                     * never overflow.
                     *
                     * C) Condition 92 <= guard < 128:
                     * first*radix + second >= radix*left7*2^57 + second
                     * so that first*radix + second >= 92*2^57 + 0 > 2^63
                     *
                     * D) Condition guard < 128:
                     * radix*first <= (radix*left7) * 2^57 + radix*(2^57 - 1)
                     * so
                     * radix*first + second <= (radix*left7) * 2^57 + radix*(2^57 - 1) + 36
                     * thus
                     * radix*first + second < 128 * 2^57 + 36*2^57 - radix + 36
                     * whence
                     * radix*first + second < 2^64 + 2^6*2^57 = 2^64 + 2^63
                     *
                     * E) Conditions C, D, and result >= 0:
                     * C and D combined imply the mathematical result
                     * 2^63 < first*radix + second < 2^64 + 2^63. The lower
                     * bound is therefore negative as a signed long, but the
                     * upper bound is too small to overflow again after the
                     * signed long overflows to positive above 2^64 - 1. Hence
                     * result >= 0 implies overflow given C and D.
                     */
                    return null;
                }
                return result;
            }
        } else {
            return null;
        }
    }

    public static Float parseFloat(CharSequence s) {
        return FastFloatingDecimal.parseFloat(s);
    }

    public static Double parseDouble(CharSequence s) {
        return FastFloatingDecimal.parseDouble(s);
    }

}
