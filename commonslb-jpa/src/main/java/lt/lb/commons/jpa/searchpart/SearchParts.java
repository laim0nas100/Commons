package lt.lb.commons.jpa.searchpart;

/**
 *
 * @author laim0nas100
 */
public class SearchParts {

    public static <T> SimpleSearchPart<T> ofSimple(T value) {
        return new SimpleSearchPart<T>(value);

    }

}
