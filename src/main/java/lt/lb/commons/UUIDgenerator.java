/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Lemmin
 */
public class UUIDgenerator {

    private volatile static AtomicLong val = new AtomicLong(0);
    private volatile static AtomicLong lastTime = new AtomicLong(0);

    public static synchronized String nextUUID(String classID) {

        long time = System.currentTimeMillis();
        long valTo = 0L;
        if (lastTime.compareAndSet(time, time)) {
            valTo = val.incrementAndGet();
        } else {
            lastTime.set(time);
            val.set(0L);

        }
        return classID + "_" + time + "_" + valTo;
    }

    public static String nextUUID() {
        return nextUUID("");
    }

}
