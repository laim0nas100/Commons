package lt.lb.commons.iteration.streams.extendable;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamExtension<X, M extends DelegatingStream<X, M>> {

    public M me();

    public static interface StreamExtensionsAll<X, M extends DelegatingStream<X, M>> extends DelegatingStream<X, M>, StreamAppenders<X, M>, StreamCollectors<X, M>, StreamConsumers<X, M>, StreamFilters<X, M> {

    }
}
