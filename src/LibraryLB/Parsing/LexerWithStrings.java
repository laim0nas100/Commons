/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Parsing;

import java.util.Collection;

/**
 *
 * @author Lemmin
 */
public class LexerWithStrings extends Lexer {

    public LexerWithStrings(Collection<String> lines) {
        super(lines);
        this.defaultSet();
    }

    public LexerWithStrings(String line) {
        super(line);
        this.defaultSet();
    }

    private void defaultSet() {
        this.prepareForStrings("\"", "\"", "\\");
        this.skipWhitespace = true;
    }

}
