package com.github.laim0nas100.commonslb.jpa.searchpart;

/**
 *
 * @author laim0nas100
 */
public class SearchParts {

    public static <T> SimpleSearchPart<T> ofSimple(T value) {
        return new SimpleSearchPart<T>(value);

    }

}
