/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 *
 * @author laim0nas100
 */
public class DelayedLog implements Closeable {

    public static final String ERR = "System.err";
    public static final String OUT = "System.out";

    private static final PrinterService serviceERR = new PrinterService(System.err);
    private static final PrinterService serviceOUT = new PrinterService(System.out);
    private final ExecutorService main = Executors.newSingleThreadExecutor();
    private final HashMap<String, PrinterService> map = new HashMap<>();

    public static class PrinterService implements Closeable {

        public PrinterService(PrintStream stream) {
            this.stream = stream;
        }
        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private PrintStream stream;

        public void log(String str) {
            Callable cal = () -> {
                stream.println(str);
                return null;
            };
            executor.submit(cal);
        }

        @Override
        public void close() {
            executor.shutdown();
            this.stream.close();
        }

    }

    public void log(String file, String str) {
        Callable call = () -> {
            this.getPrintService(file).log(str);
            return null;
        };
        main.submit(call);
    }

    public void logTimeStamp(String file, String str) {

        str = new SimpleDateFormat("d mon yyyy hh:mm:ss GMT ").format(new Date()) + str;
        log(file, str);
    }

    private PrinterService getPrintService(String file) throws FileNotFoundException {

        if (file == null || file.equals(OUT)) {
            return serviceOUT;
        }
        if (file.equals(DelayedLog.ERR)) {
            return serviceERR;
        }
        if (map.containsKey(file)) {
            return map.get(file);
        } else {
            PrintStream stream = new PrintStream(new FileOutputStream(file));
            PrinterService service = new PrinterService(stream);
            map.put(file, service);
            return service;
        }
    }

    @Override
    public void close() {
        serviceERR.executor.shutdown();
        serviceOUT.executor.shutdown();
        this.main.shutdown();
        for (PrinterService s : map.values()) {
            s.close();
        }
    }

}
