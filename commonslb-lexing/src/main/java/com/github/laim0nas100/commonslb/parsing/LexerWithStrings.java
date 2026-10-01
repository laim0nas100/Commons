package com.github.laim0nas100.commonslb.parsing;

/**
 *
 * @author laim0nas100
 */
@Deprecated
public class LexerWithStrings extends Lexer {

    public LexerWithStrings() {
        this.defaultSet();
    }
    private void defaultSet() {
        this.prepareForStrings("\"", "\"", "\\");
        this.skipWhitespace = true;
    }

}
