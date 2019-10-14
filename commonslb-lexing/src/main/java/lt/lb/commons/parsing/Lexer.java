package lt.lb.commons.parsing;

import lt.lb.commons.containers.collections.SelfSortingMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import lt.lb.commons.F;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.interfaces.Equator;

/**
 *
 * @author laim0nas100
 */
public class Lexer {

    /**
     * Set wether to skip white space or include it inside literals.
     *
     * @param skipWhitespace
     */
    public void setSkipWhitespace(boolean skipWhitespace) {
        this.skipWhitespace = skipWhitespace;
    }

    public class LexerException extends Exception {
    }

    public class NoSuchLexemeException extends LexerException {
    }

    public class StringNotTerminatedException extends LexerException {
    }
    private final Comparator<String> cmp = (Comparator<String>) (String s1, String s2) -> {
        int len = s2.length() - s1.length();
        if (len == 0) {
            len = 1;
        }
        return len;
    };

    protected String keyStringBegin,
            keyStringEnd,
            keyStringEscape;

    protected String commentLine, commentStart, commentEnd;
    protected SelfSortingMap<String, TokenProducer> keywords;
    protected SelfSortingMap<String, TokenProducer> keywordsBreaking;
    protected TokenProducer literalProducer = TokenProducer.DEFAULT_LITERAL_PROD;
    protected TokenProducer literalStringProducer = TokenProducer.DEFAULT_LITRAL_STRING_PROD;
    protected int linePos, charPos;
    protected String[] lines;
    protected boolean skipWhitespace;
    /**
     * Override for case-insensitive keywords
     */
    public Equator<String> equator = StringOp::equals;
    /**
     * Override for case-insensitive keywords
     */
    public Equator<String> equatorBreaking = StringOp::equals;

    public Lexer() {
        this.keywords = new SelfSortingMap<>(cmp, new HashMap<>());
        this.keywordsBreaking = new SelfSortingMap<>(cmp, new HashMap<>());
    }

    /**
     * Keywords can be a part of literals.If for example: 'printing' has keyword
     * 'int' inside it, but it will not be recognized as such. With default
     * token producer.
     *
     * @param keywords
     */
    public void addKeyword(String... keywords) {
        for (String tok : keywords) {
            this.keywords.put(tok, TokenProducer.DEFAULT_TOKEN_PROD);
        }
    }

    /**
     * Keywords can be a part of literals.If for example: 'printing' has keyword
     * 'int' inside it, but it will not be recognized as such. With custom token
     * producer.
     *
     * @param producer
     * @param keywords
     */
    public void addKeyword(TokenProducer producer, String... keywords) {
        for (String tok : keywords) {
            this.keywords.put(tok, producer);
        }
    }

    /**
     * Keywords that can't be a part of literals (unless inside in a string).
     * Such keywords will break apart any literals, that has a keyword inside
     * it. With default token producer.
     *
     * @param keywords
     */
    public void addKeywordBreaking(String... keywords) {
        addKeywordBreaking(TokenProducer.DEFAULT_TOKEN_PROD, keywords);
    }

    /**
     * Keywords that can't be a part of literals (unless inside in a
     * string).Such keywords will break apart any literals, that has a keyword
     * inside it. With custom token producer.
     *
     * @param producer
     * @param keywords
     */
    public void addKeywordBreaking(TokenProducer producer, String... keywords) {
        for (String tok : keywords) {
            this.keywordsBreaking.put(tok, producer);
        }
    }

    protected SafeOpt<Character> getCurrentChar() {
        Integer[] pos = new Integer[]{this.linePos, this.charPos};
        return this.getByPos(pos);
    }

    protected Integer currentLineLen() {
        if (this.lines.length > linePos) {
            return this.lines[linePos].length();
        } else {
            return null;
        }
    }

    protected SafeOpt<Character> getByPos(Integer[] pos) {
        return SafeOpt.ofGet(() -> lines[pos[0]].charAt(pos[1]));
    }

