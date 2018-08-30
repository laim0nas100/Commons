/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.mail.pop3.POP3Message;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.Log;
import lt.lb.commons.interfaces.Getter;
import lt.lb.commons.misc.F;
import lt.lb.commons.threads.DisposableExecutor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class JavaMailTest {

    public JavaMailTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    String pass = "";
    String user = "";
    String host = "";

    @Test
    public void ok() throws Exception {

        EmailChecker checker = new EmailChecker();
        POP3SEmailProps props = new POP3SEmailProps();
        props.host = host;
        props.password = pass;
        props.port = 993;
        props.username = user;

        EmailChannels channels = new EmailChannels();
        Consumer<Exception> errChannel = (e -> e.printStackTrace());
        HashSet<String> ids = new HashSet<>();
        channels.inputChannels.add(m -> {
            F.runWithHandler((e) -> {
                F.iterate(channels.errorChannels, (i, cha) -> {
                    cha.accept(e);
                });
            }, () -> {
                if (ids.contains(m.getMessageID())) {
                    return;
                } else {
                    ids.add(m.getMessageID());
                }
                POP3Message message = F.cast(m);
                Log.print(message.getClass());
                Log.print("---------------------------------");
                Log.print("ID: " + message.getMessageID());
                Log.print("Subject: " + message.getSubject());
                Log.print("Sent date: " + message.getSentDate());
                Log.print("From: " + message.getFrom()[0]);
                Log.print("Text: " + message.getContent().toString());
                Log.print(ids.size());
            });
        });
        channels.errorChannels.add(errChannel);
        Callable createPop3Poller = EmailChecker.createPop3Poller(props, channels);
        checker.addSchedulingTask(createPop3Poller, TimeUnit.SECONDS, 20);
        while (true) {
            Log.print("WAITING XDD");
            Thread.sleep(10 * 1000);
        }
    }

    public void checkEmail() throws Exception {

        Properties properties = new Properties();

        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", "993");
        properties.put("mail.pop3.starttls.enable", "true");
        Session emailSession = Session.getDefaultInstance(properties);

        Store store = emailSession.getStore("pop3s");

        store.connect(host, user, pass);

        Folder emailFolder = store.getFolder("INBOX");
        emailFolder.open(Folder.READ_ONLY);

        javax.mail.search.FlagTerm ft = null;
        Message[] messages = emailFolder.search(ft);
        Log.print("messages.length---" + messages.length);

        for (int i = 0, n = messages.length; i < n; i++) {

            POP3Message message = F.cast(messages[i]);
            Log.print(message.getClass());
            Log.print("---------------------------------");
            Log.print("Email Number " + (i + 1));
            Log.print("Subject: " + message.getSubject());
            Log.print("From: " + message.getFrom()[0]);
            Log.print("Text: " + message.getContent().toString());
            message.getRecipients(Message.RecipientType.TO);

        }

        emailFolder.close(false);
        Log.print("Closed folder");
        store.close();
        Log.print("Closed email");
    }

    public static abstract class BasicEmailProps extends Properties {

        public String username;
        public String password;
        public String host;
        public int port;

        public abstract void populate();
    }

    public static abstract class SearchableEmailProps extends BasicEmailProps {

        public static final SearchTerm SEARCH_TERM_UNSEEN = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

        public SearchTerm searchTerm = SEARCH_TERM_UNSEEN;
    }

    public static interface GetStore {

        public String getStore();
    }

    public static interface MarkRead {

        public Boolean isMarkRead();
    }

    public static class IMAPEmailProps extends SearchableEmailProps implements GetStore, MarkRead {

        public Boolean isMarkRead = false;

        @Override
        public String getStore() {
            return "imap";
        }

        @Override
        public Boolean isMarkRead() {
            return isMarkRead;
        }

        @Override
        public void populate() {

        }
    }

    public static class IMAPSEmailProps extends IMAPEmailProps {

        @Override
        public String getStore() {
            return super.getStore() + "s";
        }
    }

    public static class POP3EmailProps extends SearchableEmailProps implements GetStore, MarkRead {

        public POP3EmailProps() {

        }

        @Override
        public String getStore() {
            return "pop3";
        }

        @Override
        public Boolean isMarkRead() {
            return false;
        }

        @Override
        public void populate() {
            this.put("mail.pop3.starttls.enable", "true");
            this.put("mail.pop3.host", this.host + "");
            this.put("mail.pop3.port", this.port + "");
        }
    }

    public static class POP3SEmailProps extends POP3EmailProps {

        @Override
        public String getStore() {
            return super.getStore() + "s";
        }
    }

    public static class EmailChannels {

        public List<Consumer<MimeMessage>> inputChannels = new LinkedList<>();
        public List<Consumer<Exception>> errorChannels = new LinkedList<>();
    }

    public static class EmailChecker {

        private int poolSize = 4;
        private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        private Executor exe = new DisposableExecutor(1);

        public void addSchedulingTask(Callable call, TimeUnit tu, long dur) {

            Runnable runProxy = () -> {
                FutureTask task = new FutureTask<>(call);
                exe.execute(task);
            };
            service.scheduleAtFixedRate(runProxy, 0, dur, tu);
        }

        public static Callable createPop3Poller(POP3EmailProps props, EmailChannels channels) {
            return createEmailPoller(props, props, props, channels);
        }

        private static Callable createEmailPoller(SearchableEmailProps p, GetStore storeGet, MarkRead markRead, EmailChannels channels) {
            return () -> {

                p.populate();
                Session emailSession = Session.getDefaultInstance(p);

                Store store = emailSession.getStore(storeGet.getStore());
                store.connect(p.host, p.username, p.password);
                Log.print("Connected to store");

                Folder emailFolder = store.getFolder("INBOX");
                if (markRead.isMarkRead()) {
                    emailFolder.open(Folder.READ_WRITE);
                } else {
                    emailFolder.open(Folder.READ_ONLY);
                }
                Log.print("Opened email");

                Message[] messages;
                if (p.searchTerm != null) {
                    messages = emailFolder.search(p.searchTerm);
                } else {
                    messages = emailFolder.getMessages();
                }
                MimeMessage[] msg = new MimeMessage[messages.length];
                F.iterate(messages, (i, m) -> {
                    if (m instanceof MimeMessage) {
                        msg[i] = F.cast(m);
                    }
                    F.iterate(channels.inputChannels, (in, ch) -> {
                        ch.accept(msg[i]);
                    });
                    if (markRead.isMarkRead()) {
                        try {
                            Log.print("try writing");
                            m.setFlag(Flag.SEEN, true);
                            Log.print("Ok writing");
                        } catch (MessagingException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                emailFolder.close(false);
                Log.print("Closed folder");
                store.close();
                Log.print("Closed email");
                return msg;
            };
        }

    }
}
