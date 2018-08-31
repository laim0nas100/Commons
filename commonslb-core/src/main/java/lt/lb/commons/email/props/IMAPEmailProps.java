/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.email.props;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class IMAPEmailProps extends IMAPOrPOP3Props {

    public boolean partialFetch = false;

    @Override
    public String getStore() {
        return "imap";
    }

    @Override
    public void populate() {
        this.put("mail.imap.port", this.port + "");
        this.put("mail.imap.partialfetch", this.partialFetch + "");
    }

    public static class IMAPSEmailProps extends IMAPEmailProps {

        @Override
        public String getStore() {
            return super.getStore() + "s";
        }
    }
}
