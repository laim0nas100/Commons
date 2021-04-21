/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.threading;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.RunnableDecorators;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 */
public class TimeOutTest {

    
    public void timeoutTest1() throws Exception {

        ExecutorService exe = Executors.newScheduledThreadPool(1);
        
        UncheckedRunnable longTask = () -> {
            DLog.print("Sleep init");
            Thread.sleep(4000);
            DLog.print("Sleep done");
        };
        Runnable timeOut = RunnableDecorators.withTimeout(WaitTime.ofSeconds(2), longTask);
        exe.execute(timeOut);
        exe.execute(timeOut);
        exe.execute(timeOut);
        exe.execute(timeOut);

        DLog.print("End");
        exe.shutdown();
        exe.awaitTermination(1, TimeUnit.DAYS);
        DLog.await(1, TimeUnit.DAYS);
    }
//    @Test
    public void timeoutTest2() throws Exception {
        ExecutorService exe = Executors.newScheduledThreadPool(1);
        
        Random rnd = new Random();
        RandomDistribution uniform = RandomDistribution.uniform(rnd);
        UncheckedRunnable longTask = () -> {
            long sleeptime = uniform.nextLong(1500L, 2500L);
            DLog.print("Sleep init " +sleeptime);
            
            Thread.sleep(sleeptime);
            if(sleeptime < 1800L){
                throw new Error("Oopsie");
            }
            DLog.print("Sleep done");
        };
        
        
        UncheckedRunnable timeOut = RunnableDecorators.withTimeoutRepeat(WaitTime.ofSeconds(2),8, longTask);
        FutureTask<Void> of = Futures.ofCallable(UncheckedRunnable.toCallable(timeOut));
        exe.execute(of);

        of.get();
        DLog.print("End");
        exe.shutdown();
        exe.awaitTermination(1, TimeUnit.DAYS);
        DLog.await(1, TimeUnit.DAYS);
    }
}
