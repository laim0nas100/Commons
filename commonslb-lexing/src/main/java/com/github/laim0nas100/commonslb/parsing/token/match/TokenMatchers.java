package com.github.laim0nas100.commonslb.parsing.token.match;

import com.github.laim0nas100.commonslb.parsing.token.Literal;
import com.github.laim0nas100.commonslb.parsing.token.Token;
import com.github.laim0nas100.commonslb.parsing.token.match.impl.AnyTokenMatcher;
import com.github.laim0nas100.commonslb.parsing.token.match.impl.BaseTokenMatcher;
import com.github.laim0nas100.commonslb.parsing.token.match.impl.ConcatTokenMatcher;
import com.github.laim0nas100.commonslb.parsing.token.match.impl.ConjuctionTokenMatcher;
import com.github.laim0nas100.commonslb.parsing.token.match.impl.DisjunctionTokenMatcher;
import com.github.laim0nas100.commonslb.parsing.token.match.impl.ExactTokenMatcher;
import com.github.laim0nas100.commonslb.parsing.token.match.impl.LiteralTokenMatcher;
import com.github.laim0nas100.commonslb.parsing.token.match.impl.NoneTokenMatcher;
import com.github.laim0nas100.commonslb.parsing.token.match.impl.TypeTokenMatcher;

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
