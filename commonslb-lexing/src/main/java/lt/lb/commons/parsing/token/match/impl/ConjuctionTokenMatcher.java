package lt.lb.commons.parsing.token.match.impl;

import lt.lb.commons.Ins;
import lt.lb.commons.misc.compare.Compare;
import lt.lb.commons.parsing.token.Token;
import lt.lb.commons.parsing.token.match.TokenMatcher;

/**
 *
 * @author laim0nas100
 */
public class ConjuctionTokenMatcher extends CompositeTokenMatcher {

    protected Class<? extends Token>[] maxTypes;

    public ConjuctionTokenMatcher(String name, TokenMatcher... matchers) {
        super(assertSameLength(matchers), name, matchers);

        maxTypes = new Class[length];
        Compare.SimpleCompare<Class> cmpTypes = Compare.of(Ins.typeComparator); // broader types comes first (smaller)
        if (length > 0) {
            for (int pos = 0; pos < length; pos++) {
                maxTypes[pos] = matchers[0].requiredType(pos);

                for (int i = 1; i < matchers.length; i++) {
                    maxTypes[pos] = cmpTypes.max(maxTypes[pos], matchers[i].requiredType(pos));
                }
            }

        }

    }

    @Override
    public Class<? extends Token> requiredType(int position) {
        return maxTypes[position];
    }

    @Override
    public boolean matches(int position, Token token) {
        for (TokenMatcher matcher : matchers) {
            if (!matcher.matches(position, token)) {
                return false;
            }
        }
        return true;
    }

}
