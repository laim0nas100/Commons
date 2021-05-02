package lt.lb.commons.parsing.token.match.impl;

import java.util.Objects;
import lt.lb.commons.parsing.token.Token;

/**
 *
 * @author laim0nas100
 */
public class TypeTokenMatcher extends BaseTokenMatcher {

    protected Class<? extends Token> type;

    public TypeTokenMatcher(String name, Class<? extends Token> type) {
        super(1, name);
        this.type = Objects.requireNonNull(type, "Type should not be null");
    }

    @Override
    public Class<? extends Token> requiredType(int position) {
        return type;
    }

    @Override
    public boolean matches(int position, Token token) {
        return type.isInstance(token);

    }

}
