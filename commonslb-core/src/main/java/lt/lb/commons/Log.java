/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class Log {

    public enum LogStream {
        FILE, STD_OUT, STD_ERR
    }

    private static PrintStream printStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
    private static boolean isFileOpen = false;
    public static boolean async = true;
    public static boolean keepBufferForFile = false;
    public static boolean timeStamp = true;
    public static boolean threadName = true;
    public static boolean display = true;
    public static boolean disable = false;
    private static DateTimeFormatter timeStringFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static ExecutorService exe = Executors.newSingleThreadExecutor();
    public static final ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<>();

    protected Log() {

    }

    public static void useTimeFormat(String format) {
        timeStringFormat = DateTimeFormatter.ofPattern(format);

    }

    public static void changeStream(LogStream c, String... path) throws IOException {
        isFileOpen = false;
        if (null == c) {
            printStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
        } else {
            switch (c) {
                case FILE:
                    printStream = new PrintStream(path[0], "UTF-8");
                    isFileOpen = true;
                    break;
                case STD_ERR:
                    printStream = new PrintStream(new FileOutputStream(FileDescriptor.err));
                    break;
                default:
                    printStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
                    break;
            }
        }
    }

    public static void await(long timeout, TimeUnit tu) throws InterruptedException {
        ExecutorService serv = exe;
        exe = Executors.newSingleThreadExecutor();
        serv.shutdown();
        serv.awaitTermination(timeout, tu);
    }

    public static void flushBuffer() {
        while (!list.isEmpty()) {
            String string = list.pollFirst();
            printStream.println(string);
        }

    }

    public static void close() {
        try {
            exe.shutdown();
            exe.awaitTermination(10, TimeUnit.MINUTES);
            printStream.flush();
            if (isFileOpen) {
                printStream.close();
            }
        } catch (InterruptedException ex) {
        }
    }

    public static void printLines(Collection col) {
        if (disable) {
            return;
        }
        processString(printLinesDecorator.apply(col));
    }

    public static <T> void print(T... objects) {
        if (disable) {
            return;
        }
        processString(printDecorator.apply(objects));
    }

    public static <T> void println(T... objects) {
        if (disable) {
            return;
        }

        processString(printLnDecorator.apply(objects));
    }

    private static void processString(Supplier<String> string) {
        if (override == null) {
            long millis = System.currentTimeMillis();
            final Thread thread = Thread.currentThread();
            if (async) {
                exe.submit(() -> logThis(string.get(), thread, millis));
            } else {
                logThis(string.get(), thread, millis);
            }

        } else {
            override.accept(string);
        }
    }

    private static final Lambda.L1R<Object[], Supplier<String>> printLnDecorator = Lambda.of((Object[] objs) -> {
        return () -> {
            LineStringBuilder sb = new LineStringBuilder();
            if (objs.length == 1) {
                sb.append(String.valueOf(objs[0]));
            } else if (objs.length > 1) {
                for (Object s : objs) {
                    sb.appendLine(String.valueOf(s));
                }
            }
            if (sb.length() > 0) {
                sb.removeFromEnd(LineStringBuilder.LINE_END.length());
            }
            return sb.toString();
        };

    });

    private static final Lambda.L1R<Object[], Supplier<String>> printDecorator = Lambda.of((Object[] objs) -> {
        return () -> {
            LineStringBuilder string = new LineStringBuilder();
            if (objs.length > 0) {
                for (Object s : objs) {
                    string.append(", " + s);
                }
                string.delete(0, 2);
            }
            return string.toString();
        };

    });

    private static final Lambda.L1R<Collection, Supplier<String>> printLinesDecorator = Lambda.of((Collection col) -> {
        return () -> {
            LineStringBuilder string = new LineStringBuilder();
            if (!col.isEmpty()) {
                for (Object s : col) {
                    string.appendLine(s);
                }
                string.prependLine();
            }
            return string.toString();
        };

    });

    private static void logThis(String string, Thread thread, long millis) {
        String timeSt = timeStamp ? getZonedDateTime(timeStringFormat, millis) : "";
        String threadSt = threadName ? "[" + thread.getName() + "]" : "";
        String res = timeSt + threadSt + "{" + string + "}";
        if (display) {
            System.out.println(res);
        }
        if (keepBufferForFile) {
            Log.list.add(res);
        }
        if (isFileOpen) {
            flushBuffer();
        }

    }

    public static String getZonedDateTime(String format) {
        return ZonedDateTime.now(ZoneOffset.systemDefault()).format(DateTimeFormatter.ofPattern(format));
    }

    public static String getZonedDateTime(DateTimeFormatter format, long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.systemDefault()).format(format);
    }

    public static void printProperties(Properties properties) {
        Object[] toArray = properties.keySet().toArray();

        for (Object o : toArray) {
            String property = properties.getProperty((String) o);
            println(o.toString() + " : " + property);
        }
    }

    public static PrintStream getPrintStream() {
        return printStream;
    }

    public static Consumer<Supplier<String>> override;
}
