/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.concurrent.Callable;

/**
 *
 * @author laim0nas100
 */
public interface UnsafeRunnable extends Runnable {

    public static UnsafeRunnable from(Callable call) {
        return () -> call.call();
    }

    public static UnsafeRunnable from(Runnable run) {
        return () -> run.run();
    }

    @Override
    public default void run() {

        try {
            this.unsafeRun();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void unsafeRun() throws Exception;
}