    protected Integer[] rangeCheck(Integer shift) {
        Integer[] pos = new Integer[2];
        pos[0] = this.linePos;
        pos[1] = this.charPos + shift;
        if (this.currentLineLen() == null) {
            return pos;
        }
        while (pos[1] >= this.currentLineLen()) {
            pos[1] -= this.currentLineLen();
            pos[0] += 1;
            if (pos[0] >= this.lines.length) {
                pos[0] = this.lines.length;
                break;
            }
        }
        return pos;
    }

    protected SafeOpt<Character> peek(Integer peek) {
        Integer[] rangeCheck = this.rangeCheck(peek);
        if (rangeCheck[0] != this.lines.length) {
            return this.getByPos(rangeCheck);
        }
        return SafeOpt.empty();
    }

    protected SafeOpt<Character> advance(Integer am) {
        Integer[] pos;
        pos = this.rangeCheck(am);
        this.linePos = pos[0];
        this.charPos = pos[1];
        return this.getByPos(pos);
    }

    /**
     * Check if String parsing (begin,end,escape) is defined
     *
     * @return
     */
    public boolean hasStrings() {
        return StringOp.isNoneBlank(keyStringBegin, keyStringEnd, keyStringEscape);
    }

    public boolean hasLineComment() {
        return StringOp.isNoneBlank(commentLine);
    }

    public boolean hasMultilineComment() {
        return StringOp.isNoneBlank(commentStart, commentEnd);
    }

    /**
     * Prepare for string parsing. Does not support nesting.
     *
     * @param strBeg keyword to begin string
     * @param strEnd keyword to end string
     * @param strEsc keyword to escape within string
     */
    public void prepareForStrings(String strBeg, String strEnd, String strEsc) {
        this.keywords.put(strBeg, TokenProducer.DEFAULT_TOKEN_PROD);
        this.keywords.put(strEnd, TokenProducer.DEFAULT_TOKEN_PROD);
        this.keywords.put(strEsc, TokenProducer.DEFAULT_TOKEN_PROD);
        this.keyStringBegin = strBeg;
        this.keyStringEnd = strEnd;
        this.keyStringEscape = strEsc;

    }

    public void prepareForComments(String commLine, String commStart, String commEnd) {
        commentLine = commLine;
        commentStart = commStart;
        commentEnd = commEnd;
    }

    /**
     * Sets char and line position to 0
     */
    public void reset() {
        this.charPos = 0;
        this.linePos = 0;
    }

    /**
     * Prepares to lex lines all over
     *
     * @param allLines
     */
    public final void resetLines(Collection<String> allLines) {
        reset();
        lines = new String[allLines.size()];
        F.iterate(allLines, (i, s) -> {
            if (s.endsWith("\n")) {
                lines[i] = s;
            } else {
                lines[i] = s + "\n";
            }

        });
    }

    protected void skipWhitespace() {
        while (true) {
            SafeOpt<Character> cOpt = this.getCurrentChar();
            if (cOpt.isPresent()) {
                Character c = cOpt.get();
                if (!Character.isWhitespace(c)) {
                    return;
                }
                this.advance(1);
            } else {
                return;
            }
        }

    }

    protected Token string() throws StringNotTerminatedException, NoSuchLexemeException {
        String result = "";
        while (true) {
            SafeOpt<Character> currentChar = this.getCurrentChar();
            if (!currentChar.isPresent()) {
                throw new StringNotTerminatedException();
            } else if (this.tryToMatch(this.keyStringEscape,equatorBreaking)) {
                this.advanceByTokenKey(this.keyStringEscape);
                SafeOpt<Character> currentChar1 = this.getCurrentChar();
                if (currentChar.isPresent()) {
                    result += currentChar1.get();
                    this.advance(1);
                } else {
                    throw new StringNotTerminatedException();
                }

            }
            if (this.tryToMatch(this.keyStringEnd,equatorBreaking)) {
                this.advanceByTokenKey(this.keyStringEnd);
                break;
            }
            currentChar = this.getCurrentChar();
            if (!currentChar.isPresent()) {
                throw new StringNotTerminatedException();
            }
            result += currentChar.get();
            this.advance(1);

        }
        return literalStringProducer.produce(result, getCurrentPos());

    }

    public Integer[] getCurrentPos() {
        return new Integer[]{this.linePos, this.charPos};
    }

