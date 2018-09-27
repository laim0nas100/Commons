/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads.sync;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author laim0nas100
 */
public class ConditionalWait {

    private volatile boolean keepWaiting = false;

    public synchronized void conditionalWait() {
        try {
            while (keepWaiting) {
                this.wait();
            }
        } catch (InterruptedException e) {
        }
    }

    public synchronized void conditionalWait(TimeUnit tu, long timeOut) {
        if (!keepWaiting) {
            return;
        }
        long waitTime = tu.convert(timeOut, TimeUnit.MILLISECONDS);
        try {
            while (keepWaiting) {
                this.wait(waitTime);
            }
        } catch (Exception ex) {
        }

    }

    public synchronized void wakeUp() {
        keepWaiting = false;
        this.notifyAll();
    }

    public synchronized void requestWait() {
        keepWaiting = true;
        this.notifyAll();
    }
}
