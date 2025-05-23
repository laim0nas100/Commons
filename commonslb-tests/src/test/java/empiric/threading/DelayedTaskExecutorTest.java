package empiric.threading;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.threads.executors.scheduled.DelayedTaskExecutor;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class DelayedTaskExecutorTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        if (false) {
            Long time = WaitTime.ofDays(400).convert(TimeUnit.NANOSECONDS).time;
            Long minutes = WaitTime.ofMinutes(1).convert(TimeUnit.NANOSECONDS).time;
            DLog.print(time, minutes, time / minutes);
            return;
        }

        DLog.main().async = false;
        try (DelayedTaskExecutor exe = new DelayedTaskExecutor(1)) {
//            exe.schedule(WaitTime.ofSeconds(1), () -> {
//                DLog.print("Hi 1");
//            });
//            exe.schedule(WaitTime.ofSeconds(3), () -> {
//                DLog.print("Hi 2");
//            });
//
            ////            exe.scheduleWithFixedDelay(WaitTime.ofMillis(500), () -> {
////                DLog.print("FAST HI");
////            });
//            LongValue val = new LongValue(0);
//            exe.scheduleWithFixedDelayAndCondition(ScheduleLoopCondition.loopTimes(true, 25), WaitTime.ofMillis(500), () -> {
//                DLog.print("LOOPING HI " + val.incrementAndGet());
//                if (val.get() >= 10) {
//                    throw new Error("Whoops");
//                }
//            });
//
//            Thread.sleep(5000);
////            exe.awaitOneShotCompletion().await();
//        
//            exe.awaitFullCompletion().await();
//            DLog.print("Awaited completion");
//            exe.schedule(WaitTime.ofSeconds(1), () -> {
//                DLog.print("LAST HI");
//            });

            ScheduledFuture<?> scheduleWithFixedDelay = exe.scheduleAtFixedRate(() -> {
                DLog.print("SPEC HI");
                try {
                    Thread.sleep((long) (Math.random() * 1200));
                } catch (InterruptedException ex) {
                    DLog.print("Cancelled");
                    return;
                }
//                DLog.print("after HI");
            }, 1, 1, TimeUnit.SECONDS);
            
            exe.schedule(WaitTime.ofSeconds(10), ()->{
                DLog.print("Cancel from one shot");
                scheduleWithFixedDelay.cancel(true);
            });
            
            exe.awaitOneShotCompletion().await();

//            scheduleWithFixedDelay.cancel(true);
            DLog.print("After cancel");
            exe.close();
            
//            exe.awaitFullCompletion().await();
            DLog.print("END");
        }

    }
}
