package lt.lb.commons.parsing;

/**
 *
 * @author laim0nas100
 */
public class LiteralString extends Literal {

    public LiteralString(String value, Integer[] pos) {
        super(value, pos);
    }

    @Override
    public String toString() {
        return "String " + super.toString();
    }
    
    

}
