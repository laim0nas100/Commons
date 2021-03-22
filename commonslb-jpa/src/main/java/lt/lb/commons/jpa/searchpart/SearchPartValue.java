package lt.lb.commons.jpa.searchpart;

import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 * @param <T> type of a value
 * @param <M> implementation
 */
public interface SearchPartValue<T, M extends SearchPartValue<T, M>> extends SearchPartExpresion<T, M>, Supplier<T> {

}
