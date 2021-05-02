package lt.lb.commons.parsing.token.match;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lt.lb.commons.containers.collections.CollectionOp;
import lt.lb.commons.parsing.token.Token;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class MatchedTokens {

    public final List<TokenMatcher> matchedBy;
    public final List<Token> tokens;

    public MatchedTokens(List<TokenMatcher> matched, List<Token> tokens) {

        if (CollectionOp.isEmpty(matched)) {
            throw new IllegalArgumentException("Empty matched");
        }
        if (CollectionOp.isEmpty(tokens)) {
            throw new IllegalArgumentException("Empty tokens");
        }
        this.matchedBy = matched;
        this.tokens = tokens;
    }

    public TokenMatcher firstMatch() {
        return matchedBy.get(0);
    }

    public Token getToken(int index) {
        return tokens.get(index);
    }

    public List<Token> getTokens(int... index) {
        List<Token> list = new ArrayList<>(index.length);
        for (int i = 0; i < index.length; i++) {
            list.add(tokens.get(index[i]));
        }
        return list;
    }

    public boolean containsName(String name) {
        return matchedBy.stream().map(m -> m.name()).anyMatch(n -> StringUtils.equals(n, name));
    }

    public boolean contains(TokenMatcher matcher) {
        return matchedBy.contains(matcher);
    }

    @Override
    public String toString() {
        return "matchedBy=" + names() + ", tokens=" + values();
    }

    public String names() {
        return "" + matchedBy.stream().map(m -> m.name()).collect(Collectors.toList());
    }

    public String values() {
        return "" + tokens.stream().map(m -> m.value).collect(Collectors.toList());
    }

    public int count() {
        return tokens.size();
    }
}
