package lt.lb.commons.email.props;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import java.util.List;
import java.util.function.Consumer;
import lt.lb.uncheckedutils.Checked;

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
            Checked.uncheckedRun(() -> {
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
            super.populate(); 
        }
        
    }
}
