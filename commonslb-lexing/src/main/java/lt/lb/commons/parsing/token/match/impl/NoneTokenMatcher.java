package lt.lb.commons.parsing.token.match.impl;

import lt.lb.commons.parsing.token.Token;
import lt.lb.commons.parsing.token.TokenPos;

/**
 *
 * @author laim0nas100
 */
public class NoneTokenMatcher extends BaseTokenMatcher {

    public static class TokenNone extends Token {

        protected TokenNone(String value, TokenPos pos) {
            super(value, pos);
        }

    }

    public NoneTokenMatcher() {
        this(0, "None");
    }

    public NoneTokenMatcher(int length, String name) {
        super(length, name);
    }

    @Override
    public boolean matches(int position, Token token) {
        return false;
    }

    @Override
    public Class<? extends Token> requiredType(int position) {
        return TokenNone.class;
    }

}
