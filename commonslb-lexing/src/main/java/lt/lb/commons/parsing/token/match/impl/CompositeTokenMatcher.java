package lt.lb.commons.parsing.token.match.impl;

import java.util.stream.Stream;
import lt.lb.commons.parsing.token.match.TokenMatcher;

/**
 *
 * @author laim0nas100
 */
public abstract class CompositeTokenMatcher extends BaseTokenMatcher {

    protected TokenMatcher[] matchers;

    public CompositeTokenMatcher(int length, String name, TokenMatcher... matchers) {
        super(length, name);
        this.matchers = assertArray(matchers);
    }

    public static int sumLength(TokenMatcher... matchers) {
        return Stream.of(matchers).mapToInt(m -> m.length()).sum();
    }

    public static int assertSameLength(TokenMatcher... matchers) {
        final int expectedLength = matchers[0].length();

        for (int i = 1; i < matchers.length; i++) {
            int length = matchers[i].length();
            if (expectedLength != length) {
                throw new IllegalArgumentException("Length mismatch. Expected " + expectedLength + " but found:" + length + " at " + matchers[i].name());
            }
        }
        return expectedLength;
    }

}
