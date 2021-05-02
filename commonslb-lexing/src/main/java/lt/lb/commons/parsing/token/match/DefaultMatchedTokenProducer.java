package lt.lb.commons.parsing.token.match;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.misc.compare.ComparatorBuilder;
import lt.lb.commons.parsing.token.Token;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class DefaultMatchedTokenProducer implements MatchedTokenProducer {

    static final Comparator<Map.Entry<Integer, List<TokenMatcher>>> compEntry = new ComparatorBuilder<Map.Entry<Integer, List<TokenMatcher>>>()
            .thenComparingValue(v -> v.getKey()).reverse().build();

    static final Comparator<TokenMatcher> compLength = new ComparatorBuilder<TokenMatcher>().thenComparingValue(c -> c.length()).reverse().build();
    static final Comparator<TokenMatcher> compImportance = new ComparatorBuilder<TokenMatcher>().thenComparingValue(c -> c.importance()).reverse().build();

    protected List<TokenMatcher> matchers;
    protected Iterator<Token> lexer;
    protected Value<Token> tokenBuffer = new Value<>();
    protected SafeOpt<MatchedTokens> currentTokens = null;

    public DefaultMatchedTokenProducer(Iterator<Token> lexer, Collection<TokenMatcher> matchers) {
        Objects.requireNonNull(matchers);
        this.lexer = Objects.requireNonNull(lexer);

        this.matchers = matchers.stream()
                .filter(p -> p.length() > 0)
                .sorted(compLength.thenComparing(compImportance))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasNext() {
        if (currentTokens == null) {
            currentTokens = getNextMatchedToken();
        }
        if (currentTokens.hasError()) {
            currentTokens.throwIfErrorAsNested();
        }
        return currentTokens.isPresent();
    }

    @Override
    public SafeOpt<MatchedTokens> safeNext() {
        if (currentTokens == null) {
            hasNext();
        }
        SafeOpt<MatchedTokens> toke = currentTokens;
        currentTokens = null;
        return toke;
    }

    protected SafeOpt<MatchedTokens> getNextMatchedToken() {
        if (lexer.hasNext() || tokenBuffer.isNotNull()) {
            LinkedList<TokenMatcher> toCheck = new LinkedList<>(matchers);
            LinkedList<Token> tempTokens = new LinkedList<>();
            HashMap<Integer, List<TokenMatcher>> finalized = new HashMap<>();
            while ((lexer.hasNext() || tokenBuffer.isNotNull()) && !toCheck.isEmpty()) {
                Token next = null;
                if (tokenBuffer.isEmpty()) {
                    next = lexer.next();
                    tokenBuffer.set(next);

                } else {
                    next = tokenBuffer.get();
                }
                final Token token = next;
                tempTokens.add(token);

                int size = tempTokens.size();
                int localPos = size - 1;
                Iterator<TokenMatcher> iterator = toCheck.iterator();
                boolean foundApplicable = false;
                List<TokenMatcher> exact = new ArrayList<>();
                while (iterator.hasNext()) {
                    TokenMatcher m = iterator.next();
                    if (m.length() >= size && m.requiredType(localPos).isInstance(token) && m.matches(localPos, token)) {
                        if (m.length() == size) {
                            exact.add(m);
                        }
                        foundApplicable = true;
                    } else {
                        iterator.remove();
                    }
                }
                if (foundApplicable) {
                    tokenBuffer.set(null);
                }
                if (!exact.isEmpty()) {
                    finalized.computeIfAbsent(size, c -> new ArrayList<>()).addAll(exact);
                }

            }
            if (finalized.isEmpty()) {
                String err = tempTokens + "";
                if (tempTokens.isEmpty() && tokenBuffer.isNotNull()) {
                    err = tokenBuffer.get().toString();
                }
                return SafeOpt.error(new MatchedTokenProducerException("Failed to match any matchers, for token " + err));
            }

            Map.Entry<Integer, List<TokenMatcher>> get = finalized.entrySet().stream()
                    .sorted(compEntry).findFirst().get();
            Integer size = get.getKey();
            List<TokenMatcher> maxMatched = get.getValue();
            MatchedTokens matchedTokens = new MatchedTokens(maxMatched, tempTokens.stream().limit(size).collect(Collectors.toList()));
            return SafeOpt.of(matchedTokens);
        }
        return SafeOpt.empty();
    }

    public static List<MatchedTokens> match(Iterator<Token> lexer, Collection<TokenMatcher> matchersCol) throws MatchedTokenProducerException {

        List<TokenMatcher> matchers = matchersCol.stream()
                .filter(p -> p.length() > 0)
                .sorted(compLength).collect(Collectors.toList());

        ArrayList<MatchedTokens> matched = new ArrayList<>();
        Value<Token> tokenBuffer = new Value<>();

        while (lexer.hasNext() || tokenBuffer.isNotNull()) {

            LinkedList<TokenMatcher> toCheck = new LinkedList<>(matchers);
            LinkedList<Token> tempTokens = new LinkedList<>();
            HashMap<Integer, List<TokenMatcher>> finalized = new HashMap<>();
            while ((lexer.hasNext() || tokenBuffer.isNotNull()) && !toCheck.isEmpty()) {
                Token next = null;
                if (tokenBuffer.isEmpty()) {
                    next = lexer.next();
                    tokenBuffer.set(next);

                } else {
                    next = tokenBuffer.get();
                }
                final Token token = next;
                tempTokens.add(token);

                int size = tempTokens.size();
                int localPos = size - 1;
                Iterator<TokenMatcher> iterator = toCheck.iterator();
                boolean foundApplicable = false;
                List<TokenMatcher> exact = new LinkedList<>();
                while (iterator.hasNext()) {
                    TokenMatcher m = iterator.next();
                    if (m.length() >= size && m.requiredType(localPos).isInstance(token) && m.matches(localPos, token)) {
                        if (m.length() == size) {
                            exact.add(m);
                        }
                        foundApplicable = true;
                    } else {
                        iterator.remove();
                    }
                }
                if (foundApplicable) {
                    tokenBuffer.set(null);
                }
                if (!exact.isEmpty()) {
                    finalized.computeIfAbsent(size, c -> new ArrayList<>()).addAll(exact);
                }

            }
            if (finalized.isEmpty()) {
                String err = tempTokens + "";
                if (tempTokens.isEmpty() && tokenBuffer.isNotNull()) {
                    err = tokenBuffer.get().toString();
                }
                throw new MatchedTokenProducerException("Failed to match any matchers, for token " + err);
            }

            Map.Entry<Integer, List<TokenMatcher>> get = finalized.entrySet().stream()
                    .sorted(compEntry).findFirst().get();
            Integer size = get.getKey();
            List<TokenMatcher> maxMatched = get.getValue();

            matched.add(new MatchedTokens(maxMatched, tempTokens.stream().limit(size).collect(Collectors.toList())));

        }

        return matched;

    }

    @Override
    public MatchedTokenProducer withNewLexer(Iterator<Token> lexer) {
        return new DefaultMatchedTokenProducer(lexer, matchers);
    }

}