    protected SafeOpt<Token> breakingKeyword() {
        for (String token : this.keywordsBreaking.getOrderedList()) {
            if (this.tryToMatch(token,equatorBreaking)) {
                return SafeOpt.of(keywordsBreaking.get(token)).map(m -> m.produce(token, getCurrentPos()));
            }
        }
        return SafeOpt.empty();
    }

    protected boolean tryToMatch(String explicit, Equator<String> eq) {
        int lenToPeek = explicit.length();
        String readSymbols = "";
        for (int i = 0; i < lenToPeek; i++) {
            SafeOpt<Character> ch = this.peek(i);
            if (ch.isPresent()) {
                readSymbols += ch.get();
            } else {
                break;
            }
        }
        return eq.equals(explicit, readSymbols);
    }

    protected void advanceByTokenKey(String key) {
        this.advance(key.length());
    }

    protected Token literal(String value, Integer[] pos) {
        for (String token : this.keywords.getOrderedList()) {
            if (equator.equals(value, token)) {
                return keywords.get(token).produce(token, pos);
            }
        }
        return this.literalProducer.produce(value, pos);
    }

    protected SafeOpt<Token> getNextTokenImpl() {
        return SafeOpt.empty();
    }

    public Optional<Token> getNextToken() throws NoSuchLexemeException, StringNotTerminatedException {
        StringBuilder buffer = new StringBuilder();
        SafeOpt<Token> token = getNextTokenImpl();
        if (token.isPresent()) {
            return token.asOptional();
        }
        Integer[] pos = new Integer[]{this.linePos, this.charPos};

        while (true) {
            SafeOpt<Character> currentChar = this.getCurrentChar();

            if (!currentChar.isPresent()) {
                if (buffer.length() > 0) {
                    return Optional.of(this.literal(buffer.toString(), pos));
                }
                break;
            }
            if (this.skipWhitespace) {
                if (Character.isWhitespace(currentChar.get())) {
                    this.advance(1);
                    if (buffer.length() > 0) {
                        return Optional.of(this.literal(buffer.toString(), pos));
                    }
                    continue;
                }
            }
            if (this.hasStrings()) {
                if (this.tryToMatch(this.keyStringBegin,equatorBreaking)) {
                    this.advanceByTokenKey(this.keyStringBegin);
                    return Optional.of(this.string());
                }
            }
            if (this.hasLineComment()) {
                if (this.tryToMatch(this.commentLine,equatorBreaking)) {
                    if (buffer.length() > 0) {
                        return Optional.of(this.literal(buffer.toString(), pos));
                    } else {
                        this.linePos++; // just skip line
                        this.charPos = 0;
                    }
                    continue;
                }
            }

            if (this.hasMultilineComment()) {
                if (this.tryToMatch(this.commentStart,equatorBreaking)) {
                    if (buffer.length() > 0) {
                        return Optional.of(this.literal(buffer.toString(), pos));
                    } else {
                        this.advanceByTokenKey(this.commentStart);
                        while (!this.tryToMatch(commentEnd,equatorBreaking)) {
                            this.advance(1);
                        }
                        this.advanceByTokenKey(this.commentEnd);
                    }
                    continue;
                }
            }

            if (buffer.length() > 0) {
                token = this.breakingKeyword();
                if (token.isPresent()) { // break up current iteral
                    return Optional.of(this.literal(buffer.toString(), pos));
                } else {
                    buffer.append(currentChar.get());
                    this.advance(1);
                }
            } else {
                token = this.breakingKeyword();
                if (token.isPresent()) {
                    Token t = token.get();
                    this.advanceByTokenKey(t.value);
                    return Optional.of(t);

                } else {
                    buffer.append(currentChar.get());
                    this.advance(1);

                }
            }

        }
        return token.asOptional();
    }

    public ArrayList<Token> getRemainingTokens() throws NoSuchLexemeException, StringNotTerminatedException {
        ArrayList<Token> remains = new ArrayList<>();
        while (true) {
            Optional<Token> nextToken = this.getNextToken();
            if (nextToken.isPresent()) {
                remains.add(nextToken.get());
            } else {
                break;
            }

        }
        return remains;
    }
}
