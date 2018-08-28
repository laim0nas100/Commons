/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.*;

/**
 *
 * @author Lemmin
 * @param <Type> return type
 */
public class Promise<Type> extends FutureTask<Type> {

    public Promise(Callable<Type> clbl) {
        super(clbl);
    }

    public Promise(UnsafeRunnable run) {
        this(() -> {
            run.run();
            return null;
        });
    }

    public Promise() {
        this(() -> null);
    }

    public Promise<Type> waitFor(Promise... before) {
        Collection<Promise> toWait = new LinkedList<>();
        for (Promise p : before) {
            toWait.add(p);
        }
        return this.waitFor(toWait);
    }

    public Promise<Type> waitFor(Collection<Promise> before) {
        Promise<Type> original = this;
        Promise<Type> newTask = new Promise<>(() -> {
            int i = 0;
            for (Promise p : before) {
                p.get();
            }
            original.run();

            return original.get();
        });

        return newTask;
    }

    public Promise<Type> execute(Executor e) {
        e.execute(this);
        return this;
    }

    public Promise<Type> collect(Collection<Promise> collection) {
        collection.add(this);
        return this;
    }

}
