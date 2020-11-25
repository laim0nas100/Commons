/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.F;
import lt.lb.commons.Java;

/**
 *
 * @author laim0nas100
 * time based UUID generator. Only for reload-able purposes, not for storing outside of memory.
 */
public class UUIDgenerator {

    private volatile static AtomicLong val = new AtomicLong(0);
    private volatile static AtomicLong lastTime = new AtomicLong(0);

    public static String nextUUID(String classID) {

        long time = Java.getNanoTimePlus();
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
    
    
    private static ConcurrentHashMap<String,AtomicLong> generators = new ConcurrentHashMap<>();
    public static String counterUUID(String classID){
        classID = F.nullWrap(classID, "");
        return classID + "_" + generators.computeIfAbsent(classID, id -> new AtomicLong(0)).getAndIncrement();
    }

}
