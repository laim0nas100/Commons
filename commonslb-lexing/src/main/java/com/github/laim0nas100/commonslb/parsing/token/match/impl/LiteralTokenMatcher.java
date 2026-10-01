package com.github.laim0nas100.commonslb.parsing.token.match.impl;

import com.github.laim0nas100.commonslb.parsing.token.Literal;
import com.github.laim0nas100.commonslb.parsing.token.Token;

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
