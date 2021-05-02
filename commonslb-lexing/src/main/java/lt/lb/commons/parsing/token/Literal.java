package lt.lb.commons.parsing.token;

/**
 *
 * @author laim0nas100
 */
public class Literal extends Token {

    public Literal(String value, TokenPos pos) {
        super(value, pos);
    }

    @Override
    public String toString() {
        return "Literal " + super.toString();
    }

}
