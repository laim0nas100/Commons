package lt.lb.commons.jpa.searchpart;

import java.util.stream.Stream;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class StringSearchPart extends SimpleSearchPart<String> {

    public enum StringSearchPartEnum {
        START,
        END,
        ANY,
        EXACT;
    }

    protected boolean caseSensitive = false;
    protected StringSearchPartEnum searchType = StringSearchPartEnum.EXACT;

    public StringSearchPart() {
    }

    public StringSearchPart(String val) {
        super(val);
    }

    protected StringSearchPart(StringSearchPart copy) {
        super(copy);
        this.searchType = copy.searchType;
    }

    @Override
    public StringSearchPart clone() {
        return new StringSearchPart(this);
    }

    public StringSearchPartEnum getSearchType() {
        return searchType;
    }

    public void setSearchType(StringSearchPartEnum searchType) {
        this.searchType = searchType;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public static String escapeLike(String query) {
        String[] text = new String[]{"\\", "%", "_"};
        String[] replace = new String[]{"\\\\", "\\%", "\\_"};
        return StringUtils.replaceEach(query, text, replace);
//        return query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    @Override
    public Predicate buildPredicateImpl(CriteriaBuilder builder, Expression<String> search) {

        final String val = caseSensitive ? getValue() : StringUtils.upperCase(getValue());
        final Expression<String> expression = caseSensitive ? search : builder.upper(search);

        switch (searchType) {

            case EXACT:
                return builder.equal(expression, val);

            case START:
                return builder.like(expression, escapeLike(val) + "%", '\\');

            case END:
                return builder.like(expression, "%" + escapeLike(val), '\\');

            case ANY:
                return builder.like(expression, "%" + escapeLike(val) + "%", '\\');

            default:
                throw new IllegalArgumentException("Failed to match search type:" + searchType);

        }
    }

}
