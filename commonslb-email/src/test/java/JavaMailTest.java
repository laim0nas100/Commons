/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import lt.lb.commons.Log;
import lt.lb.commons.email.EmailChannels;
import lt.lb.commons.email.EmailChecker;
import lt.lb.commons.email.props.POP3EmailProps;
import lt.lb.commons.misc.F;
import lt.lb.commons.threads.DisposableExecutor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author laim0nas100
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

    public void ok() throws Exception {

        EmailChecker checker = new EmailChecker();
        POP3EmailProps props = new POP3EmailProps.POP3SEmailProps();
        props.host = host;
        props.password = pass;
        props.port = 993;
        props.username = user;
        props.setFolderOpenMode(Folder.READ_WRITE);

        EmailChannels channels = new EmailChannels();
        Consumer<Exception> errChannel = (e -> e.printStackTrace());
        HashSet<String> ids = new HashSet<>();
        channels.errorChannel = errChannel;
        channels.inputChannel = (m -> {
            F.unsafeRunWithHandler(errChannel, () -> {
                if (ids.contains(m.getMessageID())) {
                    return;
                } else {
                    ids.add(m.getMessageID());
                }
                Log.print(m.getClass());
                Log.print("---------------------------------");
                Log.print("ID: " + m.getMessageID());
                Log.print("Subject: " + m.getSubject());
                Log.print("Sent date: " + m.getSentDate());
                Log.print("From: " + m.getFrom()[0]);
//                Log.print("Text: " + message.getContent().toString());
                Log.print(ids.size());
            });
        });

        Runnable createPop3Poller = checker.createCommonEmailPoller(props, channels);
        checker.debug = (objs) -> {
            Log.print(objs);
            return checker.debug;
        };
        String taskID = checker.addSchedulingTask(new DisposableExecutor(1), createPop3Poller, TimeUnit.SECONDS, 20);
        int i = 1;
        boolean enabled = true;
        while (true) {
            Log.print("WAITING XDD");
            Thread.sleep(10 * 1000);
            if (i % 5 == 0) {
                enabled = !enabled;
                checker.setEnabledTask(taskID, enabled);
            }
            i++;
        }
    }

    public static void main(String[] args) throws Exception {
        JavaMailTest t = new JavaMailTest();
        t.other();
    }
    
    public void other(){
        Executor e = new DisposableExecutor(1);
        EmailChecker ch = new EmailChecker();
        ch.addSchedulingTask(e, ()->{
            Log.print("Hi 2");
        }, TimeUnit.SECONDS, 2);
        ch.addSchedulingTask(e, ()->{
            Log.print("Hi 1");
        }, TimeUnit.SECONDS, 1);
        ch.addSchedulingTask(e, ()->{
            Log.print("Hi 3");
        }, TimeUnit.SECONDS, 3);
        
        while(true){
            F.unsafeRun(()->{
                Thread.sleep(1000);
            });
        }
        
        
    }

//    public void checkEmail() throws Exception {
//
//        Properties properties = new Properties();
//
//        properties.put("mail.pop3.host", host);
//        properties.put("mail.pop3.port", "993");
//        properties.put("mail.pop3.starttls.enable", "true");
//        Session emailSession = Session.getDefaultInstance(properties);
//
//        Store store = emailSession.getStore("pop3s");
//
//        store.connect(host, user, pass);
//
//        Folder emailFolder = store.getFolder("INBOX");
//        emailFolder.open(Folder.READ_ONLY);
//
//        javax.mail.search.FlagTerm ft = null;
//        Message[] messages = emailFolder.search(ft);
//        Log.print("messages.length---" + messages.length);
//
//        for (int i = 0, n = messages.length; i < n; i++) {
//
//            POP3Message message = F.cast(messages[i]);
//            Log.print(message.getClass());
//            Log.print("---------------------------------");
//            Log.print("Email Number " + (i + 1));
//            Log.print("Subject: " + message.getSubject());
//            Log.print("From: " + message.getFrom()[0]);
//            Log.print("Text: " + message.getContent().toString());
//            message.getRecipients(Message.RecipientType.TO);
//
//        }
//
//        emailFolder.close(false);
//        Log.print("Closed folder");
//        store.close();
//        Log.print("Closed email");
//    }
}
