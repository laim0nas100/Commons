package lt.lb.commons.email.props;

import jakarta.mail.Flags;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.SearchTerm;


/**
 *
 * @author laim0nas100
 */
public abstract class SearchableEmailProps extends BasicEmailProps {

    public static final SearchTerm SEARCH_TERM_UNSEEN = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

    public SearchTerm searchTerm = SEARCH_TERM_UNSEEN;
}
