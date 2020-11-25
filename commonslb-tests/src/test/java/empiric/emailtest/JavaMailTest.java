package empiric.emailtest;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.mail.Folder;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.Log;
import lt.lb.commons.email.EmailChannels;
import lt.lb.commons.email.EmailChecker;
import lt.lb.commons.email.props.POP3EmailProps;
import lt.lb.commons.F;
import lt.lb.commons.Predicates;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.containers.values.NumberValue;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.email.props.IMAPEmailProps;
import lt.lb.commons.io.TextFileIO;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.threads.executors.FastExecutor;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.executors.TaskBatcher;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.commons.wekaparsing.Comment;
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

//    @Test
    public void ok() throws Exception {

        EmailChecker checker = new EmailChecker();
        IMAPEmailProps.IMAPSEmailProps props = new IMAPEmailProps.IMAPSEmailProps() {
            @Override
            public void populate() {
                this.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                this.setProperty("mail.imaps.socketFactory.fallback", "false");
                this.setProperty("mail.imaps.port", "993");
                this.setProperty("mail.imaps.socketFactory.port", "993");
                this.setProperty( "mail.store.protocol", "imaps");

            }
        };
        props.host = host;
        props.password = pass;
        props.port = 993;
        props.username = user;
        props.setFolderOpenMode(Folder.READ_ONLY);

        EmailChannels channels = new EmailChannels();
        Consumer<Throwable> errChannel = (e -> e.printStackTrace());
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
        String taskID = checker.addSchedulingTask(new FastExecutor(1), createPop3Poller, TimeUnit.SECONDS, 10);
        int i = 1;
        boolean enabled = true;
        while (true) {
            Log.print("WAITING XDD");
            Thread.sleep(10 * 1000);
//            if (i % 5 == 0) {
//                enabled = !enabled;
//                checker.setEnabledTask(taskID, enabled);
//            }
//            i++;
        }
    }

    public static void main(String[] args) throws Exception {
        JavaMailTest t = new JavaMailTest();
//        t.wekaParseEmails();
        t.ok();
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

        @Comment("Number of lines")
        public Integer lineCount;

        @Comment("Maximum repeated word count")
        public Integer maxRepeatedWordCount;
        @Comment("Maximum repeated capital word count")
        public Integer maxRepeatedCapitalWordCount;

        @Comment("Capital characters count / char count")
        public Double capitalCharTotal;

        @Comment("Capital character word count / word count")
        public Double capitalRunTotal;

        @Comment("Shortest capital word")
        public Integer minCapitalRun;
        @Comment("Longest capital word")
        public Integer maxCapitalRun;

        @Comment("Average word length")
        public Double averageWordLengh;
        @Comment("Shortest word length")
        public Integer minWordLength;
        @Comment("Longest word length")
        public Integer maxWordLength;

        @Comment("Number count / word count")
        public Double numberCount;

        @Comment("Alpha caracter count / character count")
        public Double alphaCount;
        @Comment("Alpha-numeric caracter count / character count")
        public Double alphaNumCount;
        @Comment("Alpha word count / word count")
        public Double alphaWordCount;

        //alpha sequences
        @Comment("Alpha character sequence of length 1 / word count")
        public Double charSeq1;
        @Comment("Alpha character sequence of length 2 / word count")
        public Double charSeq2;
        @Comment("Alpha character sequence of length 3 / word count")
        public Double charSeq3;
        @Comment("Alpha character sequence of length 4 / word count")
        public Double charSeq4;
        @Comment("Alpha character sequence of length 5 / word count")
        public Double charSeq5;
        @Comment("Alpha character sequence of length 6 / word count")
        public Double charSeq6;
        @Comment("Alpha character sequence of length 7 / word count")
        public Double charSeq7;
        @Comment("Alpha character sequence of length 8 / word count")
        public Double charSeq8;
        @Comment("Alpha character sequence of length 9 / word count")
        public Double charSeq9;

        @Comment("Whitespace characters count / character count")
        public Double whiteSpaceCount;

        @Comment("Wether email is spam")
        public EmailType spam;

    }

    public static enum EmailType {
        SPAM, NOTSPAM;
    }

    public Integer countMatcher(Matcher m) {
        int counter = 0;
        while (m.find()) {
            counter++;
        }
        return counter;
    }

    public Long countWordsOfLength(Stream<String> stream, int length) {
        return countWords(stream, s -> s.length() == length);
    }

    public Long countWords(Stream<String> coll, Predicate<String> str) {
        return coll.filter(str).count();
    }

    public Integer countMatcher(String regex, String text) {
        Matcher m = Pattern.compile(regex).matcher(text);
        int counter = 0;
        while (m.find()) {
            counter++;
        }
        return counter;
    }

    public EmailAttributes emailParser(Path file, boolean toMark) {
        Value<EmailAttributes> val = new Value<>();
        F.unsafeRun(() -> {
            ArrayList<String> content = TextFileIO.readFromFile(file.toString());
            EmailAttributes em = new EmailAttributes();
            val.set(em);
            em.spam = toMark ? EmailType.SPAM : EmailType.NOTSPAM;
            String all = "";
            for (String line : content) {
                all += line + "\n";
            }

            Integer cappitalCharTotal = 0;
            HashMap<String, IntegerValue> map = new HashMap<>();
            HashMap<String, IntegerValue> capMap = new HashMap<>();
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
                                capMap.put(capitalWord, new IntegerValue(1));
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
                            map.put(word.toLowerCase(), new IntegerValue(1));
                        }
                        word = "";
                    }
                }
            }

            NumberValue<Double> wordCount = NumberValue.of(0d);
            NumberValue<Double> wordLengthTotal = NumberValue.of(0d);
            Iter.iterate(map, (k, i) -> {
                wordCount.incrementAndGet(i.get());
                wordLengthTotal.incrementAndGet(k.length() * i.get());
            });
            NumberValue<Integer> capitalChars = NumberValue.of(0);
            NumberValue<Integer> capitalWords = NumberValue.of(0);
            Iter.iterate(capMap, (w, count) -> {
                capitalChars.incrementAndGet(count.get() * w.length());
                capitalWords.incrementAndGet(count.get());
            });

            wordCount.incrementAndGet(capitalChars.get());
            wordLengthTotal.incrementAndGet(capitalChars.get());

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

            ArrayList<String> distinctCapitalWords = new ArrayList<>();
            distinctCapitalWords.addAll(capMap.keySet());

            em.maxCapitalRun = distinctCapitalWords.stream().map(m -> m.length()).sorted(ExtComparator.ofComparable().reversed()).findFirst().get();
            em.minCapitalRun = distinctCapitalWords.stream().map(m -> m.length()).sorted(ExtComparator.ofComparable()).findFirst().get();

            em.capitalRunTotal = (double) capitalChars.get() / wordCount.get();
            em.capitalCharTotal = capitalChars.get() / charCount;

            em.charSeq1 = countWordsOfLength(map.keySet().stream(), 1) / wordCount.get();
            em.charSeq2 = countWordsOfLength(map.keySet().stream(), 2) / wordCount.get();
            em.charSeq3 = countWordsOfLength(map.keySet().stream(), 3) / wordCount.get();
            em.charSeq4 = countWordsOfLength(map.keySet().stream(), 4) / wordCount.get();
            em.charSeq5 = countWordsOfLength(map.keySet().stream(), 5) / wordCount.get();
            em.charSeq6 = countWordsOfLength(map.keySet().stream(), 6) / wordCount.get();
            em.charSeq7 = countWordsOfLength(map.keySet().stream(), 7) / wordCount.get();
            em.charSeq8 = countWordsOfLength(map.keySet().stream(), 8) / wordCount.get();
            em.charSeq9 = countWordsOfLength(map.keySet().stream(), 9) / wordCount.get();

            em.maxRepeatedCapitalWordCount = distinctCapitalWords.stream().sorted(ExtComparator.ofValue(f -> capMap.get(f).get()).reversed()).findFirst().get().length();
            em.maxRepeatedWordCount = distinctWords.stream().sorted(ExtComparator.ofValue(f -> map.get(f).get()).reversed()).findFirst().get().length();

        });

        return val.get();
    }

    public void wekaParseEmails() throws Exception {
        DirectoryStream<Path> goodStream = Files.newDirectoryStream(Paths.get("E:\\University\\MIF_Master_Informatics_Semester1\\Data Analysis\\cp\\pratybos\\03\\enron1\\ham"));
        DirectoryStream<Path> spamStream = Files.newDirectoryStream(Paths.get("E:\\University\\MIF_Master_Informatics_Semester1\\Data Analysis\\cp\\pratybos\\03\\enron1\\spam"));
        ConcurrentLinkedDeque<EmailAttributes> deque = new ConcurrentLinkedDeque<>();

        TaskBatcher batcher = new TaskBatcher(new FastWaitingExecutor(8, WaitTime.ofSeconds(2)));

        spamStream.forEach(es -> {
            batcher.execute(() -> {
                EmailAttributes emailParser = emailParser(es, true);
                deque.add(emailParser);
            });

        });
        goodStream.forEach(es -> {
            batcher.execute(() -> {
                EmailAttributes emailParser = emailParser(es, false);
                deque.add(emailParser);
            });

        });

        batcher.awaitFailOnFirst();

        TextFileIO.writeToFile("spamFile3.arff", new WekaParser(EmailAttributes.class,
                "spam").wekaReadyLines("EmailRelation", deque));

        Log.await(1, TimeUnit.HOURS);

    }
}
