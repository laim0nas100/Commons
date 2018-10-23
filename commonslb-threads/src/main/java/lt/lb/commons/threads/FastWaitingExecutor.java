/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 *  Similar to FastExecutor, but spawns new Threads sparingly.
 *  Simply waiting set time for new tasks become available before exiting.
 *  Default wait time is 1 second.
 * 
 * @author laim0nas100
 */
public class FastWaitingExecutor extends FastExecutor{
    
    
    protected long toWait;
    protected TimeUnit tu;
    
    public FastWaitingExecutor(int maxThreads) {
        this(maxThreads,1,TimeUnit.SECONDS);
    }
    
    public FastWaitingExecutor(int maxThreads, long timeToWait, TimeUnit tu) {
        super(maxThreads);
        this.tasks = new LinkedBlockingDeque<>();
        this.toWait = timeToWait;
        this.tu = tu;
    }
    
    
    @Override
    protected Runnable getMainBody(){
        return () -> {
            LinkedBlockingDeque<Runnable> deque = (LinkedBlockingDeque)tasks;
            Runnable last = null;
            do {
                
                try {
                    last = deque.pollLast(toWait, tu);
                } catch (InterruptedException ex) {
                }
                if (last != null) {
                    last.run();
                }

            }while (!deque.isEmpty() || last != null);
        };
    }
    
    @Override
    protected void startThread(final int maxT) {
        Thread t = new Thread(getRun(maxT));
        t.setName("Fast Waiting Executor " + t.getName());
        t.start();
    }
    
}
