/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.email.props;

import java.util.List;
import java.util.function.Consumer;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public class IMAPEmailProps extends IMAPOrPOP3Props {

    public boolean partialFetch = false;

    public boolean markAsSeen = true;

    @Override
    public String getStore() {
        return "imap";
    }

    @Override
    public void populate() {
        this.put("mail.imap.port", this.port + "");
        this.put("mail.imap.partialfetch", this.partialFetch + "");
    }

    @Override
    public List<Consumer<Message>> getAfterReadActions() {
        List<Consumer<Message>> list = super.getAfterReadActions();
        list.add((m) -> {
            if (this.getFolderOpenMode() != Folder.READ_WRITE) {
                return;
            }
            F.uncheckedRun(() -> {
                m.setFlag(Flags.Flag.SEEN, markAsSeen);
            });
        });
        return list;
    }

    public static class IMAPSEmailProps extends IMAPEmailProps {

        @Override
        public String getStore() {
            return super.getStore() + "s";
        }

        @Override
        public void populate() {
            super.populate(); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
