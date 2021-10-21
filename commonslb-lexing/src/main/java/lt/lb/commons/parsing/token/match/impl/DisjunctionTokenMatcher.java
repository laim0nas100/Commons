package lt.lb.commons.parsing.token.match.impl;

import lt.lb.commons.Ins;
import lt.lb.commons.parsing.token.Token;
import lt.lb.commons.parsing.token.match.TokenMatcher;
import lt.lb.readablecompare.Compare;
import lt.lb.readablecompare.CompareOperator;
import lt.lb.readablecompare.SimpleCompare;

/**
 *
 * @author laim0nas100
 */
public class DisjunctionTokenMatcher extends CompositeTokenMatcher {

    protected Class<? extends Token>[] minTypes;

    public DisjunctionTokenMatcher(String name, TokenMatcher... matchers) {
        super(assertSameLength(matchers), name, matchers);
        Ins.InsCl<Token> insToken = Ins.of(Token.class);
        minTypes = new Class[length];
        SimpleCompare<Class> cmpTypes = Compare.of(Ins.TYPE_COMPARATOR); // broader types comes first (smaller)
        if (length > 0) {
            for (int pos = 0; pos < length; pos++) {
                Class<? extends Token> requiredType = matchers[0].requiredType(pos);
                if (!insToken.superClassOf(requiredType)) {
                    throw new IllegalArgumentException(matchers[0].name() + " returns type that is not a subtype of " + Token.class);
                }
                minTypes[pos] = matchers[0].requiredType(pos);

                for (int i = 1; i < matchers.length; i++) {
                    Class maybeBroader = matchers[i].requiredType(pos);
                    if (!insToken.superClassOf(maybeBroader)) {
                        throw new IllegalArgumentException(matchers[i].name() + " returns type that is not a subtype of " + Token.class);
                    }

                    while (!Token.class.equals(maybeBroader)) {
                        boolean greater = cmpTypes.compare(minTypes[pos], CompareOperator.GREATER, maybeBroader);
                        if (greater) {
                            break;
                        } else {
                            maybeBroader = maybeBroader.getSuperclass();
                        }
                    }
                    minTypes[pos] = maybeBroader;
                }
            }

        }

    }

    @Override
    public Class<? extends Token> requiredType(int position) {
        return minTypes[position];
    }

    @Override
    public boolean matches(int position, Token token) {
        for (TokenMatcher matcher : matchers) {
            if (matcher.matches(position, token)) {
                return true;
            }
        }
        return false;
    }

}
