/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 *
 * @author laim0nas100
 */
public interface HandledExecutor {

    public Executor getExecutor();

    public default CompletableFuture<Void> submit(Runnable run) {
        return CompletableFuture.runAsync(run, getExecutor());
    }

    public default CompletableFuture<Void> submit(UnsafeRunnable run) {
        return CompletableFuture.runAsync(run, getExecutor());
    }

    public default <T> CompletableFuture<T> submit(Callable<T> call, Consumer<Exception> handler) {
        return submitAsync(call, handler, getExecutor());
    }

    public default <T> CompletableFuture<T> submitAsync(Callable<T> call, Consumer<Exception> handler, Executor exe) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return call.call();
            } catch (Exception e) {
                handler.accept(e);
                return null;
            }
        }, exe);
    }

}
