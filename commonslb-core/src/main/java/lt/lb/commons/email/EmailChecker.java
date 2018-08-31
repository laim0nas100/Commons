/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.email;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import lt.lb.commons.UUIDgenerator;
import lt.lb.commons.containers.Value;
import lt.lb.commons.email.props.IMAPOrPOP3Props;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.misc.F;
import lt.lb.commons.threads.DisposableExecutor;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class EmailChecker {

    public ILineAppender debug;
    private ScheduledExecutorService service;
    private Executor exe;
    private HashMap<String, Value<Boolean>> enabledMap = new HashMap<>();

    public EmailChecker() {
        debug = (objs) -> {
            return debug;
        };
    }

    public String addSchedulingTask(Runnable call, TimeUnit tu, long dur) {
        String nextUUID = UUIDgenerator.nextUUID("Email task");
        AtomicBoolean enabled = new AtomicBoolean(true);
        Runnable runProxy = () -> {
            if (enabled.get()) {
                debug.appendLine("Execute ", nextUUID);
                getExecutor().execute(call);
            } else {
                debug.appendLine("Disabled ", nextUUID);
            }
        };
        enabledMap.put(nextUUID, new Value<>(true));
        getService().scheduleAtFixedRate(runProxy, 0, dur, tu);
        return nextUUID;
    }

    public Runnable createCommonEmailPoller(IMAPOrPOP3Props p, EmailChannels channels) {
        p.populate();
        return () -> {

            F.unsafeRunWithHandler(channels.errorChannel, () -> {
                Session emailSession = Session.getInstance(p);
                debug.appendLine("Session == null ?" + (emailSession == null)," ",emailSession.getClass().getName());
                debug.appendLine("Try get store " + p.getStore());
                Store store = emailSession.getStore(p.getStore());
                debug.appendLine("Store == null ?" + (store == null)," ",store.getClass().getName());
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
                F.iterate(messages, (i, m) -> {
                    if (m == null) {
                        debug.appendLine("Found null message at " + i);
                    } else {
                        if (m instanceof MimeMessage) {
                            MimeMessage cast = F.cast(m);
                            debug.appendLine(i + " Send message");
                            channels.inputChannel.accept(cast);
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

    private Executor getExecutor() {
        if (exe == null) {
            exe = new DisposableExecutor(1);
        }
        return exe;
    }

    private ScheduledExecutorService getService() {
        if (service == null) {
            service = Executors.newSingleThreadScheduledExecutor();
        }
        return service;
    }

    public void shutdown() {
        this.getService().shutdown();
        this.service = null;
    }

    public void enableEmailTask(String uuid, boolean enable) {
        if (this.enabledMap.containsKey(uuid)) {
            this.enabledMap.get(uuid).set(enable);
        }
    }

}
