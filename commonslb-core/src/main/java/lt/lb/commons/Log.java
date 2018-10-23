/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

import java.io.*;
import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lt.lb.commons.containers.StringValue;

/**
 *
 * @author laim0nas100
 */
public class Log {

    private static Log mainLog = new Log();

    public static Log main() {
        return mainLog;
    }

    public static enum LogStream {
        FILE, STD_OUT, STD_ERR
    }

    protected PrintStream printStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
    protected boolean isFileOpen = false;
    public boolean async = true;
    public boolean keepBufferForFile = false;
    public boolean timeStamp = true;
    public boolean threadName = true;
    public boolean stackTrace = true;
    public boolean display = true;
    public boolean disable = false;
    public Consumer<Supplier<String>> override;
    protected DateTimeFormatter timeStringFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    protected ExecutorService exe = Executors.newSingleThreadExecutor();
    public final ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<>();

    protected Log() {

    }

    public static void useTimeFormat(String format) {
        useTimeFormat(mainLog, format);
    }

    public static void useTimeFormat(Log log, String format) {
        log.timeStringFormat = DateTimeFormatter.ofPattern(format);
    }

    public static void changeStream(Log log, LogStream c, String... path) throws IOException {
        log.isFileOpen = false;
        if (null == c) {
            log.printStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
        } else {
            switch (c) {
                case FILE:
                    log.printStream = new PrintStream(path[0], "UTF-8");
                    log.isFileOpen = true;
                    break;
                case STD_ERR:
                    log.printStream = new PrintStream(new FileOutputStream(FileDescriptor.err));
                    break;
                default:
                    log.printStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
                    break;
            }
        }
    }

    public static void changeStream(LogStream c, String... path) throws IOException {
        changeStream(mainLog, c, path);
    }

    public static void await(Log log, long timeout, TimeUnit tu) throws InterruptedException {
        ExecutorService serv = log.exe;
        log.exe = Executors.newSingleThreadExecutor();
        serv.shutdown();
        serv.awaitTermination(timeout, tu);
    }

    public static void await(long timeout, TimeUnit tu) throws InterruptedException {
        await(mainLog, timeout, tu);
    }

    public static void flushBuffer() {
        flushBuffer(mainLog);
    }

    public static void flushBuffer(Log log) {
        while (!log.list.isEmpty()) {
            String string = log.list.pollFirst();
            log.printStream.println(string);
        }
    }

    public static void close() {
        close(mainLog);
    }

    public static void close(Log log) {
        try {
            log.exe.shutdown();
            log.exe.awaitTermination(10, TimeUnit.MINUTES);
            log.printStream.flush();
            if (log.isFileOpen) {
                log.printStream.close();
            }
        } catch (InterruptedException ex) {
        }
    }

    public static void printLines(Collection col) {
        printLines(mainLog, col);
    }

    public static void printLines(Log log, Collection col) {
        if (log.disable) {
            return;
        }
        processString(log, printLinesDecorator.apply(col));
    }

    public static <T> void print(Log log, T... objects) {
        if (log.disable) {
            return;
        }
        processString(log, printDecorator.apply(objects));
    }

    public static <T> void print(T... objects) {
        print(mainLog, objects);
    }

    public static <T> void println(Log log, T... objects) {
        if (log.disable) {
            return;
        }

        processString(log, printLnDecorator.apply(objects));
    }

    public static <T> void println(T... objects) {
        println(mainLog, objects);
    }

//    private static StackTraceElement getStackElement(int elem) {
//        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
//        return trace[elem];
//    }
    private static StackTraceElement getStackElement(Throwable th, int elem) throws Exception {
//        thMethod.setAccessible(true);
        Object invoke = thMethod.invoke(th, elem);
//        thMethod.setAccessible(false);
        return F.cast(invoke);
    }

    private static Class<Throwable> threadClass = Throwable.class;
    private static Method thMethod = getThrowableMethod();

    private static Method getThrowableMethod() {
        try {
            Method declaredMethod = threadClass.getDeclaredMethod("getStackTraceElement", Integer.TYPE);
            declaredMethod.setAccessible(true);
            return declaredMethod;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void processString(Log log, Supplier<String> string) {
        if (log.override == null) {
            long millis = System.currentTimeMillis();
            final String threadName = Thread.currentThread().getName();

            StringValue trace = new StringValue();
            if (log.stackTrace) {
                Throwable th = new Throwable();
                try {
                    trace.set(getStackElement(th, 3).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (log.async) {
                log.exe.submit(() -> logThis(log, finalPrintDecorator.apply(log, trace.get(), threadName, millis, string.get())));
            } else {
                logThis(log, finalPrintDecorator.apply(log, trace.get(), threadName, millis, string.get()));
            }

        } else {
            log.override.accept(string);
        }
    }

    private static final Lambda.L5R<Log, String, String, Long, String, String> finalPrintDecorator = Lambda.of((Log log, String trace, String name, Long millis, String string) -> {
        String timeSt = log.timeStamp ? getZonedDateTime(log.timeStringFormat, millis) : "";
        String threadSt = log.threadName ? "[" + name + "]" : "";
        int firstComma = trace.indexOf("(");
        int lastComma = trace.indexOf(")");
        if (firstComma > 0 && lastComma > firstComma && lastComma > 0) {
            trace = trace.substring(firstComma + 1, lastComma);
        }
        return timeSt + threadSt + "@" + trace + ":{" + string + "}";
    });

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

    private static void logThis(Log log, String res) {
        if (log.display) {
            System.out.println(res);
        }
        if (log.keepBufferForFile) {
            log.list.add(res);
        }
        if (log.isFileOpen) {
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
        return getPrintStream(mainLog);
    }

    public static PrintStream getPrintStream(Log log) {
        return log.printStream;
    }

}
