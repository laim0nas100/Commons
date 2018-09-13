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

/**
 *
 * @author Laimonas Beniušis
 */
public class Log {

    private PrintStream printStream;
    private boolean console = true;
    public static boolean instant = false;
    public static boolean keepBuffer = true;
    public static boolean timeStamp = true;
    public static boolean display = true;
    public static boolean disable = false;
    private static DateTimeFormatter timeStringFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static ExecutorService exe = Executors.newSingleThreadExecutor();
    private static final Log INSTANCE = new Log();
    public final ConcurrentLinkedDeque<String> list;

    protected Log() {

        printStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
        list = new ConcurrentLinkedDeque<>();

    }

    public static Log getInstance() {
        return INSTANCE;
    }

    public static void useTimeFormat(String format, boolean concat) {
        timeStringFormat = DateTimeFormatter.ofPattern(format);

    }

    public static void changeStream(char c, String... path) throws IOException {
        INSTANCE.console = true;
        switch (c) {
            case ('f'): {
                try {
                    INSTANCE.printStream = new PrintStream(path[0], "UTF-8");
                    INSTANCE.console = false;
                } catch (FileNotFoundException ex) {
                }
                break;
            }
            case ('e'): {
                INSTANCE.printStream = new PrintStream(new FileOutputStream(FileDescriptor.err));
                break;
            }
            default: {
                INSTANCE.printStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
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
        while (!INSTANCE.list.isEmpty()) {
            String string = INSTANCE.list.pollFirst();
            INSTANCE.printStream.println(string);
        }

    }

    public static void close() {
        try {
            INSTANCE.exe.shutdown();
            INSTANCE.exe.awaitTermination(10, TimeUnit.MINUTES);
            INSTANCE.printStream.flush();
            if (!INSTANCE.console) {
                INSTANCE.printStream.close();
            }
        } catch (InterruptedException ex) {
        }
    }

    public static void printLines(Collection col) {
        if (disable) {
            return;
        }
        long millis = System.currentTimeMillis();
        final Thread t = Thread.currentThread();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                LineStringBuilder string = new LineStringBuilder();
                if (col.size() > 0) {
                    for (Object s : col) {
                        string.appendLine(s);
                    }
                    string.prependLine();
                }
                logThis(string.toString(), t, millis);
            }
        };
        submit(r);
    }

    public static void print(Object... objects) {
        if (disable) {
            return;
        }
        long millis = System.currentTimeMillis();
        final Thread t = Thread.currentThread();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                LineStringBuilder string = new LineStringBuilder();
                if (objects.length > 0) {
                    for (Object s : objects) {
                        string.append(", " + s);
                    }
                    string.delete(0, 2);
                }
                logThis(string.toString(), t, millis);
            }
        };
        submit(r);

    }

    private static void submit(Runnable run) {
        if (instant) {
            run.run();
        } else {
            INSTANCE.exe.submit(run);
        }
    }

    public static void println(Object... objects) {
        if (disable) {
            return;
        }
        long millis = System.currentTimeMillis();
        final Thread t = Thread.currentThread();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String string = "";
                if (objects.length == 1) {
                    string = String.valueOf(objects[0]);
                } else if (objects.length > 1) {
                    for (Object s : objects) {
                        string += "\n" + String.valueOf(s);
                    }
                    string = string.substring(1);
                }
                logThis(string, t, millis);
            }
        };
        submit(r);
    }

    private static void logThis(String string, Thread thread, long millis) {
        String time = getZonedDateTime(timeStringFormat, millis);
        String res = string;
        if (timeStamp) {
            res = time + "[" + thread.getName() + "] {" + res + "}";
        }
        if (display) {
            System.out.println(res);
        }
        if (keepBuffer) {
            Log.INSTANCE.list.add(res);
        }
        if (!INSTANCE.console) {
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
        return INSTANCE.printStream;
    }
}