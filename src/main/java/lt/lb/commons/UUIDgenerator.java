/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

/**
 *
 * @author Lemmin
 */
public class UUIDgenerator {

    private volatile static long val = 0L;
    private volatile static long lastTime = 0L;

    public static synchronized String nextUUID(String classID) {

        long time = System.currentTimeMillis();
        if (lastTime == time) {
            val++;
        } else {
            val = 0L;
            lastTime = time;

        }
        return classID + "_" + time + "_" + val;
    }

    public static String nextUUID() {
        return nextUUID("");
    }

}
