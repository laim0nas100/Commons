package lt.lb.commons.email;

import java.util.List;
import java.util.function.Consumer;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import lt.lb.commons.email.props.IMAPOrPOP3Props;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.F;
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
                Session emailSession = Session.getInstance(p);
                debug.appendLine("Session == null ?" + (emailSession == null), " ", emailSession.getClass().getName());
                debug.appendLine("Try get store " + p.getStore());
                Store store = emailSession.getStore(p.getStore());
                debug.appendLine("Store == null ?" + (store == null), " ", store.getClass().getName());
                debug.appendLine("Try connect to store");
                store.connect(p.host, p.username, p.password);
                debug.appendLine("Try open folder " + p.folderName + " mode:" + p.getFolderOpenMode());
                Folder emailFolder = store.getFolder(p.folderName);
                emailFolder.open(p.getFolderOpenMode());

                Message[] messages;
                debug.appendLine("Search messages");
                if (p.searchTerm != null) {
                    messages = emailFolder.search(p.searchTerm);
                } else {
                    messages = emailFolder.getMessages();
                }
                List<Consumer<Message>> afterReadActions = p.getAfterReadActions();
                F.iterate(messages, (i, m) -> {
                    if (m == null) {
                        debug.appendLine("Found null message at " + i);
                    } else {
                        if (m instanceof MimeMessage) {
                            MimeMessage cast = F.cast(m);
                            debug.appendLine(i + " Send message");
                            channels.inputChannel.accept(cast);
                            afterReadActions.forEach(action -> {
                                action.accept(m);
                            });
                        } else {
                            debug.appendLine("Wrong type " + m.getClass().getName());
                        }
                    }

                });

                debug.appendLine("Close folder");
                emailFolder.close(true);
                debug.appendLine("Session over");
                store.close();
            });

        };
    }
}
