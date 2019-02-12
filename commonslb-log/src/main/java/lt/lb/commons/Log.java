package lt.lb.commons;

import java.io.*;
import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lt.lb.commons.containers.LazyValue;
import lt.lb.commons.containers.StringValue;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.parsing.StringOp;
import lt.lb.commons.threads.CloseableExecutor;
import lt.lb.commons.threads.FastWaitingExecutor;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.sync.WaitTime;

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
    protected boolean closeable = false;
    public boolean async = true;
    public boolean keepBufferForFile = false;
    public boolean timeStamp = true;
    public boolean threadName = true;
    public boolean stackTrace = true;
    public boolean display = true;
    public boolean disable = false;
    public boolean surroundString = true;
    protected boolean closed = false;
    public Optional<Consumer<Supplier<String>>> override = Optional.empty();

    /**
     * Override-able decorators.
     */
    /**
     * Final string concatenation
     *
     * Log log, String trace, String name, Long millis, String string
     */
    public Lambda.L5R<Log, String, String, Long, String, String> finalPrintDecorator = DefaultLogDecorators.finalPrintDecorator();

    /**
     * Used in println Object[] objects
     */
    public Lambda.L1R<Object[], Supplier<String>> printLnDecorator = DefaultLogDecorators.printLnDecorator();
    /**
     * Used in print Object[] objects
     */
    public Lambda.L1R<Object[], Supplier<String>> printDecorator = DefaultLogDecorators.printDecorator();
    /**
     * Used in printLines Collection objects
     */
    public Lambda.L1R<Collection, Supplier<String>> printLinesDecorator = DefaultLogDecorators.printLinesDecorator();
    /**
     * Used in printIter
     */
    public Lambda.L1R<Iterator, Supplier<String>> printIterDecorator = DefaultLogDecorators.printIterDecorator();
    
    public Lambda.L1R<Throwable, Supplier<String>> stackTraceSupplier = DefaultLogDecorators.stackTraceSupplier();
    public DateTimeFormatter timeStringFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    public CloseableExecutor exe = new FastWaitingExecutor(1, WaitTime.ofSeconds(10));
    public final ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<>();

    public Log() {

    }

    public static void useTimeFormat(String format) {
        useTimeFormat(main(), format);
    }

    public static void useTimeFormat(Log log, String format) {
        log.timeStringFormat = DateTimeFormatter.ofPattern(format);
    }

    public static void changeStream(Log log, LogStream c, String... path) throws IOException {
        boolean closeable = false;
        PrintStream stream;
        if (null == c) {
            stream = new PrintStream(new FileOutputStream(FileDescriptor.out));
        } else {
            switch (c) {
                case FILE:
                    stream = new PrintStream(path[0], "UTF-8");
                    closeable = true;
                    break;
                case STD_ERR:
                    stream = new PrintStream(new FileOutputStream(FileDescriptor.err));
                    break;
                default:
                    stream = new PrintStream(new FileOutputStream(FileDescriptor.out));
                    break;
            }
        }

        assignStream(log, stream, closeable);
    }

    public static void changeStream(LogStream c, String... path) throws IOException {
        changeStream(main(), c, path);
    }

    public static void assignStream(Log log, PrintStream stream, boolean closeable) {
        close(log);
        log.closed = false;
        log.exe = new FastWaitingExecutor(1, WaitTime.ofSeconds(10));
        log.closeable = closeable;
        log.printStream = stream;
    }

    public static void assignStream(PrintStream stream, boolean closeable) {
        assignStream(main(), stream, closeable);
    }

    public static void await(Log log, long timeout, TimeUnit tu) throws InterruptedException, TimeoutException {
        FutureTask shutdown = Futures.empty();
        log.exe.execute(shutdown);
        try {
            shutdown.get(timeout, tu);
        } catch (ExecutionException e) {
        }
    }

    public static void await(long timeout, TimeUnit tu) throws InterruptedException, TimeoutException {
        await(main(), timeout, tu);
    }

    public static void flushBuffer() {
        flushBuffer(main());
    }

    public static void flushBuffer(Log log) {
        while (!log.list.isEmpty()) {
            String string = log.list.pollFirst();
            if (string != null) {
                log.printStream.println(string);
            }
        }
    }

    public static void close() {
        close(main());
    }

    public static void close(Log log) {
        if (log.closed) {
            throw new IllegalStateException("Is allready closed");
        }
        log.closed = true;
        FutureTask<Void> shutdownRequest = Futures.of(() -> {
            flushBuffer(log);
            log.printStream.flush();
            if (log.closeable) {
                log.printStream.close();
            }
        });
        log.exe.execute(shutdownRequest);
        F.unsafeRun(() -> {
            shutdownRequest.get();
            log.exe.close();
        });

    }

    public static void printLines(Collection col) {
        printLines(main(), col);
    }

    public static void printLines(Log log, Collection col) {
        if (log.disable || log.closed) {
            return;
        }
        processString(log, log.printLinesDecorator.apply(col));
    }

    public static void printLines(Log log, Iterator iter) {
        if (log.disable || log.closed) {
            return;
        }
        processString(log, log.printIterDecorator.apply(iter));
    }

    public static void printLines(Iterator iter) {
        printLines(main(), iter);
    }

    public static <T> void print(Log log, T... objects) {
        if (log.disable || log.closed) {
            return;
        }
        processString(log, log.printDecorator.apply(objects));
    }

    public static <T> void print(T... objects) {
        print(main(), objects);
    }

    public static <T> void println(Log log, T... objects) {
        if (log.disable || log.closed) {
            return;
        }

        processString(log, log.printLnDecorator.apply(objects));
    }

    public static <T> void println(T... objects) {
        println(main(), objects);
    }

    

    private static void processString(Log log, Supplier<String> string) {
        if (!log.override.isPresent()) {
            long millis = System.currentTimeMillis();
            final String threadName = Thread.currentThread().getName();

            StringValue trace = new StringValue("");
            if (log.stackTrace) {
                Throwable th = new Throwable();
                trace.set(log.stackTraceSupplier.apply(th).get());
            }

            if (log.async) {
                log.exe.execute(() -> logThis(log, log.finalPrintDecorator.apply(log, trace.get(), threadName, millis, string.get())));
            } else {
                logThis(log, log.finalPrintDecorator.apply(log, trace.get(), threadName, millis, string.get()));
            }

        } else {
            log.override.get().accept(string);
        }
    }

    private static void logThis(Log log, String res) {
        if (log.display) {
            System.out.println(res);
        }

        if (log.keepBufferForFile || log.closeable) {
            log.list.add(res);
        }
        if (log.closeable) {
            flushBuffer(log);
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
        return getPrintStream(main());
    }

    public static PrintStream getPrintStream(Log log) {
        return log.printStream;
    }


}
