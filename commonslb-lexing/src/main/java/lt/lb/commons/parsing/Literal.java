package lt.lb.commons.parsing;

/**
 *
 * @author laim0nas100
 */
public class Literal extends Token {

    public Literal(String value, int[] pos) {
        super(value, pos);
    }

    @Override
    public String toString() {
        return "Literal " + super.toString();
    }

}
