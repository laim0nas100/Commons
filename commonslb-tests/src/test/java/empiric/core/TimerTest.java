/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.uncheckedutils.Checked;
/**
 *
 * @author laim0nas100
 */
public class TimerTest {

//    @Test
    public void timerTest() {
        IntegerValue times = new IntegerValue(10);
        Checked.uncheckedRun(() -> {
            while (times.decrementAndGet() > 0) {
                for (int i = 0; i < 10; i++) {
                    DLog.print(System.nanoTime(), Java.getNanoTimePlus(), Java.getNanoTime(), Java.getNanoTime() - Java.getNanoTimePlus(), Long.MIN_VALUE);
                }
                DLog.print();
                Thread.sleep(5000);
            }
        });
    }

}
