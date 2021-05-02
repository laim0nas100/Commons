package lt.lb.commons.parsing.token.match.impl;

import java.util.Objects;
import lt.lb.commons.parsing.token.Token;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class ExactTokenMatcher extends BaseTokenMatcher {

    protected boolean ignoreCase = false;
    protected String word;

    public ExactTokenMatcher(boolean ignoreCase, String name, String word) {
        super(1, name);
        this.word = Objects.requireNonNull(word,"Provided word should not be null");
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean matches(int position, Token token) {
        String tVal = token.value;
        return ignoreCase ? StringUtils.equalsIgnoreCase(word, tVal) : StringUtils.equals(word, tVal);
    }

}
