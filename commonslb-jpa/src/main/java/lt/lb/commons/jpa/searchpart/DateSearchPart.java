package lt.lb.commons.jpa.searchpart;

import java.util.Calendar;
import java.util.Date;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 */
public class DateSearchPart extends ComparableSearchPart<Date> {

    protected boolean ignoreTime = true;

    public DateSearchPart() {
    }

    public DateSearchPart(Date val) {
        super(val);
    }

    public DateSearchPart(DateSearchPart copy) {
        super(copy);
        this.ignoreTime = copy.ignoreTime;
    }

    public boolean isIgnoreTime() {
        return ignoreTime;
    }

    public void setIgnoreTime(boolean ignoreTime) {
        this.ignoreTime = ignoreTime;
    }

    @Override
    public DateSearchPart clone() {
        return new DateSearchPart(this);
    }

    @Override
    public Predicate buildPredicateImpl(CriteriaBuilder builder, Expression<Date> search) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(get());
        switch (searchType) {
            case EXACT: {
                break;
            }
            case BEFORE: {
                if (ignoreTime) {
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                }
                break;

            }
            case AFTER: {
                if (ignoreTime) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Failed to match search type:" + searchType);
        }
        return DateSearchPart.comparePredicate(builder, search, searchType, including, cal.getTime());
    }

}
