package lt.lb.commons.parsing;

/**
 *
 * @author laim0nas100
 */
public class LexerWithStrings extends Lexer {

    public LexerWithStrings() {
        this.defaultSet();
    }
    private void defaultSet() {
        this.prepareForStrings("\"", "\"", "\\");
        this.skipWhitespace = true;
    }

}
