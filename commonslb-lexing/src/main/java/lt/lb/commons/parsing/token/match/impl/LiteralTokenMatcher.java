package lt.lb.commons.parsing.token.match.impl;

import lt.lb.commons.parsing.token.Literal;
import lt.lb.commons.parsing.token.Token;

/**
 *
 * @author laim0nas100
 */
public class LiteralTokenMatcher extends ExactTokenMatcher {

    public LiteralTokenMatcher(boolean ignoreCase, String name, String word) {
        super(ignoreCase, name, word);
    }

    @Override
    public Class<? extends Token> requiredType(int position) {
        return Literal.class;
    }

}
