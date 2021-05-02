package lt.lb.commons.parsing.token.match;

import lt.lb.commons.parsing.token.Literal;
import lt.lb.commons.parsing.token.Token;
import lt.lb.commons.parsing.token.match.impl.AnyTokenMatcher;
import lt.lb.commons.parsing.token.match.impl.BaseTokenMatcher;
import lt.lb.commons.parsing.token.match.impl.ConcatTokenMatcher;
import lt.lb.commons.parsing.token.match.impl.ConjuctionTokenMatcher;
import lt.lb.commons.parsing.token.match.impl.DisjunctionTokenMatcher;
import lt.lb.commons.parsing.token.match.impl.ExactTokenMatcher;
import lt.lb.commons.parsing.token.match.impl.LiteralTokenMatcher;
import lt.lb.commons.parsing.token.match.impl.NoneTokenMatcher;
import lt.lb.commons.parsing.token.match.impl.TypeTokenMatcher;

/**
 *
 * @author laim0nas100
 */
public abstract class TokenMatchers {

    public static TokenMatcher none() {
        return new NoneTokenMatcher();
    }

    public static TokenMatcher any(int length) {
        return new AnyTokenMatcher(length, "Any " + length);
    }

    public static TokenMatcher any() {
        return new AnyTokenMatcher(0, "Any 0");
    }

    public static TokenMatcher exact(String word) {
        return new ExactTokenMatcher(false, "is " + word, word);
    }

    public static TokenMatcher exactIgnoreCase(String word) {
        return new ExactTokenMatcher(true, "ignoreCase is" + word, word);
    }

    public static TokenMatcher ofType(Class<? extends Token> type) {
        return new TypeTokenMatcher("Type:" + type.getSimpleName(), type);
    }

    public static TokenMatcher literalType() {
        return new TypeTokenMatcher("Literal", Literal.class);
    }

    public static TokenMatcher literal(String word) {
        return new LiteralTokenMatcher(false, "Literal of " + word, word);
    }

    public static TokenMatcher literalIgnoreCase(String word) {
        return new LiteralTokenMatcher(true, "Literal IC of " + word, word);
    }

    public static TokenMatcher or(TokenMatcher... matchers) {
        return new DisjunctionTokenMatcher("Or", matchers);
    }

    public static TokenMatcher and(TokenMatcher... matchers) {
        return new ConjuctionTokenMatcher("And", matchers);
    }

    public static TokenMatcher concat(TokenMatcher... matchers) {
        BaseTokenMatcher.assertArray(matchers);
        StringBuilder name = new StringBuilder(matchers[0].name());
        for (int i = 1; i < matchers.length; i++) {
            name.append("+").append(matchers[i].name());
        }
        return new ConcatTokenMatcher(name.toString(), matchers);
    }

}
