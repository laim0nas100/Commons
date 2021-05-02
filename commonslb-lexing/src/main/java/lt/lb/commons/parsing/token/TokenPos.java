package lt.lb.commons.parsing.token;

/**
 *
 * @author laim0nas100
 */
public class TokenPos {

    public final int line;
    public final int col;

    public TokenPos(int line, int col) {
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return "[" + line + "," + col + "]";
    }

}
