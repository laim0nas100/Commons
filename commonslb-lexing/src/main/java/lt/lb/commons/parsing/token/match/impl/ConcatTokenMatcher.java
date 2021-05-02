package lt.lb.commons.parsing.token.match.impl;

import lt.lb.commons.parsing.token.Token;
import lt.lb.commons.parsing.token.match.TokenMatcher;

/**
 *
 * @author laim0nas100
 */
public class ConcatTokenMatcher extends CompositeTokenMatcher {

    public ConcatTokenMatcher(String name, TokenMatcher... matchers) {
        super(sumLength(matchers), name, matchers);
    }

    @Override
    public boolean matches(int position, Token token) {
        int i = 0;
        for (TokenMatcher m : matchers) {
            int len = m.length();
            if (position >= len) {
                position -= len;
                i++;
            } else {
                return matchers[i].matches(position, token);
            }

        }
        return false;
    }

    @Override
    public Class<? extends Token> requiredType(int position) {
        int i = 0;
        for (TokenMatcher m : matchers) {
            int len = m.length();
            if (position >= len) {
                position -= len;
                i++;
            } else {
                return matchers[i].requiredType(position);
            }

        }
        return super.requiredType(position);
    }

}
