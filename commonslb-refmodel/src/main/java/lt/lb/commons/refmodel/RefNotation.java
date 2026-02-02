package lt.lb.commons.refmodel;

import java.util.List;
import java.util.Optional;
import lt.lb.commons.parsing.StringParser;
import lt.lb.commons.parsing.numbers.FastParse;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class RefNotation {

    public static final RefNotation DEFAULT = new RefNotation(".", "[", "]", "%d");

    public final String separator;
    public final String arrayStart;
    public final String arrayEnd;
    public final String indexTemplate;

    public RefNotation(String separator, String arrayStart, String arrayEnd, String indexTemplate) {
        this.separator = assertNotEmpty(separator);
        this.arrayStart = assertNotEmpty(arrayStart);
        this.arrayEnd = assertNotEmpty(arrayEnd);
        this.indexTemplate = assertNotEmpty(indexTemplate);
    }

    public static String assertNotEmpty(String s) {
        if (StringUtils.isEmpty(s)) {
            throw new IllegalArgumentException("Empty string is not allowed");
        }
        return s;
    }

    /**
     * Attempts to extract an integer index from a string step that follows the
     * pattern {@code ...[N]}.
     * <p>
     * Returns:
     * <ul>
     * <li>{@code Optional.empty()} if no valid index is found (e.g.
     * {@code items}, {@code items[%d]})</li>
     * <li>{@code Optional.of(N)} if a valid integer is found inside brackets
     * (e.g. {@code items[2] → 2}, {@code items[-2] → -2})</li>
     * </ul>
     * <p>
     * The method assumes the first occurrence of {@code [...]} contains the
     * index. Leading/trailing whitespace inside brackets is not trimmed.
     *
     * @param step the step string (e.g. {@code items[42]}, {@code members[-1]})
     * @return an {@code Optional} containing the parsed integer index, or empty
     * if not found or invalid
     */
    public Optional<Integer> getIndexFromStep(String step) {
        int start = step.indexOf(arrayStart);
        if (start < 0) {
            return Optional.empty();
        }

        int end = step.indexOf(arrayEnd, start + arrayStart.length());
        if (end <= start) {
            return Optional.empty();
        }

        return Optional.ofNullable(FastParse.parseInt(step.substring(start + arrayStart.length(), end)));
    }

    /**
     * Removes the substring after first [ works with templates and specific
     * indices:
     * <br> items[%d] -> items
     * <br> items[2] -> items
     *
     * @param step
     * @return the member without array notation
     */
    public String getMemberWithoutIndex(String step) {
        int index_0 = step.indexOf(arrayStart);
        if (index_0 >= 0) {
            return step.substring(0, index_0);
        }
        return step;
    }

    /**
     * Produces a relation. If parent is empty, just returns child. For example
     * {@code parent.child}
     *
     * @param parent
     * @param child
     * @return
     */
    public String produceRelation(String parent, String child) {
        if (StringUtils.isBlank(parent)) {
            return child;
        }
        return parent + separator + child;
    }

    /**
     * Produces an array access relation. If parent is empty, just returns
     * child. For example {@code parent[%d]}
     *
     * @param parent
     * @param child
     * @return
     */
    public String produceArrayAccess(String parent, String child) {
        if (StringUtils.isBlank(parent)) {
            return child;
        }
        return parent + child;
    }

    /**
     * Produces complete concrete index array access. For example
     * {@code root.items[1]}
     *
     * @param listPath {@code root.items} object path
     * @param index concrete index
     * @return
     */
    public String produceArrayAccess(String listPath, int index) {
        return listPath + getArrayIndexReplaced(getArrayIndexTemplate(), index);
    }

    /**
     * Array index template. For example {@code [%d]}
     *
     * @return array index template
     */
    public String getArrayIndexTemplate() {
        return arrayStart + indexTemplate + arrayEnd;
    }

    /**
     * Array index substitution. For example {@code list[%d] -> list[0]}
     *
     * @param template
     * @param index
     * @return
     */
    public String getArrayIndexReplaced(String template, int index) {
        return String.format(template, index);
    }

    /**
     * Step array to get to the value. For example
     * {@code root.list[0].name -> [root, list[0], name]}
     *
     * @param path
     * @return
     */
    public List<String> steps(String path) {
        return StringParser.split(path, separator);
    }
}
