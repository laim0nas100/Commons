/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class Futures {

    public static <V> FutureTask<V> ofCallable(Callable<V> call) {
        return new FutureTask<>(call);
    }

    public static <V> FutureTask<V> ofSupplier(Supplier<V> call) {
        return new FutureTask<>(call::get);
    }
    
    public static FutureTask<Void> ofRunnable(Runnable r) {
        return new FutureTask<>(() -> {
            r.run();
            return null;
        });
    }

    public static <V> FutureTask<V> empty() {
        return ofCallable(() -> null);
    }

    public static <V> FutureTask<V> chainForward(Future<V> base, Collection<Future> next) {
        return ofCallable(() -> {
            V get = base.get();
            for (Future f : next) {
                f.get();
            }
            return get;
        });
    }

    public static <V> FutureTask<V> chainBackward(Future<V> base, Collection<Future> before) {
        return ofCallable(() -> {
            for (Future f : before) {
                f.get();
            }
            return base.get();
        });
    }
}
