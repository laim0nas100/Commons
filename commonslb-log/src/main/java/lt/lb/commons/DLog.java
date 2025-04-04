package lt.lb.commons;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.executors.CloseableExecutor;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.Checked;

/**
 * Simple logger oriented to debugging, not intended to replace a serious
 * logging framework such as log4j.
 *
 * Debugging using print statements, yes. Sometimes the easiest way is the most
 * efficient one.
 *
 * @author laim0nas100
 */
public class DLog {
    
    private static DLog mainLog = new DLog();
    
    public static DLog main() {
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
     * Set to a minimal log output. No stackTrace, timestamp, threadName and
     * surroundString
     *
     * @param log
     */
    public static void setMinimal(DLog log) {
        log.timeStamp = false;
        log.threadName = false;
        log.stackTrace = false;
        log.surroundString = false;
    }

    /**
     * Set to a minimal log output. No stackTrace, timestamp, threadName and
     * surroundString
     */
    public static void setMinimal() {
        setMinimal(main());
    }

    /**
     * Override-able decorators.
     */
    /**
     * Final string concatenation
     *
     * DLog log, String trace, String name, Long millis, String string
     */
    public Lambda.L5R<DLog, String, String, Long, String, String> finalPrintDecorator = DefaultDLogDecorators.finalPrintDecorator();

    /**
     * Used in println Object[] objects
     */
    public Lambda.L1R<Object[], Supplier<String>> printLnDecorator = DefaultDLogDecorators.printLnDecorator();
    /**
     * Used in print Object[] objects
     */
    public Lambda.L1R<Object[], Supplier<String>> printDecorator = DefaultDLogDecorators.printDecorator();
    /**
     * Used in printLines Collection objects
     */
    public Lambda.L1R<Iterable, Supplier<String>> printLinesDecorator = DefaultDLogDecorators.printLinesDecorator();
    /**
     * Used in printIter
     */
    public Lambda.L1R<Iterator, Supplier<String>> printIterDecorator = DefaultDLogDecorators.printIterDecorator();

    /**
     * Used in printStackStrace
     */
    public Lambda.L3R<Throwable, Integer, Integer, Supplier<String>> printStackDecorator = DefaultDLogDecorators.stackTraceFullSupplier();
    
    public Lambda.L1R<Throwable, Supplier<String>> stackTraceSupplier = DefaultDLogDecorators.stackTraceSupplier();
    public DateTimeFormatter timeStringFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    public CloseableExecutor exe = createDefaultExecutor();
    public final ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<>();
    
    public CloseableExecutor createDefaultExecutor(){
        return new FastWaitingExecutor(1, WaitTime.ofSeconds(1));
    }
    
    public DLog() {
        
    }
    
    public static void useTimeFormat(String format) {
        useTimeFormat(main(), format);
    }
    
    public static void useTimeFormat(DLog log, String format) {
        log.timeStringFormat = DateTimeFormatter.ofPattern(format);
    }
    
    public static void changeStream(DLog log, LogStream c, String... path) throws IOException {
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
    
    public static void assignStream(DLog log, PrintStream stream, boolean closeable) {
        close(log);
        log.closed = false;
        log.exe = log.createDefaultExecutor();
        log.closeable = closeable;
        log.printStream = stream;
    }
    
    public static void assignStream(PrintStream stream, boolean closeable) {
        assignStream(main(), stream, closeable);
    }
    
    public static void await(DLog log, long timeout, TimeUnit tu) throws InterruptedException, TimeoutException {
        if (log.closed) {
            return;
        }
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
    
    public static void flushBuffer(DLog log) {
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
    
    public static void close(DLog log) {
        if (log.closed) {
            throw new IllegalStateException("Is allready closed");
        }
        log.closed = true;
        FutureTask<Void> shutdownRequest = Futures.ofRunnable(() -> {
            flushBuffer(log);
            log.printStream.flush();
            if (log.closeable) {
                log.printStream.close();
            }
        });
        log.exe.execute(shutdownRequest);
        Checked.uncheckedRun(() -> {
            shutdownRequest.get();
            log.exe.close();
        });
        
    }
    
    public static void printLines(Iterable col) {
        printLines(main(), col);
    }
    
    public static void printLines(DLog log, Iterable col) {
        if (log.disable || log.closed) {
            return;
        }
        processString(log, log.printLinesDecorator.apply(col));
    }
    
    public static void printLines(DLog log, Iterator iter) {
        if (log.disable || log.closed) {
            return;
        }
        processString(log, log.printIterDecorator.apply(iter));
    }
    
    public static void printLines(Iterator iter) {
        printLines(main(), iter);
    }
    
    public static void printLines(DLog log, ReadOnlyIterator iter) {
        if (log.disable || log.closed) {
            return;
        }
        processString(log, log.printIterDecorator.apply(iter));
    }
    
    public static void printLines(ReadOnlyIterator iter) {
        printLines(main(), iter);
    }
    
    public static void print(DLog log, Supplier<String> sup) {
        if (log.disable || log.closed) {
            return;
        }
        processString(log, sup);
        
    }
    
    public static void print(Supplier<String> sup) {
        print(DLog.main(), sup);
    }
    
    public static <T> void print(DLog log, T... objects) {
        if (log.disable || log.closed) {
            return;
        }
        processString(log, log.printDecorator.apply(objects));
    }
    
    public static <T> void print(T... objects) {
        print(main(), objects);
    }
    
    public static <T> void println(DLog log, T... objects) {
        if (log.disable || log.closed) {
            return;
        }
        
        processString(log, log.printLnDecorator.apply(objects));
    }
    
    public static <T> void println(T... objects) {
        println(main(), objects);
    }
    
    public static void printStackTrace() {
        if (main().disable || main().closed) {
            return;
        }
        printStackTrace(main(), -1, 0, new Throwable());
    }
    
    public static void printStackTrace(DLog log) {
        if (log.disable || log.closed) {
            return;
        }
        printStackTrace(log, -1, 0, new Throwable());
    }
    
    public static void printStackTrace(int depth) {
        if (main().disable || main().closed) {
            return;
        }
        printStackTrace(main(), depth, 0, new Throwable());
    }
    
    public static void printStackTrace(DLog log, int depth) {
        if (log.disable || log.closed) {
            return;
        }
        printStackTrace(log, depth, 0, new Throwable());
    }
    
    public static void printStackTrace(int depth, Throwable th) {
        if (main().disable || main().closed) {
            return;
        }
        printStackTrace(main(), depth, 0, th);
    }
    
    public static void printStackTrace(DLog log, int depth, Throwable th) {
        if (log.disable || log.closed) {
            return;
        }
        printStackTrace(log, depth, 0, th);
    }
    
    public static void printStackTrace(int depth, int reduceBy, Throwable th) {
        if (main().disable || main().closed) {
            return;
        }
        printStackTrace(main(), depth, reduceBy, th);
    }
    
    public static void printStackTrace(DLog log, int depth, int reduceBy, Throwable th) {
        if (log.disable || log.closed) {
            return;
        }
        long millis = System.currentTimeMillis();
        final String threadName = Thread.currentThread().getName();
        Supplier<String> supplier = log.printStackDecorator.apply(th, depth, reduceBy);
        if (log.override.isPresent()) {
            log.override.get().accept(supplier);
            return;
        }
        
        if (log.async) {
            log.exe.execute(() -> logThis(log, log.finalPrintDecorator.apply(log, "", threadName, millis, supplier.get())));
        } else {
            logThis(log, log.finalPrintDecorator.apply(log, "", threadName, millis, supplier.get()));
        }
        
    }
    
    private static void processString(DLog log, Supplier<String> string) {
        if (!log.override.isPresent()) {
            long millis = System.currentTimeMillis();
            final String threadName = Thread.currentThread().getName();
            
            final String trace = log.stackTrace ? log.stackTraceSupplier.apply(new Throwable()).get() : "";
            
            if (log.async) {
                log.exe.execute(() -> logThis(log, log.finalPrintDecorator.apply(log, trace, threadName, millis, string.get())));
            } else {
                logThis(log, log.finalPrintDecorator.apply(log, trace, threadName, millis, string.get()));
            }
            
        } else {
            log.override.get().accept(string);
        }
    }
    
    private static void logThis(DLog log, String res) {
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
        printProperties(DLog.main(), properties);
    }
    
    public static void printProperties(DLog log, Properties properties) {
        if (log.disable || log.closed) {
            return;
        }
        Object[] toArray = properties.keySet().toArray();
        
        for (Object o : toArray) {
            String property = properties.getProperty((String) o);
            println(log, o.toString() + " : " + property);
        }
    }
    
    public static PrintStream getPrintStream() {
        return getPrintStream(main());
    }
    
    public static PrintStream getPrintStream(DLog log) {
        return log.printStream;
    }
    
}
