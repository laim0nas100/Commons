package lt.lb.commons.parsing.token.match.impl;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import lt.lb.commons.parsing.token.Token;
import lt.lb.commons.parsing.token.match.TokenMatcher;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseTokenMatcher implements TokenMatcher {

    protected int length = 1;
    protected String name = "";

    public BaseTokenMatcher(int length, String name) {
        this.length = length;
        this.name = Objects.requireNonNull(name, "Name should not be null");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public Class<? extends Token> requiredType(int position) {
        return Token.class;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + name() + " " + length();
    }

    public static <T> T[] assertArray(T... array) {
        if(array.length == 0){
            throw new IllegalArgumentException("Empty arrays not allowed");
        }
        if (Stream.of(array).anyMatch(f -> f == null)) {
            throw new IllegalArgumentException(Arrays.asList(array) + " contains a null");
        }
        return array;
    }

}
