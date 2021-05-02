package lt.lb.commons.parsing.token;

import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class Token {

    public final String value;
    public final TokenPos pos;

    public Token(String value, TokenPos pos) {
        this.value = value;
        this.pos = Objects.requireNonNull(pos);

    }

    public int getLen() {
        return this.value.length();
    }

    @Override
    public String toString() {
        return this.pos + ":" + this.value + ":";
    }
}
