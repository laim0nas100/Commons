package lt.lb.commons.parsing.token;

/**
 *
 * @author laim0nas100
 */
public class LiteralString extends Literal {

    public LiteralString(String value, TokenPos pos) {
        super(value, pos);
    }

    @Override
    public String toString() {
        return "String " + super.toString();
    }

}
