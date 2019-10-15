package lt.lb.commons.parsing;

import java.io.Serializable;

/**
 *
 * @author laim0nas100
 */
public interface TokenProducer<T extends Token> {

    public T produce(String value, int[] pos);
    
    public static final DefaultTokenProducer DEFAULT_TOKEN_PROD = new DefaultTokenProducer();
    public static final DefaultLiteralProducer DEFAULT_LITERAL_PROD = new DefaultLiteralProducer();
    public static final DefaultLiteralStringProducer DEFAULT_LITRAL_STRING_PROD = new DefaultLiteralStringProducer();
    
   
    public static class DefaultTokenProducer implements TokenProducer<Token>, Serializable {

        @Override
        public Token produce(String value, int[] pos) {
            return new Token(value, pos);
        }

    }

    public static class DefaultLiteralProducer implements TokenProducer<Literal>, Serializable {

        @Override
        public Literal produce(String value, int[] pos) {
            return new Literal(value, pos);
        }

    }

    public static class DefaultLiteralStringProducer implements TokenProducer<LiteralString>, Serializable {

        @Override
        public LiteralString produce(String value, int[] pos) {
            return new LiteralString(value, pos);
        }

    }
}
