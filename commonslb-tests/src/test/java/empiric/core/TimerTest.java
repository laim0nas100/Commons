/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.Timer;
import lt.lb.commons.containers.IntegerValue;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class TimerTest {

//    @Test
    public void timerTest() {
        IntegerValue times = new IntegerValue(10);
        F.unsafeRun(() -> {
            while (times.decrementAndGet() > 0) {
                for (int i = 0; i < 10; i++) {
                    Log.print(System.nanoTime(), Timer.getNanoTimePlus(), Timer.getNanoTime(), Timer.getNanoTime() - Timer.getNanoTimePlus(), Long.MIN_VALUE);
                }
                Log.print();
                Thread.sleep(5000);
            }
        });
    }

}
