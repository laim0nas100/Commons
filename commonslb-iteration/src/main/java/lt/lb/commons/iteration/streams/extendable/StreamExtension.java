package lt.lb.commons.iteration.streams.extendable;

import java.util.List;
import java.util.Set;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamExtension<X, M extends DecoratableStream<X, M>> {

    public M me();

    public static interface StreamExtensionsAll<X, M extends DecoratableStream<X, M>> extends DecoratableStream<X, M>, StreamAppenders<X, M>, StreamCollectors<X, M>, StreamConsumers<X, M>, StreamFilters<X, M> {

        //JDK 17 collision
        @Override
        public default List<X> toList() {
            return StreamCollectors.super.toList();
        }

        @Override
        public default Set<X> toSet(){
            return StreamCollectors.super.toSet();
        }
        
        
    }
}
