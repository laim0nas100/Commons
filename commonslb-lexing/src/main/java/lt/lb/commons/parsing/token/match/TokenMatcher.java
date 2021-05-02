package lt.lb.commons.parsing.token.match;

import java.util.function.Predicate;
import lt.lb.commons.parsing.token.Token;
import lt.lb.commons.parsing.token.match.impl.ImportanceTokenMatcher;
import lt.lb.commons.parsing.token.match.impl.NamedTokenMatcher;

/**
 *
 * @author laim0nas100
 */
public interface TokenMatcher extends Predicate<Token> {

    /**
     * How to recognize what has matched
     *
     * @return
     */
    public String name();

    /**
     * How many tokens are required. 0 or below means it is never used.
     *
     * @return
     */
    public default int length() {
        return 1;
    }

    /**
     * Higher importance means it is tried applied sooner
     *
     * @return
     */
    public default int importance() {
        return 0;
    }

    /**
     * Supply required most narrow subtype for optimized matching
     *
     * @param position should be within length
     * @return
     */
    public default Class<? extends Token> requiredType(int position) {
        return Token.class;
    }

    @Override
    public default boolean test(Token t) {
        for (int i = 0; i < this.length(); i++) {
            if (matches(i, t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If given token can be at given position
     *
     * @param position should be within length (negative)
     * @param token
     * @return
     */
    public boolean matches(int position, Token token);

    public default TokenMatcher named(String newName) {
        return new NamedTokenMatcher(newName, this);
    }

    public default TokenMatcher importance(int newImportance) {
        return new ImportanceTokenMatcher(newImportance, this);
    }
}
