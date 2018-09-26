/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.email.props;

import javax.mail.Flags;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

/**
 *
 * @author laim0nas100
 */
public abstract class SearchableEmailProps extends BasicEmailProps {

    public static final SearchTerm SEARCH_TERM_UNSEEN = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

    public SearchTerm searchTerm = SEARCH_TERM_UNSEEN;
}
