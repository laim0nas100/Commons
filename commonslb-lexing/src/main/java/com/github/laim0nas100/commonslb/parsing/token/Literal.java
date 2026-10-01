package com.github.laim0nas100.commonslb.parsing.token;

/**
 *
 * @author laim0nas100
 */
public class Literal extends Token {

    public Literal(String value, TokenPos pos) {
        super(value, pos);
    }

    @Override
    public String toString() {
        return "Literal " + super.toString();
    }

}
