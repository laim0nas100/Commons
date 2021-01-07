package lt.lb.commons.email;

import java.util.List;
import java.util.function.Consumer;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import lt.lb.commons.F;
import lt.lb.commons.email.props.IMAPOrPOP3Props;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.iteration.For;
import lt.lb.commons.threads.executors.ScheduledDispatchExecutor;

/**
 *
 * @author laim0nas100
 */
public class EmailChecker extends ScheduledDispatchExecutor {

    public ILineAppender debug;

    public EmailChecker() {
        debug = (objs) -> {
            return debug;
        };
    }

    public Runnable createCommonEmailPoller(IMAPOrPOP3Props p, EmailChannels channels) {
        p.populate();
        return () -> {

            F.unsafeRunWithHandler(channels.errorChannel, () -> {
                Session emailSession = null;
                Store store = null;
                Folder emailFolder = null;
                try {

                    emailSession = Session.getInstance(p);
                    debug.appendLine("Session == null ? " + (emailSession == null), " ", emailSession.getClass().getName());
                    debug.appendLine("Try get store " + p.getStore());
                    store = emailSession.getStore(p.getStore());
                    debug.appendLine("Store == null ? " + (store == null), " ", store.getClass().getName());
                    debug.appendLine("Try connect to store");
                    store.connect(p.host, p.port, p.username, p.password);
                    debug.appendLine("Try open folder " + p.folderName + " mode:" + p.getFolderOpenMode());
                    emailFolder = store.getFolder(p.folderName);
                    emailFolder.open(p.getFolderOpenMode());

                    Message[] messages;
                    debug.appendLine("Search messages");
                    if (p.searchTerm != null) {
                        messages = emailFolder.search(p.searchTerm);
                    } else {
                        messages = emailFolder.getMessages();
                    }
                    List<Consumer<Message>> afterReadActions = p.getAfterReadActions();
                    For.elements().iterate(messages, (i, m) -> {
                        if (m == null) {
                            debug.appendLine("Found null message at " + i);
                        } else {
                            if (m instanceof MimeMessage) {
                                MimeMessage cast = F.cast(m);
                                debug.appendLine(i + " Send message");
                                channels.inputChannel.accept(cast);
                                for (Consumer<Message> cons : afterReadActions) {
                                    cons.accept(m);
                                }
                            } else {
                                debug.appendLine("Wrong type " + m.getClass().getName());
                            }
                        }

                    });

                } finally {
                    debug.appendLine("Close folder");
                    if (emailFolder != null) {
                        emailFolder.close(true);
                    }
                    debug.appendLine("Close store");
                    if (store != null) {
                        store.close();
                    }
                }

            });

        };
    }
}
