package emailtest;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.lang.reflect.Field;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import lt.lb.commons.Log;
import lt.lb.commons.email.EmailChannels;
import lt.lb.commons.email.EmailChecker;
import lt.lb.commons.email.props.POP3EmailProps;
import lt.lb.commons.F;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.Tracer;
import lt.lb.commons.containers.NumberValue;
import lt.lb.commons.containers.Value;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.io.FileReader;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.threads.FastExecutor;
import lt.lb.commons.wekaparsing.WekaParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
        String taskID = checker.addSchedulingTask(new FastExecutor(1), createPop3Poller, TimeUnit.SECONDS, 20);
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

    public void other() {
        Executor e = new FastExecutor(1);
        EmailChecker ch = new EmailChecker();
        ch.addSchedulingTask(e, () -> {
            Log.print("Hi 2");
        }, TimeUnit.SECONDS, 2);
        ch.addSchedulingTask(e, () -> {
            Log.print("Hi 1");
        }, TimeUnit.SECONDS, 1);
        ch.addSchedulingTask(e, () -> {
            Log.print("Hi 3");
        }, TimeUnit.SECONDS, 3);

        while (true) {
            F.unsafeRun(() -> {
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
    public static class EmailAttributes {

        public Integer lineCount;

        public Integer maxRepeatedWordCount;
        public Integer maxRepeatedCapitalWordCount;

        public Double capitalCharTotal;
        public Double capitalRunTotal;
        public Integer minCapitalRun;
        public Integer maxCapitalRun;

        public Double averageWordLengh;
        public Integer minWordLength;
        public Integer maxWordLength;

        public Double numberCount;

        public Double alphaCount;
        public Double alphaNumCount;
        public Double alphaWordCount;

        //alpha sequences
        public Double charSeq1;
        public Double charSeq2;
        public Double charSeq3;
        public Double charSeq4;
        public Double charSeq5;
        public Double charSeq6;
        public Double charSeq7;
        public Double charSeq8;
        public Double charSeq9;

        public Double whiteSpaceCount;

        public Boolean spam;
        

    }

    public Integer countMatcher(Matcher m) {
        int counter = 0;
        while (m.find()) {
            counter++;
        }
        return counter;
    }

    public Integer countMatcher(String regex, String text) {
        Matcher m = Pattern.compile(regex).matcher(text);
        int counter = 0;
        while (m.find()) {
            counter++;
        }
        return counter;
    }

    

    public Consumer<Path> emailParser(Collection<EmailAttributes> toAdd, boolean toMark) {

        return (file) -> {
            F.unsafeRun(() -> {
                ArrayList<String> content = FileReader.readFromFile(file.toString());
                EmailAttributes em = new EmailAttributes();
                toAdd.add(em);
                em.spam = toMark;
                String all = "";
                for (String line : content) {
                    all += line + " ";
                }

                Integer cappitalCharTotal = 0;
                HashMap<String, NumberValue<Integer>> map = new HashMap<>();
                HashMap<String, NumberValue<Integer>> capMap = new HashMap<>();
                String word = "";
                String capitalWord = "";
                for (Character ch : all.toCharArray()) {

                    if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
                        word += ch;
                        if (Character.isUpperCase(ch)) {
                            capitalWord += ch;
                        } else {
                            if (!capitalWord.isEmpty()) {
                                if (capMap.containsKey(capitalWord)) {
                                    capMap.get(capitalWord).incrementAndGet();
                                } else {
                                    capMap.put(capitalWord, NumberValue.of(1));
                                }
                                capitalWord = "";
                            }
                        }
                    } else {
                        if (word.isEmpty()) {
                            //nothing
                        } else {
                            if (map.containsKey(word.toLowerCase())) {
                                map.get(word.toLowerCase()).incrementAndGet();
                            } else {
                                map.put(word.toLowerCase(), NumberValue.of(1));
                            }
                            word = "";
                        }
                    }
                }

                NumberValue<Double> wordCount = NumberValue.of(0d);
                NumberValue<Double> wordLengthTotal = NumberValue.of(0d);
                F.iterate(map, (k, i) -> {
                    wordCount.incrementAndGet(i.get());
                    wordLengthTotal.incrementAndGet(k.length() * i.get());
                });
                ArrayList<String> distinctWords = new ArrayList<>();
                distinctWords.addAll(map.keySet());

                em.maxWordLength = distinctWords.stream().map(m -> m.length()).sorted(ExtComparator.ofComparable().reversed()).findFirst().get();
                em.minWordLength = distinctWords.stream().map(m -> m.length()).sorted(ExtComparator.ofComparable()).findFirst().get();

                Double charCount = countMatcher(Pattern.compile("\\p{ASCII}").matcher(all)).doubleValue();

                em.alphaCount = countMatcher(Pattern.compile("\\p{Alpha}").matcher(all)) / charCount;
                em.alphaWordCount = countMatcher(Pattern.compile("(\\p{Alpha}){2,}+").matcher(all)) / wordCount.get();
                em.alphaNumCount = countMatcher("(\\p{Alnum})", all) / charCount;
                em.numberCount = countMatcher("\\p{Digit}", all) / wordCount.get();
                em.lineCount = content.size();

                em.whiteSpaceCount = countMatcher("\\p{Space}", all).doubleValue() / charCount;
                em.averageWordLengh = wordLengthTotal.get() / wordCount.get();

                NumberValue<Integer> capitalChars = NumberValue.of(0);
                NumberValue<Integer> capitalWords = NumberValue.of(0);
                F.iterate(capMap, (w, count) -> {
                    capitalChars.incrementAndGet(count.get() * w.length());
                    capitalWords.incrementAndGet(count.get());
                });

                ArrayList<String> distinctCapitalWords = new ArrayList<>();
                distinctCapitalWords.addAll(capMap.keySet());

                em.maxCapitalRun = distinctCapitalWords.stream().map(m -> m.length()).sorted(ExtComparator.ofComparable().reversed()).findFirst().get();
                em.minCapitalRun = distinctCapitalWords.stream().map(m -> m.length()).sorted(ExtComparator.ofComparable()).findFirst().get();

                em.capitalRunTotal = (double) capitalChars.get() / wordCount.get();
                em.capitalCharTotal = capitalChars.get() / charCount;

                em.charSeq1 = countMatcher("\\p{Alpha}{1}", all) / wordCount.get();
                em.charSeq2 = countMatcher("\\p{Alpha}{2}", all) / wordCount.get();
                em.charSeq3 = countMatcher("\\p{Alpha}{3}", all) / wordCount.get();
                em.charSeq4 = countMatcher("\\p{Alpha}{4}", all) / wordCount.get();
                em.charSeq5 = countMatcher("\\p{Alpha}{5}", all) / wordCount.get();
                em.charSeq6 = countMatcher("\\p{Alpha}{6}", all) / wordCount.get();
                em.charSeq7 = countMatcher("\\p{Alpha}{7}", all) / wordCount.get();
                em.charSeq8 = countMatcher("\\p{Alpha}{8}", all) / wordCount.get();
                em.charSeq9 = countMatcher("\\p{Alpha}{9}", all) / wordCount.get();

                em.maxRepeatedCapitalWordCount = distinctCapitalWords.stream().sorted(ExtComparator.ofValue(f -> capMap.get(f).get()).reversed()).findFirst().get().length();
                em.maxRepeatedWordCount = distinctWords.stream().sorted(ExtComparator.ofValue(f -> map.get(f).get()).reversed()).findFirst().get().length();

            });
        };
    }

    

    @Test
    public void wekaParseEmails() throws Exception {
        DirectoryStream<Path> goodStream = Files.newDirectoryStream(Paths.get("E:\\University\\DM\\pratybos\\03\\enron1\\ham"));
        DirectoryStream<Path> spamStream = Files.newDirectoryStream(Paths.get("E:\\University\\DM\\pratybos\\03\\enron1\\spam"));
        ArrayList<EmailAttributes> emailsGood = new ArrayList<>();
        ArrayList<EmailAttributes> emailsSpam = new ArrayList<>();

        goodStream.forEach(emailParser(emailsGood, false));
        spamStream.forEach(emailParser(emailsSpam, true));
        
        
        ArrayList<EmailAttributes> merged = new ArrayList<>();
        merged.addAll(emailsGood);
        merged.addAll(emailsSpam);
        
        FileReader.writeToFile("spamFile2.arff", new WekaParser(EmailAttributes.class, "spam").wekaReadyLines("EmailRelation", merged));
       
        Log.await(1, TimeUnit.HOURS);

    }
}
