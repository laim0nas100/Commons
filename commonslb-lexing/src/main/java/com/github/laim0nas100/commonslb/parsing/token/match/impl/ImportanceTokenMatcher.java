package com.github.laim0nas100.commonslb.parsing.token.match.impl;

import com.github.laim0nas100.commonslb.parsing.token.match.TokenMatcher;

/**
 *
 * @author laim0nas100
 */
public class ImportanceTokenMatcher extends DelegatedTokenMatcher {

    protected int importance;

    public ImportanceTokenMatcher(int importance, TokenMatcher delegate) {
        super(delegate);
        this.importance = importance;
    }

    @Override
    public int importance() {
        return importance;
    }

}
