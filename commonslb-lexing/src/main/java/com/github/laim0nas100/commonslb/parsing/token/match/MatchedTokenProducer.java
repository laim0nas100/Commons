package com.github.laim0nas100.commonslb.parsing.token.match;

import java.util.Iterator;
import com.github.laim0nas100.commonslb.iteration.UncheckedIterator;
import com.github.laim0nas100.commonslb.parsing.token.Token;

/**
 *
 * @author laim0nas100
 */
public interface MatchedTokenProducer extends UncheckedIterator<MatchedTokens>{
    
    public static class MatchedTokenProducerException extends Exception {

        public MatchedTokenProducerException(String message) {
            super(message);
        }

    }
    
    public MatchedTokenProducer withNewLexer(Iterator<Token> lexer);
    
}
