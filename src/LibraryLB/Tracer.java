/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class Tracer {

    private Predicate disabled = (s) -> false;

    private Tracer(String s) {
        this.cls = s;
    }

    public Tracer setDisabled(Predicate pr) {
        disabled = pr;
        return this;
    }

    private String cls;

    public static Tracer get(String c) {
        Tracer trace = new Tracer(c);
        return trace;
    }

    public static Tracer get(Class c) {
        return get(c.getName());
    }

    public static PrintStream stream = System.out;
    public static ExecutorService service = Executors.newSingleThreadExecutor();
    public static DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public void print(final String str) {
        if (disabled.test(str)) {
            return;
        }
        final Thread t = Thread.currentThread();
        final long millis = System.currentTimeMillis();

        Callable<String> call = () -> str;
        submit(t, millis, cls, call);
    }

    private static void submit(final Thread t, final long millis, final String cls, final Callable<String> str) {
        Callable run = () -> {
            String strTime = Instant.ofEpochMilli(millis).atZone(ZoneOffset.systemDefault()).format(format);
            String s = strTime + " " + cls + " " + t.getName() + " " + str.call();
            stream.println(s);
            return null;
        };
        service.submit(run);
    }

    public void dump(final Object o) {
        if (this.disabled.test(o)) {
            return;
        }
        final Thread t = Thread.currentThread();
        final long millis = System.currentTimeMillis();
        Callable<String> call = () -> ReflectionUtils.reflectionString(o, 5);
        submit(t, millis, cls, call);
    }

}
