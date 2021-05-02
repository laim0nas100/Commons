package lt.lb.commons.parsing.token.match;

import java.util.Iterator;
import lt.lb.commons.iteration.UncheckedIterator;
import lt.lb.commons.parsing.token.Token;

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
