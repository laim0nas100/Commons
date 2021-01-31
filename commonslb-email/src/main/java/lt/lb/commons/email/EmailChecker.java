package lt.lb.commons.email;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import lt.lb.commons.F;
import lt.lb.commons.email.props.IMAPOrPOP3Props;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.iteration.For;
import lt.lb.commons.misc.NestedException;
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

    public Runnable createCommonEmailPollerExposed(IMAPOrPOP3Props p, EmailChannels channels, ExposedEmailConnection exposed) {

        Objects.requireNonNull(p);
        Objects.requireNonNull(channels);
        Objects.requireNonNull(exposed);

        return () -> {
            checkEmail(p, channels, exposed);
        };
    }

    /**
     * Check email.
     * @param p email properties
     * @param channels input and error channels
     * @param exposed optional connection expose
     */
    public void checkEmail(IMAPOrPOP3Props p, EmailChannels channels, ExposedEmailConnection exposed) {
        p.populate();
        F.uncheckedRunWithHandler(channels.errorChannel, () -> {
            Session emailSession = null;
            Store store = null;
            Folder emailFolder = null;
            try {

                emailSession = Session.getInstance(p);
                if (exposed != null) {
                    exposed.emailSession.complete(emailSession);
                }
                debug.appendLine("Session == null ? " + (emailSession == null), " ", emailSession.getClass().getName());
                debug.appendLine("Try get store " + p.getStore());
                store = emailSession.getStore(p.getStore());
                if (exposed != null) {
                    exposed.emailStore.complete(store);
                }
                debug.appendLine("Store == null ? " + (store == null), " ", store.getClass().getName());
                debug.appendLine("Try connect to store");
                store.connect(p.host, p.port, p.username, p.password);
                debug.appendLine("Try open folder " + p.folderName + " mode:" + p.getFolderOpenMode());
                emailFolder = store.getFolder(p.folderName);
                emailFolder.open(p.getFolderOpenMode());
                if (exposed != null) {
                    exposed.emailFolder.complete(emailFolder);
                }

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

                final Folder finalFolder = emailFolder;
                F.checkedRun(() -> {
                    debug.appendLine("Close folder");
                    if (finalFolder != null) {
                        finalFolder.close();
                    }
                }).ifPresent(channels.errorChannel);

                final Store finalStore = store;

                F.checkedRun(() -> {
                    debug.appendLine("Close store");
                    if (finalStore != null) {
                        finalStore.close();
                    }
                }).ifPresent(channels.errorChannel);

            }

        });

    }

    public Runnable createCommonEmailPoller(IMAPOrPOP3Props p, EmailChannels channels) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(channels);
        return () -> {
            checkEmail(p, channels, null);
        };
    }
}
