package lt.lb.commons.parsing;

/**
 *
 * @author laim0nas100
 */
public class LiteralString extends Literal {

    public LiteralString(String value, int[] pos) {
        super(value, pos);
    }

    @Override
    public String toString() {
        return "String " + super.toString();
    }

}
