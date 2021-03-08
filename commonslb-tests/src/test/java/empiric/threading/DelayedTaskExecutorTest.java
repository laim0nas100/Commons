package empiric.threading;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.values.LongValue;
import lt.lb.commons.threads.executors.scheduled.DelayedTaskExecutor;
import lt.lb.commons.threads.executors.scheduled.ScheduleLoopCondition;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class DelayedTaskExecutorTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        
        DLog.main().async = false;
        try (DelayedTaskExecutor exe = new DelayedTaskExecutor(ForkJoinPool.commonPool(), 5)) {
            exe.schedule(WaitTime.ofSeconds(1), () -> {
                DLog.print("Hi 1");
            });
            exe.schedule(WaitTime.ofSeconds(3), () -> {
                DLog.print("Hi 2");
            });
            
            exe.scheduleWithFixedDelay(WaitTime.ofMillis(500), () -> {
                DLog.print("FAST HI");
            });
            
            exe.schedule(WaitTime.ofSeconds(5), ()->{
                DLog.print("LAST HI 1");
            });
            LongValue val = new LongValue(0);
            exe.scheduleWithFixedDelayAndCondition(ScheduleLoopCondition.loopTimes(10), WaitTime.ofMillis(200), () -> {
                DLog.print("SUPER FAST HI "+val.incrementAndGet());
            });
            
            
            exe.awaitOneShotCompletion().await();
            
            
            DLog.print("END");
        }

    }
}
