package lt.lb.commons.parsing.token.match.impl;

import lt.lb.commons.parsing.token.Token;

/**
 *
 * @author laim0nas100
 */
public class AnyTokenMatcher extends BaseTokenMatcher {

    public AnyTokenMatcher(int length, String name) {
        super(length, name);
    }

    @Override
    public boolean matches(int position, Token token) {
        return true;
    }

}
