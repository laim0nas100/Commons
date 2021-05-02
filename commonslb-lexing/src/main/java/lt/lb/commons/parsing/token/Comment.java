package lt.lb.commons.parsing.token;

import java.util.Objects;


/**
 *
 * @author laim0nas100
 */
public class Comment extends Token {

    public final TokenPos endPos;

    public Comment(String value, TokenPos pos, TokenPos endPos) {
        super(value, pos);
        this.endPos = Objects.requireNonNull(endPos);
    }

    @Override
    public String toString() {
        return "Comment" + super.toString() + " " + endPos;
    }

}
