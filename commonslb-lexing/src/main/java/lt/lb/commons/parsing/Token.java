package lt.lb.commons.parsing;

import java.util.Arrays;

/**
 *
 * @author laim0nas100
 */
public class Token {

    public final String value;
    public final Integer[] pos;

    public Token(String value, Integer[] pos) {
        this.value = value;
        this.pos = pos;

    }

    public int getLen() {
        return this.value.length();
    }

    @Override
    public String toString() {
        return Arrays.toString(this.pos) + ":" + this.value + ":";
    }
}
