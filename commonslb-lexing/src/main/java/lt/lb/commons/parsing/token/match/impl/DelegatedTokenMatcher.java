package lt.lb.commons.parsing.token.match.impl;

import java.util.Objects;
import lt.lb.commons.parsing.token.Token;
import lt.lb.commons.parsing.token.match.TokenMatcher;

/**
 *
 * @author laim0nas100
 */
public class DelegatedTokenMatcher implements TokenMatcher {

    protected TokenMatcher delegate;

    public DelegatedTokenMatcher(TokenMatcher delegate) {
        this.delegate = Objects.requireNonNull(delegate, "Delegated matcher was not supplied");
    }

    public TokenMatcher getDelegate() {
        return delegate;
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public int length() {
        return delegate.length();
    }

    @Override
    public int importance() {
        return delegate.importance();
    }

    @Override
    public Class<? extends Token> requiredType(int position) {
        return delegate.requiredType(position);
    }

    @Override
    public boolean matches(int position, Token token) {
        return delegate.matches(position, token);
    }

    @Override
    public TokenMatcher named(String newName) {
        return delegate.named(newName);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
