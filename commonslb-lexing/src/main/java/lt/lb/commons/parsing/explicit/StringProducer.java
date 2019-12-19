package lt.lb.commons.parsing.explicit;

import java.util.function.Function;

/**
 * Lazy string concatenation based on optional context
 * @author laim0nas100
 */
public interface StringProducer<T> extends Function<T, String> {

    /**
     * StringProducer of constant
     * @param <T>
     * @param s
     * @return 
     */
    public static <T> StringProducer<T> ofConstant(String s) {
        return ctx -> s;
    }
    
    /**
     * StringProducer of functor. For one-liner decoration.
     * @param <T>
     * @param f
     * @return 
     */
    public static <T> StringProducer<T> ofFormat(Function<T,String> f) {
        return ctx -> f.apply(ctx);
    }

    /**
     * Cumulative StringProducer of all provided producers
     * @param <T>
     * @param prods
     * @return 
     */
    public static <T> StringProducer<T> ofAll(StringProducer<T>... prods) {
        return ctx -> {
            StringBuilder sb = new StringBuilder();
            for (StringProducer<T> prod : prods) {
                sb.append(prod.apply(ctx));
            }
            return sb.toString();
        };
    }

    /**
     * Cumulative StringProducer of all provided producers
     * @param <T>
     * @param prods
     * @return 
     */
    public static <T> StringProducer<T> ofAll(Iterable<StringProducer<T>> prods) {
        return ctx -> {
            StringBuilder sb = new StringBuilder();
            for (StringProducer<T> prod : prods) {
                sb.append(prod.apply(ctx));
            }
            return sb.toString();
        };
    }

    /**
     * StringProducer enclosed with constant strings
     * @param before
     * @param after
     * @return 
     */
    public default StringProducer<T> enclose(String before, String after) {
        StringProducer<T> me = this;
        return ctx -> before + me.apply(ctx) + after;
    }

    /**
     * StringProducer with additional constant value afterwards
     * @param after
     * @return 
     */
    public default StringProducer<T> addAfter(String after) {
        StringProducer<T> me = this;
        return ctx -> me.apply(ctx) + after;
    }

    /**
     * StringProducer with additional constant value beforehand
     * @param before
     * @return 
     */
    public default StringProducer<T> addBefore(String before) {
        StringProducer<T> me = this;
        return ctx -> before + me.apply(ctx);
    }

}
