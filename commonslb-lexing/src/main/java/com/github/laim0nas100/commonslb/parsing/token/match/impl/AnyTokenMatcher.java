package com.github.laim0nas100.commonslb.parsing.token.match.impl;

import com.github.laim0nas100.commonslb.parsing.token.Token;

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
